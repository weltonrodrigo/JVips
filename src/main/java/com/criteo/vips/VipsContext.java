/*
  Copyright (c) 2019 Criteo

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package com.criteo.vips;

public class VipsContext extends Vips {
    /**
     * Output leak information like GObject liveness
     *
     * @param enable Set to True for enabling information
     */
    public static native void setLeak(boolean enable);

    /**
     * Set the number of worker threads that vips should use when running a VipsThreadPool
     *
     * @param concurrency 0 means "default", the number of threads available on the host machine
     */
    public static native void setConcurrency(int concurrency);

    /**
     * Get the number of worker threads that vips should use when running a VipsThreadPool
     *
     * @return thread number
     */
    public static native int getConcurrency();

    /**
     * Set the maximum number of operations we keep in cache
     *
     * @param max maximum number of operation to cache
     */
    public static native void setMaxCache(int max);

    /**
     * Get the maximum number of operations we keep in cache
     *
     * @return maximum number of operation to cache
     */
    public static native int getMaxCache();

    /**
     * Set the maximum amount of tracked memory we allow before we start dropping cached operations
     *
     * @param max_mem maximum amound of tracked memory we use
     */
    public static native void setMaxCacheMem(int max_mem);

    /**
     * Get the maximum amount of tracked memory we allow before we start dropping cached operations
     *
     * @return maximum amound of tracked memory we use
     */
    public static native int getMaxCacheMem();

    /**
     * Shutdown vips context
     */
    public static native void shutdown();

    /**
     * Free thread-local resources for the current thread.
     * 
     * IMPORTANT: Call this before any thread that uses JVips exits if the thread
     * was not created by libvips (i.e., application threads, thread pool workers).
     * Failing to call this will result in memory leaks.
     * 
     * It is safe to call this multiple times, though unnecessary calls may impact performance.
     * This is automatically called by vips_shutdown() for the main thread.
     * 
     * Example usage in a thread pool:
     * <pre>{@code
     * executor.submit(() -> {
     *     try {
     *         VipsImage image = new VipsImage(bytes, length);
     *         // ... process image ...
     *         image.release();
     *     } finally {
     *         VipsContext.threadShutdown();  // Clean up thread-local data
     *     }
     * });
     * }</pre>
     */
    public static native void threadShutdown();

    /**
     * Drop all cached operations.
     * 
     * This can be useful after a failure or when you want to ensure
     * all cached resources are released. The cache stores intermediate
     * computation results to speed up repeated operations, but it can
     * accumulate memory over time, especially after failed operations.
     * 
     * Note: This affects the global cache shared by all threads.
     * 
     * Example usage after an error:
     * <pre>{@code
     * try {
     *     image.writeJPEGToArray(quality, strip);
     * } catch (VipsException e) {
     *     VipsContext.cacheDropAll();  // Clean up cached operations
     *     throw e;
     * }
     * }</pre>
     */
    public static native void cacheDropAll();

    /**
     * Get current number of operations cached.
     * 
     * This can be used for monitoring memory usage and debugging.
     * The cache stores intermediate computation results, and this
     * count indicates how many operations are currently cached.
     * 
     * @return number of cached operations
     */
    public static native int getCacheSize();

    /**
     * Clean up after a failed operation.
     * 
     * This is a convenience method that drops all cached operations.
     * It should be called after catching a VipsException to ensure
     * clean state and prevent memory leaks from accumulated cache entries.
     * 
     * Note: This does NOT clean up thread-local data. Use threadShutdown()
     * before the thread exits for complete cleanup.
     * 
     * Example usage:
     * <pre>{@code
     * try {
     *     // ... vips operations ...
     * } catch (VipsException e) {
     *     VipsContext.cleanupAfterError();
     *     throw e;
     * }
     * }</pre>
     */
    public static void cleanupAfterError() {
        cacheDropAll();
    }

    /**
     * Get diagnostic information about memory usage.
     * 
     * This is useful for debugging memory leaks and monitoring
     * resource usage in production.
     * 
     * @return formatted string with cache and memory information
     */
    public static String getMemoryInfo() {
        return String.format(
            "Cache size: %d operations, Max cache: %d, Max cache mem: %d bytes",
            getCacheSize(),
            getMaxCache(),
            getMaxCacheMem()
        );
    }
}
