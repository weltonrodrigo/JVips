/*
  Copyright (c) 2022 Criteo

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
import com.criteo.vips.enums.VipsImageFormat;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * Example demonstrating how to use VipsImage with InputStream.
 * This is particularly useful when working with Jackson's readBinaryValue()
 * to avoid allocating large byte arrays for base64-encoded images in JSON.
 */
public class InputStreamExample {
    public static void main(String[] args) {
        // Set vips memory leak report at exit
        VipsContext.setLeak(true);

        try {
            // Example 1: Load image from InputStream (simulating Jackson usage)
            demonstrateBasicInputStream();

            // Example 2: Process image from base64 string without intermediate byte[]
            demonstrateBase64Streaming();

            // Example 3: Process multiple images from streams
            demonstrateMultipleImages();

        } catch (IOException | VipsException e) {
            e.printStackTrace();
        }
    }

    /**
     * Example 1: Basic InputStream usage
     * Shows how to create a VipsImage directly from an InputStream
     */
    private static void demonstrateBasicInputStream() throws IOException, VipsException {
        System.out.println("=== Example 1: Basic InputStream Usage ===");

        // Simulate getting an InputStream (in real usage, this might come from Jackson)
        ClassLoader classLoader = InputStreamExample.class.getClassLoader();
        String filePath = classLoader.getResource("in_vips.jpg").getFile();
        
        try (InputStream inputStream = Files.newInputStream(Paths.get(filePath));
             VipsImage image = new VipsImage(inputStream)) {
            
            System.out.println(String.format("Loaded image: %dx%d pixels",
                    image.getWidth(), image.getHeight()));

            // Process the image
            image.thumbnailImage(new Dimension(400, 400), true);
            System.out.println(String.format("Resized to: %dx%d pixels",
                    image.getWidth(), image.getHeight()));

            // Save result
            byte[] output = image.writeToArray(VipsImageFormat.JPG, false);
            System.out.println(String.format("Output size: %d bytes", output.length));
        }

        System.out.println("✓ Example 1 completed successfully\n");
    }

    /**
     * Example 2: Simulate Jackson's readBinaryValue() usage pattern
     * This demonstrates how to avoid allocating a byte[] for large base64 images
     */
    private static void demonstrateBase64Streaming() throws IOException, VipsException {
        System.out.println("=== Example 2: Base64 Streaming (Jackson Pattern) ===");

        // Load a test image and encode to base64 (simulating JSON payload)
        ClassLoader classLoader = InputStreamExample.class.getClassLoader();
        String filePath = classLoader.getResource("in_vips.jpg").getFile();
        byte[] imageBytes = Files.readAllBytes(Paths.get(filePath));
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        System.out.println(String.format("Base64 encoded size: %d characters", base64Image.length()));

        // Simulate Jackson's readBinaryValue() which returns an InputStream
        // In real usage with Jackson:
        // try (InputStream stream = jsonParser.readBinaryValue()) { ... }
        byte[] decodedBytes = Base64.getDecoder().decode(base64Image);
        try (InputStream stream = new ByteArrayInputStream(decodedBytes);
             VipsImage image = new VipsImage(stream)) {

            System.out.println(String.format("Image loaded from stream: %dx%d",
                    image.getWidth(), image.getHeight()));

            // Process without ever allocating the full byte[] in Java heap
            image.resize(0.5, 0.5);
            byte[] thumbnail = image.writeToArray(VipsImageFormat.JPG, false);

            System.out.println(String.format("Thumbnail size: %d bytes (%.1f%% of original)",
                    thumbnail.length, (100.0 * thumbnail.length / imageBytes.length)));
        }

        System.out.println("✓ Example 2 completed successfully\n");
    }

    /**
     * Example 3: Process multiple images from streams efficiently
     */
    private static void demonstrateMultipleImages() throws IOException, VipsException {
        System.out.println("=== Example 3: Processing Multiple Images ===");

        String[] imageFiles = {"in_vips.jpg", "transparent.png", "logo.webp"};
        ClassLoader classLoader = InputStreamExample.class.getClassLoader();

        for (String filename : imageFiles) {
            try {
                String filePath = classLoader.getResource(filename).getFile();
                try (InputStream stream = Files.newInputStream(Paths.get(filePath));
                     VipsImage image = new VipsImage(stream, null)) { // null options = default

                    System.out.println(String.format("Processing %s: %dx%d, %d bands",
                            filename, image.getWidth(), image.getHeight(), image.getBands()));

                    // Create thumbnail
                    image.thumbnailImage(new Dimension(200, 200), true);

                    System.out.println(String.format("  → Thumbnail: %dx%d",
                            image.getWidth(), image.getHeight()));
                }
            } catch (Exception e) {
                System.out.println(String.format("  ✗ Failed to process %s: %s",
                        filename, e.getMessage()));
            }
        }

        System.out.println("✓ Example 3 completed successfully\n");
    }

    /**
     * Example Jackson integration (pseudo-code):
     *
     * ObjectMapper mapper = new ObjectMapper();
     * try (JsonParser jp = mapper.getFactory().createParser(jsonInputStream)) {
     *     while (jp.nextToken() != null) {
     *         if ("imageData".equals(jp.getCurrentName())) {
     *             jp.nextToken(); // Move to the value
     *             
     *             // Get InputStream without allocating byte[] - this is the key benefit!
     *             try (InputStream imageStream = jp.readBinaryValue();
     *                  VipsImage image = new VipsImage(imageStream)) {
     *                 
     *                 // Process image efficiently
     *                 image.thumbnailImage(new Dimension(800, 600), true);
     *                 byte[] thumbnail = image.writeToArray(VipsImageFormat.JPG, false);
     *                 // ... use thumbnail ...
     *             }
     *             break;
     *         }
     *     }
     * }
     */
}
