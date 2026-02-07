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

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class VipsContextTest {
    @Test
    public void TestSetLeak() {
        VipsContext.setLeak(true);
    }

    @Test
    public void TestSetConcurrency() {
        VipsContext.setConcurrency(8);
        Assert.assertEquals(8, VipsContext.getConcurrency());
    }

    @Test
    public void TestSetMaxCache() {
        VipsContext.setMaxCache(0);
        Assert.assertEquals(0, VipsContext.getMaxCache());
    }

    @Test
    public void TestSetMaxCacheMem() {
        VipsContext.setMaxCacheMem(1024);
        Assert.assertEquals(1024, VipsContext.getMaxCacheMem());
    }

    @Test
    public void TestThreadShutdown() {
        // Test that threadShutdown can be called without error
        VipsContext.threadShutdown();
        
        // Call it again - should be safe to call multiple times
        VipsContext.threadShutdown();
    }

    @Test
    public void TestCacheDropAll() {
        // Get initial cache size
        int initialSize = VipsContext.getCacheSize();
        
        // Drop all cached operations
        VipsContext.cacheDropAll();
        
        // After drop, cache should be empty
        Assert.assertEquals(0, VipsContext.getCacheSize());
    }

    @Test
    public void TestGetCacheSize() {
        // Just verify we can call it without error
        int size = VipsContext.getCacheSize();
        Assert.assertTrue("Cache size should be non-negative", size >= 0);
    }

    @Test
    public void TestCleanupAfterError() {
        // Test the convenience method
        VipsContext.cleanupAfterError();
        
        // Verify cache was cleared
        Assert.assertEquals(0, VipsContext.getCacheSize());
    }

    @Test
    public void TestGetMemoryInfo() {
        // Test the diagnostic method
        String info = VipsContext.getMemoryInfo();
        Assert.assertNotNull(info);
        Assert.assertTrue("Memory info should contain cache size", 
            info.contains("Cache size"));
        Assert.assertTrue("Memory info should contain max cache", 
            info.contains("Max cache"));
    }

    @Test
    public void TestThreadShutdownInThreadPool() throws Exception {
        // Test that threadShutdown works correctly in a thread pool
        ExecutorService executor = Executors.newFixedThreadPool(4);
        AtomicInteger successCount = new AtomicInteger(0);
        
        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                try {
                    // Just call threadShutdown in each thread
                    VipsContext.threadShutdown();
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        
        executor.shutdown();
        Assert.assertTrue("Thread pool should terminate", 
            executor.awaitTermination(10, TimeUnit.SECONDS));
        Assert.assertEquals("All threads should complete successfully", 
            10, successCount.get());
    }
}

