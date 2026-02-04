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

import java.io.*;
import java.util.logging.Logger;

public class Vips {
    private static final Logger LOGGER = Logger.getLogger("com.criteo.vips.Vips");
    private static final String SYSTEM_NAME = System.getProperty("os.name").toLowerCase();
    private static final String SYSTEM_ARCH = System.getProperty("os.arch").toLowerCase();
    private static final String PLATFORM = detectPlatform();

    /**
     * Detects the current platform as {os}-{arch}.
     * Returns values like: linux-x86_64, linux-aarch64, darwin-x86_64, darwin-aarch64, windows-x86_64
     */
    private static String detectPlatform() {
        String os;
        if (SYSTEM_NAME.contains("linux")) {
            os = "linux";
        } else if (SYSTEM_NAME.contains("mac") || SYSTEM_NAME.contains("darwin")) {
            os = "darwin";
        } else if (SYSTEM_NAME.contains("win")) {
            os = "windows";
        } else {
            os = "unknown";
        }

        String arch;
        if (SYSTEM_ARCH.equals("amd64") || SYSTEM_ARCH.equals("x86_64")) {
            arch = "x86_64";
        } else if (SYSTEM_ARCH.equals("aarch64") || SYSTEM_ARCH.equals("arm64")) {
            arch = "aarch64";
        } else {
            arch = SYSTEM_ARCH;
        }

        return os + "-" + arch;
    }

    private static final String[] LINUX_LIBRARIES = {
            "aom",
            "heif",
            "exif",
            "png16",
            "spng",
            "gif",
            "jpeg",
            "turbojpeg",
            "webp",
            "webpmux",
            "webpdemux",
            "imagequant",
            "lcms2",
            "vips"
    };

    /**
     * Actually, loading embedded libraries doesn't work on Windows 64.
     * An UnsatisfiedLinkError exception is thrown with embedded dll:
     * "Can't find dependent libraries"
     * Thus, JVips.dll should use system libraries.
     * We only provide libimagequant because it's out of windows binaries release.
     * <p>
     * TODO: add Windows 64 embedded libraries
     */
    private final static String[] WINDOWS_LIBRARIES = {
            "libimagequant"
    };

    static {
        try {
            if (tryLoadLibrariesFromJar())
                LOGGER.info("JVips dependencies have been loaded from jar");
            else
                LOGGER.info("Using JVips dependencies installed on system");
            LOGGER.info("Trying to load JVips");
            loadLibraryFromJar("JVips");
            init();
        } catch (IOException e) {
            throw new RuntimeException("Can't load JVips library and/or dependencies");
        } catch (VipsException e) {
            throw new RuntimeException("Can't init JVips");
        }
    }

    private static boolean tryLoadLibrariesFromJar() throws IOException {
        if (isMacOS())
            return true;
        String[] libraries = !isWindows() ? LINUX_LIBRARIES : WINDOWS_LIBRARIES;
        try {
            for (String library : libraries) {
                loadLibraryFromJar(library);
            }
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }

    private static boolean isWindows() {
        return SYSTEM_NAME.indexOf("win") >= 0;
    }

    private static boolean isMacOS() {
        return (SYSTEM_NAME.indexOf("mac") >= 0);
    }

    private static void loadLibraryFromJar(String name) throws IOException {
        String libName = System.mapLibraryName(name);
        File temp;

        // Try platform-specific path first (e.g., linux-x86_64/libJVips.so)
        String platformPath = PLATFORM + "/" + libName;
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(platformPath);

        // Fallback to root path for backward compatibility
        if (in == null) {
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream(libName);
        }

        if (in == null) {
            LOGGER.warning("Could not load lib '" + libName + "' via classloader (tried " + platformPath + " and root)");
            return;
        }

        try (InputStream is = in) {
            byte[] buffer = new byte[1024];
            int read;
            temp = File.createTempFile(libName, "");
            temp.deleteOnExit();
            try (FileOutputStream fos = new FileOutputStream(temp)) {
                while ((read = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
            }
        }
        System.load(temp.getAbsolutePath());
    }

    /**
     * Initialize vips context
     *
     * @throws VipsException
     */
    private static native void init() throws VipsException;
}
