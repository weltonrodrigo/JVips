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

package com.criteo.vips.example;

import com.criteo.vips.VipsContext;
import com.criteo.vips.VipsException;
import com.criteo.vips.VipsImage;
import com.criteo.vips.VipsTestUtils;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Example demonstrating proper error handling and memory management in JVips.
 * 
 * This example simulates a long-running server that processes images, including
 * handling occasional failures (corrupted images, invalid operations, etc.).
 * 
 * Key Points:
 * 1. Always call VipsContext.threadShutdown() before threads exit
 * 2. Call VipsContext.cleanupAfterError() when catching VipsException
 * 3. Monitor cache size and memory usage periodically
 * 4. Set appropriate cache limits for your application
 */
public class ErrorHandlingExample {
    
    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger errorCount = new AtomicInteger(0);
    
    public static void main(String[] args) throws Exception {
        // Enable leak detection
        VipsContext.setLeak(true);
        
        // Configure cache limits for long-running server
        VipsContext.setMaxCache(50);  // Limit cached operations
        VipsContext.setMaxCacheMem(100 * 1024 * 1024);  // 100MB limit
        
        System.out.println("Starting long-running image server simulation...");
        System.out.println("Initial memory: " + VipsContext.getMemoryInfo());
        
        // Load test image
        final byte[] validImage = VipsTestUtils.getByteArray("in_vips.jpg");
        final byte[] corruptedImage = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04};
        
        // Create thread pool
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        // Simulate processing many images with occasional failures
        int totalRequests = 100;
        for (int i = 0; i < totalRequests; i++) {
            final int requestId = i;
            // Inject failure every 10th request
            final byte[] imageData = (i % 10 == 0) ? corruptedImage : validImage;
            
            executor.submit(() -> processImage(requestId, imageData));
            
            // Monitor memory usage periodically
            if (requestId % 25 == 0 && requestId > 0) {
                printMemoryStats(requestId);
            }
        }
        
        // Shutdown and wait for completion
        executor.shutdown();
        boolean terminated = executor.awaitTermination(60, TimeUnit.SECONDS);
        
        if (!terminated) {
            System.err.println("ERROR: Thread pool did not terminate in time!");
            executor.shutdownNow();
        }
        
        // Final statistics
        System.out.println("\n=== Final Statistics ===");
        System.out.println("Total requests: " + totalRequests);
        System.out.println("Successful: " + successCount.get());
        System.out.println("Errors: " + errorCount.get());
        System.out.println("Final memory: " + VipsContext.getMemoryInfo());
        
        // Clean up remaining cache
        VipsContext.cacheDropAll();
        System.out.println("After cleanup: " + VipsContext.getMemoryInfo());
    }
    
    private static void processImage(int requestId, byte[] imageData) {
        try {
            // Process image with proper resource management
            try (VipsImage image = new VipsImage(imageData, imageData.length)) {
                // Perform operations that build the pipeline
                image.thumbnailImage(200, 200, false);
                
                // Write operation triggers pipeline execution
                // This is where failures typically occur
                byte[] result = image.writeJPEGToArray(85, true);
                
                successCount.incrementAndGet();
                if (requestId % 20 == 0) {
                    System.out.println("Request " + requestId + ": Success (" + 
                        result.length + " bytes)");
                }
            }
        } catch (VipsException e) {
            // Handle error gracefully
            errorCount.incrementAndGet();
            System.err.println("Request " + requestId + ": Error - " + e.getMessage());
            
            // IMPORTANT: Clean up cache after error to prevent memory leak
            VipsContext.cleanupAfterError();
            
            // In a real application, you might:
            // - Log the error to monitoring system
            // - Return error response to client
            // - Increment error metrics
            
        } catch (Exception e) {
            System.err.println("Request " + requestId + ": Unexpected error - " + e.getMessage());
            e.printStackTrace();
        } finally {
            // CRITICAL: Clean up thread-local data
            // This must be called before thread exits to prevent memory leaks
            VipsContext.threadShutdown();
        }
    }
    
    private static void printMemoryStats(int requestId) {
        String memInfo = VipsContext.getMemoryInfo();
        System.out.println("Request " + requestId + ": " + memInfo + 
            " | Success: " + successCount.get() + 
            " | Errors: " + errorCount.get());
    }
}
