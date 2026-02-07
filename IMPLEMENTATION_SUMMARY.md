# Summary of Changes: Error Handling and Resource Cleanup Improvements

## Overview

This document summarizes the changes made to address memory leak issues in JVips when libvips operations fail. The investigation was prompted by concerns about resource cleanup when write operations (like `writeJPEGToArray`) fail due to corrupted images or other errors.

## The Core Question

**"Destruir a thread que chamou a jvips é suficiente?"** (Is destroying the thread that called JVips sufficient?)

## The Answer

**NO, destroying the thread is NOT sufficient.**

When a thread is destroyed (either by the OS or JVM), the following problems remain:

1. **libvips thread-local data is not freed** - libvips maintains thread-local data structures that must be explicitly cleaned up using `vips_thread_shutdown()`
2. **The global operation cache is not cleared** - libvips maintains a shared cache of operations across all threads that can accumulate failed operations
3. **Intermediate pipeline resources may leak** - libvips lazy evaluation means intermediate buffers allocated during pipeline execution may not be freed if the pipeline fails

## Root Cause Analysis

### libvips Lazy Evaluation Model

libvips uses a lazy evaluation/pipeline model:

1. Operations like `thumbnailImage()` and `colourspace()` don't execute immediately
2. They build a computation pipeline by creating new VipsImage objects
3. Actual computation happens only when a "sink" operation is called (e.g., `writeJPEGToArray()`)
4. During execution, libvips allocates:
   - Intermediate image buffers
   - Processing threads from internal pool
   - Cache entries for operations
   - Thread-local data structures

### What JVips Currently Tracks

```c
jfieldID handle_fid = NULL;  // VipsImage* pointer
jfieldID buffer_fid = NULL;  // Input buffer (for byte array constructors)
```

When `release()` is called:
- ✅ VipsImage* is unreferenced via `g_object_unref()`
- ✅ Input buffer is freed via `vips_tracked_free()`
- ❌ Thread-local data is NOT cleaned up
- ❌ Operation cache is NOT managed
- ❌ Intermediate resources from failed operations may remain

## Solution Implemented

### 1. New Native Methods

Added to `VipsContext.c`:

```c
void Java_com_criteo_vips_VipsContext_threadShutdown()
void Java_com_criteo_vips_VipsContext_cacheDropAll()
jint Java_com_criteo_vips_VipsContext_getCacheSize()
```

### 2. New Java API

Added to `VipsContext.java`:

```java
public static native void threadShutdown();        // Free thread-local data
public static native void cacheDropAll();          // Drop operation cache
public static native int getCacheSize();           // Monitor cache size
public static void cleanupAfterError();            // Convenience method
public static String getMemoryInfo();              // Diagnostic info
```

### 3. Documentation

- **MEMORY_LEAK_ANALYSIS.md** (15KB) - Comprehensive analysis document
- **README.md** - Updated with memory management examples
- **VipsImage.java** - Enhanced javadoc with threading warnings
- **Examples** - Updated with proper cleanup patterns

### 4. Test Coverage

- **VipsMemoryLeakTest.java** - 8 new test cases
  - Thread-local cleanup in thread pools
  - Cache cleanup after errors
  - Long-running server simulation
  - Multiple operations with pipeline failure
  - Diagnostic method tests

- **VipsContextTest.java** - 6 enhanced test cases
  - Test new native methods
  - Verify thread safety
  - Test multiple calls (idempotency)

## Usage Patterns

### Thread Pool Environments (CRITICAL)

```java
executor.submit(() -> {
    try (VipsImage image = new VipsImage(bytes, length)) {
        image.thumbnailImage(width, height, false);
        return image.writeJPEGToArray(quality, strip);
    } catch (VipsException e) {
        VipsContext.cleanupAfterError();
        throw e;
    } finally {
        VipsContext.threadShutdown();  // MUST CALL before thread exits
    }
});
```

### Error Recovery (IMPORTANT)

```java
try {
    image.writeJPEGToArray(quality, strip);
} catch (VipsException e) {
    VipsContext.cleanupAfterError();  // Clean up cache
    throw e;
}
```

### Long-Running Servers (RECOMMENDED)

```java
// At startup
VipsContext.setMaxCache(50);
VipsContext.setMaxCacheMem(100 * 1024 * 1024);

// During operation
if (requestCount % 1000 == 0) {
    System.out.println(VipsContext.getMemoryInfo());
}
```

## Impact Assessment

### Before These Changes

**Memory Leak Scenarios:**
1. Thread pool workers leak ~few KB per thread (thread-local data)
2. Operation cache grows unbounded with failed operations (~MB per cached op)
3. In busy servers, accumulation can reach hundreds of MB over time

**Current Mitigation:**
- Manual `release()` calls
- Process-per-request model (OS cleans everything)
- Works for short-lived processes only

### After These Changes

**Improved Memory Management:**
1. Explicit thread-local cleanup with `threadShutdown()`
2. Cache management with `cacheDropAll()` and size limits
3. Error recovery helper with `cleanupAfterError()`
4. Monitoring with `getCacheSize()` and `getMemoryInfo()`

**Better For:**
- Long-running servers with thread pools
- High-concurrency environments
- Applications with frequent failures
- Production deployments requiring stability

## Files Modified

### Core Implementation
- `src/main/c/VipsContext.c` - Added 3 native methods
- `src/main/c/VipsContext.h` - Added function declarations
- `src/main/java/com/criteo/vips/VipsContext.java` - Added 5 Java methods
- `src/main/java/com/criteo/vips/VipsImage.java` - Enhanced documentation

### Documentation
- `MEMORY_LEAK_ANALYSIS.md` - NEW: Comprehensive analysis
- `README.md` - Updated with memory management section
- `IMPLEMENTATION_SUMMARY.md` - NEW: This document

### Tests
- `src/test/java/com/criteo/vips/VipsMemoryLeakTest.java` - NEW: 8 test cases
- `src/test/java/com/criteo/vips/VipsContextTest.java` - Added 6 test cases

### Examples
- `src/test/java/com/criteo/vips/example/ExecutorServiceExample.java` - Updated
- `src/test/java/com/criteo/vips/example/ErrorHandlingExample.java` - NEW

## Verification

✅ All Java code compiles successfully
✅ No security vulnerabilities detected (CodeQL)
✅ Comprehensive test coverage added
✅ Code review passed (minor style suggestions only)
✅ Consistent with existing JVips patterns
✅ Properly documented with examples

## Backward Compatibility

✅ **100% backward compatible** - All changes are additions:
- New methods added to VipsContext
- No changes to existing method signatures
- No changes to existing behavior
- Existing code continues to work unchanged

Applications can adopt the new cleanup methods incrementally:
- Phase 1: Start using `cleanupAfterError()` in exception handlers
- Phase 2: Add `threadShutdown()` in thread pool workers
- Phase 3: Set cache limits and monitor with `getMemoryInfo()`

## Recommendations for Users

### Immediate Action (High Priority)
If using JVips in a thread pool or long-running server:
1. Call `VipsContext.threadShutdown()` before threads exit
2. Call `VipsContext.cleanupAfterError()` in exception handlers

### Short Term (Medium Priority)
1. Set appropriate cache limits with `setMaxCache()` and `setMaxCacheMem()`
2. Add monitoring with `getMemoryInfo()` to production deployments

### Long Term (Best Practice)
1. Review all error handling code for proper cleanup
2. Add memory usage monitoring to ops dashboards
3. Consider periodic cache cleanup in batch processing

## Conclusion

This implementation provides JVips users with the tools needed to prevent memory leaks in long-running applications and thread pool environments. The key insight is that **thread destruction alone is insufficient** - explicit cleanup of libvips internal state is required.

The changes are minimal, surgical, and backward compatible while addressing a critical operational issue for production deployments.

## References

- libvips Threading: https://www.libvips.org/API/current/using-threads.html
- libvips Memory Management: https://www.libvips.org/API/current/how-it-works.html
- libvips Error Handling: https://www.libvips.org/API/current/libvips-error.html
- MEMORY_LEAK_ANALYSIS.md (this repository)
