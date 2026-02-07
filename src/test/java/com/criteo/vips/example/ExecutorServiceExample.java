/*
  Copyright (c) 2020 Criteo

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

import com.criteo.vips.PixelPacket;
import com.criteo.vips.VipsContext;
import com.criteo.vips.VipsException;
import com.criteo.vips.VipsImage;
import com.criteo.vips.VipsTestUtils;
import com.criteo.vips.enums.VipsCompassDirection;
import com.criteo.vips.enums.VipsImageFormat;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Example demonstrating proper JVips usage in an ExecutorService thread pool.
 * 
 * IMPORTANT: This example shows the correct pattern for preventing memory leaks
 * when using JVips in thread pools. The key points are:
 * 
 * 1. Always use try-with-resources for VipsImage to ensure release() is called
 * 2. Call VipsContext.threadShutdown() in finally block before thread exits
 * 3. Call VipsContext.cleanupAfterError() when catching VipsException
 * 4. Optionally monitor memory usage with VipsContext.getMemoryInfo()
 */
public class ExecutorServiceExample {
    public static void main(String[] args) {
        // Set vips memory leak report at exit
        VipsContext.setLeak(true);
        
        // Configure cache limits for long-running applications
        VipsContext.setMaxCache(100);  // Limit number of cached operations
        VipsContext.setMaxCacheMem(50 * 1024 * 1024);  // 50MB cache limit
        
        try {
            final byte[] contents = VipsTestUtils.getByteArray("in_vips.jpg");
            int numCpu = 8;
            int numCall = 256;
            ExecutorService executor = (ExecutorService) Executors.newFixedThreadPool(numCpu);
            List<Callable<Void>> callables = new ArrayList();

            for (int i = 0; i < numCall; ++i) {
                final int taskId = i;
                callables.add(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        try {
                            // Use try-with-resources to ensure release() is called
                            try (VipsImage image = new VipsImage(contents.clone(), contents.length)) {
                                image.thumbnailImage(new Dimension(256, 256), true);
                                image.pad(new Dimension(512, 512),
                                        new PixelPacket(255, 255, 255),
                                        VipsCompassDirection.Centre);
                                byte[] out = image.writeToArray(VipsImageFormat.JPG, false);
                            }
                        } catch (VipsException e) {
                            // Clean up cache on error to prevent memory leak
                            VipsContext.cleanupAfterError();
                            throw e;
                        } finally {
                            // CRITICAL: Clean up thread-local data before thread exits
                            // This prevents memory leaks in thread pool workers
                            VipsContext.threadShutdown();
                        }
                        return null;
                    }
                });
            }

            executor.invokeAll(callables);
            executor.shutdown();
            
            // Print final memory info
            System.out.println("Final memory state: " + VipsContext.getMemoryInfo());
            System.out.println("All " + numCall + " tasks completed successfully");
            
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
