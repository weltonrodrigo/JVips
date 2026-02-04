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

package com.criteo.vips.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Some hints about the image loader. [flags@Vips.ForeignFlags.PARTIAL]
 * means that the image can be read directly from the file without
 * needing to be unpacked to a temporary image first.
 * [flags@Vips.ForeignFlags.SEQUENTIAL] means that the loader supports
 * lazy reading, but only top-to-bottom (sequential) access. Formats like
 * PNG can read sets of scanlines, for example, but only in order. If
 * neither PARTIAL or SEQUENTIAL is set, the loader only supports whole
 * image read. Setting both PARTIAL and SEQUENTIAL is an error.
 * [flags@Vips.ForeignFlags.BIGENDIAN] means that image pixels are most-
 * significant byte first. Depending on the native byte order of the host
 * machine, you may need to swap bytes. See [method@Image.copy].
 */
public enum VipsForeignFlags {
    /** no flags set */
    ForeignNone(0),
    /** the image may be read lazilly */
    ForeignPartial(1),
    /** image pixels are most-significant byte first */
    ForeignBigendian(2),
    /** top-to-bottom lazy reading */
    ForeignSequential(4),
    ForeignAll(7);

    private int value;
    private static Map map = new HashMap<VipsForeignFlags, Integer>();

    private VipsForeignFlags(int i) {
      value = i;
    }

    static {
        for (VipsForeignFlags e : VipsForeignFlags.values()) {
            map.put(e.value, e);
        }
    }

    public static VipsForeignFlags valueOf(int i) {
        return (VipsForeignFlags) map.get(i);
    }

    public int getValue() {
      return value;
    }
}
