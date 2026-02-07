# Investigation: Adding InputStream API to JVips for Jackson Integration

**Date**: February 2026  
**Author**: GitHub Copilot  
**Issue**: Add support for creating images from InputStream to use with Jackson's `readBinaryValue` to avoid byte[] allocation for large images

## Executive Summary

**Finding**: ✅ **YES - This API addition is FEASIBLE and RECOMMENDED**

Adding an InputStream-based image creation API to JVips is technically feasible and would provide significant benefits for handling large images from JSON/HTTP streams. The implementation requires moderate effort but is well within the scope of JVips' existing architecture.

## Problem Statement

When receiving large images as base64-encoded JSON fields, the current JVips API requires:
1. Jackson deserializes the entire base64 field into a `byte[]` array
2. The `byte[]` is passed to JVips: `new VipsImage(byte[] buffer, int length)`
3. JVips copies the entire buffer into native memory via JNI

For large images (e.g., 10MB+), this creates significant memory pressure with multiple full copies of the image data in memory simultaneously.

### Desired Solution

Use Jackson's streaming API to avoid the intermediate `byte[]` allocation:
```java
try (JsonParser jp = objectMapper.getFactory().createParser(inputStream)) {
    // Navigate to image field
    jp.nextToken();
    
    // Stream directly to JVips without byte[] allocation
    try (VipsImage image = new VipsImage(jp.readBinaryValue(), options)) {
        // Process image...
    }
}
```

However, Jackson's `readBinaryValue()` returns an `InputStream`, not a `byte[]`. JVips currently has no way to consume an `InputStream` directly.

## Current JVips API

### Existing Image Creation Methods

JVips currently supports 4 ways to create images:

1. **From byte array**:
   ```java
   VipsImage(byte[] buffer, int length)
   VipsImage(byte[] buffer, int length, String options)
   ```

2. **From ByteBuffer** (for direct memory):
   ```java
   VipsImage(ByteBuffer buffer, int length)
   VipsImage(ByteBuffer buffer, int length, String options)
   ```

3. **From file path**:
   ```java
   VipsImage(String filename)
   ```

4. **From another image**:
   ```java
   VipsImage(VipsImage image, PixelPacket color)
   ```

### Current Limitations

- No InputStream support
- No streaming/chunked data input
- All data must be loaded into memory before creating an image
- Cannot integrate with Jackson's streaming Base64 decoder

## Technical Analysis

### libvips Capabilities

JVips uses **libvips 8.12.2**, which includes full support for:

1. **VipsSource API** (introduced in libvips 8.9):
   - `vips_image_new_from_source()` - Create image from streaming source
   - `vips_source_new_from_descriptor()` - Create source from file descriptor
   - `vips_source_new_from_memory()` - Create source from memory buffer
   - Custom VipsSource implementation support

2. **True Streaming**:
   - libvips 8.9+ supports reading images in chunks without loading entire files
   - Reduces memory footprint for large images
   - Particularly effective for formats with progressive/chunked encoding (JPEG, PNG, WEBP)

### Implementation Approaches

#### Option 1: Buffer the InputStream (Simple, Lower Memory Efficiency)

**Approach**: Read the entire InputStream into a byte array, then use existing API.

**Pros**:
- No JVips code changes required
- Can be done entirely in user code
- Simple to implement

**Cons**:
- Defeats the purpose - still allocates full byte[]
- No memory savings compared to current approach
- Not a true solution to the problem

**Verdict**: ❌ Not recommended - doesn't solve the problem

#### Option 2: Add InputStream Constructor with Internal Buffering (Moderate Complexity)

**Approach**: Add new constructors that accept InputStream and buffer internally.

```java
// New constructors in VipsImage.java
public VipsImage(InputStream inputStream) throws VipsException, IOException
public VipsImage(InputStream inputStream, String options) throws VipsException, IOException
```

**Implementation**:
- Read InputStream in chunks (e.g., 64KB at a time)
- Build a byte array progressively
- Pass to existing `newFromBuffer()` native method

**Pros**:
- No changes to native C code required
- Works with existing JNI bindings
- Easier to implement and test
- Automatically works with all image formats supported by libvips

**Cons**:
- Still requires full image buffer in memory eventually
- Only modest memory savings (avoids Jackson's allocation, but not JVips')
- Doesn't leverage libvips streaming capabilities

**Verdict**: ⚠️ Acceptable compromise - provides some benefit with minimal complexity

#### Option 3: True Streaming with VipsSource (Complex, Maximum Efficiency)

**Approach**: Implement a custom VipsSource that streams from Java InputStream via JNI callbacks.

**Architecture**:
```
Java InputStream → JNI Callback → Custom VipsSource → vips_image_new_from_source()
```

**Implementation Requirements**:

1. **New Native C Code**:
   ```c
   // Custom VipsSource subclass
   typedef struct {
       VipsSource parent;
       JNIEnv *env;           // JNI environment
       jobject stream_ref;     // Global ref to Java InputStream
       jmethodID read_mid;     // Cached method ID for read()
   } VipsJavaInputStream;
   
   // Override read() to call back to Java
   static gint64 vips_java_stream_read(VipsSource *source, void *data, size_t length) {
       VipsJavaInputStream *jstream = (VipsJavaInputStream*) source;
       // Use JNI to call inputStream.read(byte[], int, int)
       // Copy result to data buffer
   }
   ```

2. **New Java Native Methods**:
   ```java
   private native void newFromInputStream(InputStream stream) throws VipsException;
   private native void newFromInputStream(InputStream stream, String options) throws VipsException;
   ```

3. **New Java Constructors**:
   ```java
   public VipsImage(InputStream inputStream) throws VipsException
   public VipsImage(InputStream inputStream, String options) throws VipsException
   ```

**Pros**:
- Maximum memory efficiency - truly streams data
- No need to buffer entire image in Java memory
- Leverages full power of libvips 8.9+ streaming
- Enables processing of arbitrarily large images
- Future-proof architecture

**Cons**:
- Complex JNI implementation with threading concerns
- Must manage Java object lifecycle across JNI boundary
- Need to handle Java exceptions in native code
- Requires careful memory management
- More extensive testing required
- Potential performance overhead from JNI callbacks

**Verdict**: ✅ Recommended for maximum benefits, but requires significant development effort

## Recommended Implementation

### Phase 1: Simple InputStream Support (Quick Win)

Implement **Option 2** as an initial enhancement:

```java
public VipsImage(InputStream inputStream) throws VipsException, IOException {
    this(inputStream, null);
}

public VipsImage(InputStream inputStream, String options) throws VipsException, IOException {
    // Read stream in chunks to avoid huge single allocation
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    byte[] chunk = new byte[65536]; // 64KB chunks
    int bytesRead;
    
    while ((bytesRead = inputStream.read(chunk)) != -1) {
        buffer.write(chunk, 0, bytesRead);
    }
    
    byte[] imageData = buffer.toByteArray();
    if (options != null) {
        newFromBuffer(imageData, imageData.length, options);
    } else {
        newFromBuffer(imageData, imageData.length);
    }
}
```

**Benefits**:
- Can be implemented in 1-2 hours
- Provides immediate value for Jackson integration
- No native code changes required
- Backward compatible
- Enables the desired Jackson usage pattern

**Example Usage with Jackson**:
```java
ObjectMapper mapper = new ObjectMapper();
try (JsonParser jp = mapper.getFactory().createParser(jsonInputStream)) {
    while (jp.nextToken() != null) {
        if ("image".equals(jp.getCurrentName())) {
            jp.nextToken(); // Move to value
            
            // Get InputStream without allocating byte[]
            try (InputStream imageStream = jp.readBinaryValue()) {
                // JVips now handles the buffering internally
                VipsImage image = new VipsImage(imageStream);
                image.thumbnailImage(new Dimension(800, 600), true);
                byte[] thumbnail = image.writeToArray(VipsImageFormat.JPG, false);
                image.release();
            }
            break;
        }
    }
}
```

### Phase 2: True Streaming (Future Enhancement)

After Phase 1 is stable, implement **Option 3** for maximum efficiency:

1. Create `VipsJavaInputStream` C structure and implementation
2. Add JNI methods for stream-based image creation
3. Handle threading and lifecycle management
4. Add comprehensive tests
5. Update documentation

This would enable truly streaming image processing with minimal memory overhead.

## Impact Assessment

### Memory Impact

For a 10MB base64-encoded image in JSON:

**Current Implementation**:
- Jackson allocates: ~7.5MB (base64 string)
- Jackson decodes to: ~10MB (byte[])
- JVips copies to: ~10MB (native buffer)
- **Peak memory**: ~27.5MB for one image

**With Option 2 (Simple InputStream)**:
- Jackson streams: ~0MB (no intermediate allocation)
- JVips buffers: ~10MB (byte[])
- JVips native: ~10MB
- **Peak memory**: ~20MB (**~27% reduction**)

**With Option 3 (True Streaming)**:
- Jackson streams: ~0MB
- JVips streams: ~64KB (chunk buffer)
- JVips native: ~10MB (decoded image)
- **Peak memory**: ~10MB (**~63% reduction**)

### Performance Impact

- **Option 2**: Negligible performance difference (perhaps slightly slower due to chunked reading)
- **Option 3**: May have minor overhead from JNI callbacks, but gains from reduced GC pressure

### API Compatibility

- Fully backward compatible
- New constructors don't affect existing code
- No changes to existing methods

## Risks and Mitigations

### Risk 1: InputStream Not Repositionable

**Issue**: Some InputStreams don't support `mark()`/`reset()`, which libvips might need for format detection.

**Mitigation**: 
- Document that the InputStream is consumed fully
- For Option 3, buffer the first few KB for format detection if needed

### Risk 2: JNI Complexity (Option 3)

**Issue**: JNI threading and object lifecycle management is error-prone.

**Mitigation**:
- Start with Option 2
- Thorough testing before implementing Option 3
- Use JNI best practices (global refs, proper cleanup)

### Risk 3: Limited Format Support

**Issue**: Not all image formats support streaming equally well.

**Mitigation**:
- Document streaming performance characteristics by format
- Works best with JPEG, PNG, WEBP (progressive formats)

## Testing Requirements

### Unit Tests

1. **Basic Functionality**:
   - Load image from ByteArrayInputStream
   - Load image from FileInputStream
   - Load with various options strings

2. **Edge Cases**:
   - Empty InputStream
   - Corrupted image data
   - Very large images (100MB+)
   - Interleaved reads (chunked data)

3. **Format Coverage**:
   - Test with JPG, PNG, WEBP, GIF, TIFF, AVIF

4. **Integration Tests**:
   - Jackson integration example
   - HTTP stream example
   - Piped streams between threads

### Performance Tests

1. Memory profiling before/after
2. Throughput comparison with byte[] approach
3. Benchmark against various image sizes

## Documentation Requirements

1. **README.md**: Add example of InputStream usage
2. **Javadoc**: Document new constructors with usage examples
3. **Migration Guide**: Show how to upgrade from byte[] to InputStream
4. **Jackson Integration Guide**: Specific examples for the JSON use case

## Implementation Estimate

### Option 2: Simple InputStream Support

**Effort**: 4-8 hours

- Java code: 2 hours
- Tests: 2 hours
- Documentation: 2 hours
- Review/polish: 2 hours

### Option 3: True Streaming with VipsSource

**Effort**: 20-40 hours

- JNI/C code: 8-16 hours
- Java bindings: 4-8 hours
- Tests: 4-8 hours
- Documentation: 2-4 hours
- Review/debugging: 2-4 hours

## Conclusion

**Recommendation**: ✅ **Implement Option 2 now, plan for Option 3 later**

1. **Short-term (Implement Now)**:
   - Add InputStream constructors with internal buffering (Option 2)
   - Provides immediate value for Jackson integration
   - Low implementation risk and effort
   - Delivers 27% memory reduction for the target use case

2. **Long-term (Future Enhancement)**:
   - Design and implement true streaming with VipsSource (Option 3)
   - Achieves 63% memory reduction
   - Positions JVips as a best-in-class solution for high-throughput image processing

The requested feature is not only possible but highly beneficial. The simple implementation (Option 2) can be delivered quickly and provides significant value, while the advanced implementation (Option 3) can be pursued as a future enhancement when resources permit.

## References

- libvips 8.12.2 documentation: https://www.libvips.org/API/current/
- Jackson streaming API: https://github.com/FasterXML/jackson-core
- JVips repository: https://github.com/criteo/JVips
- libvips streaming blog post: https://www.libvips.org/2019/11/29/True-streaming-for-libvips.html

---

**Next Steps**:
1. Review this document with stakeholders
2. Approve implementation approach
3. Create implementation task for Option 2
4. Plan for Option 3 as a future enhancement
