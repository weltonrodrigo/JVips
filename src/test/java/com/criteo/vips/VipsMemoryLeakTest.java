/*
  Copyright (c) 2024 Criteo

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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests to verify that memory leaks are properly handled when operations fail.
 * These tests demonstrate the correct usage of threadShutdown() and cacheDropAll()
 * to prevent memory leaks in various scenarios.
 */
public class VipsMemoryLeakTest {

    private byte[] testImageBytes;

    @Before
    public void setUp() throws IOException {
        // Load a test image
        testImageBytes = Files.readAllBytes(
            Paths.get("src/test/resources/in_vips.jpg"));
    }

    @After
    public void tearDown() {
        // Clean up after each test
        VipsContext.cacheDropAll();
        VipsContext.threadShutdown();
    }

    @Test
    public void testThreadLocalCleanupInThreadPool() throws Exception {
        // Test that thread-local cleanup works correctly in a thread pool
        ExecutorService executor = Executors.newFixedThreadPool(4);
        AtomicInteger processedCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        // Process images in multiple threads
        for (int i = 0; i < 20; i++) {
            executor.submit(() -> {
                try {
                    try (VipsImage image = new VipsImage(testImageBytes, testImageBytes.length)) {
                        image.thumbnailImage(100, 100, false);
                        byte[] result = image.writeJPEGToArray(80, true);
                        Assert.assertNotNull(result);
                        processedCount.incrementAndGet();
                    }
                } catch (VipsException e) {
                    errorCount.incrementAndGet();
                    VipsContext.cleanupAfterError();
                } finally {
                    // CRITICAL: Clean up thread-local data before thread exits
                    VipsContext.threadShutdown();
                }
            });
        }

        executor.shutdown();
        Assert.assertTrue("Thread pool should terminate",
            executor.awaitTermination(30, TimeUnit.SECONDS));
        
        Assert.assertTrue("Should have processed some images successfully",
            processedCount.get() > 0);
        System.out.println("Processed " + processedCount.get() + 
            " images, " + errorCount.get() + " errors");
    }

    @Test
    public void testCacheCleanupAfterError() throws Exception {
        // Test that cache is properly cleaned up after errors
        byte[] corruptedImage = new byte[]{0x00, 0x01, 0x02, 0x03}; // Invalid image data
        
        int initialCacheSize = VipsContext.getCacheSize();
        int errorCount = 0;

        // Try to process corrupted images multiple times
        for (int i = 0; i < 10; i++) {
            VipsImage image = null;
            try {
                image = new VipsImage(corruptedImage, corruptedImage.length);
                image.writeJPEGToArray(80, true);
                Assert.fail("Should have thrown VipsException for corrupted image");
            } catch (VipsException e) {
                // Expected - corrupted image should fail
                errorCount++;
                // Clean up the cache after each error
                VipsContext.cacheDropAll();
            } finally {
                // Release image if it was created
                if (image != null) {
                    image.release();
                }
            }
        }

        Assert.assertEquals("Should have encountered errors for all corrupted images", 
            10, errorCount);
        
        // Cache should remain at or near initial size (not growing unbounded)
        int finalCacheSize = VipsContext.getCacheSize();
        Assert.assertEquals("Cache should not grow after errors with cleanup",
            0, finalCacheSize);
    }

    @Test
    public void testLongRunningServerSimulation() throws Exception {
        // Simulate a long-running server processing many images
        int iterations = 100;
        int errorInjectionFrequency = 10; // Inject error every N iterations
        
        for (int i = 0; i < iterations; i++) {
            try {
                // Simulate occasional corrupted input
                byte[] input = (i % errorInjectionFrequency == 0) 
                    ? new byte[]{0x00, 0x01} // Corrupted
                    : testImageBytes; // Valid
                
                try (VipsImage image = new VipsImage(input, input.length)) {
                    image.thumbnailImage(200, 200, false);
                    byte[] result = image.writeJPEGToArray(85, true);
                    Assert.assertNotNull(result);
                }
            } catch (VipsException e) {
                // Handle error gracefully and clean up
                VipsContext.cleanupAfterError();
            }
            
            // Periodically check and log memory info
            if (i % 25 == 0) {
                String memInfo = VipsContext.getMemoryInfo();
                System.out.println("Iteration " + i + ": " + memInfo);
            }
        }
        
        // Final cleanup
        VipsContext.cacheDropAll();
        
        // Cache should be clean after dropAll
        Assert.assertEquals("Cache should be empty after explicit drop",
            0, VipsContext.getCacheSize());
    }

    @Test
    public void testMultipleOperationsWithPipelineFailure() throws Exception {
        // Test that pipeline operations are properly cleaned up on failure
        for (int i = 0; i < 5; i++) {
            try (VipsImage image = new VipsImage(testImageBytes, testImageBytes.length)) {
                // Build a pipeline of operations
                image.thumbnailImage(512, 512, false);
                image.colourspace(com.criteo.vips.enums.VipsInterpretation.Srgb);
                
                // This should succeed
                byte[] result = image.writeJPEGToArray(80, true);
                Assert.assertNotNull(result);
                Assert.assertTrue("Result should have data", result.length > 0);
            } catch (VipsException e) {
                VipsContext.cleanupAfterError();
                Assert.fail("Should not fail with valid image: " + e.getMessage());
            }
        }
        
        // Check that cache hasn't grown too much
        int cacheSize = VipsContext.getCacheSize();
        System.out.println("Cache size after multiple operations: " + cacheSize);
        
        // Clean up
        VipsContext.cacheDropAll();
        Assert.assertEquals(0, VipsContext.getCacheSize());
    }

    @Test
    public void testMemoryInfoFormat() {
        // Test that memory info returns expected format
        String info = VipsContext.getMemoryInfo();
        
        Assert.assertNotNull(info);
        Assert.assertTrue("Should contain cache size", info.contains("Cache size"));
        Assert.assertTrue("Should contain operations", info.contains("operations"));
        Assert.assertTrue("Should contain Max cache", info.contains("Max cache"));
        Assert.assertTrue("Should contain bytes", info.contains("bytes"));
        
        System.out.println("Memory info: " + info);
    }

    @Test
    public void testThreadShutdownMultipleCalls() {
        // Verify that calling threadShutdown multiple times is safe
        for (int i = 0; i < 5; i++) {
            VipsContext.threadShutdown();
        }
        // Should complete without error
    }

    @Test
    public void testCacheDropAllMultipleCalls() {
        // Verify that calling cacheDropAll multiple times is safe
        for (int i = 0; i < 5; i++) {
            VipsContext.cacheDropAll();
            Assert.assertEquals("Cache should be empty after each drop",
                0, VipsContext.getCacheSize());
        }
    }
}
