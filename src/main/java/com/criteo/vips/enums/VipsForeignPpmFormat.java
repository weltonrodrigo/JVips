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
 * The netpbm file format to save as. [enum@Vips.ForeignPpmFormat.PBM]
 * images are single bit. [enum@Vips.ForeignPpmFormat.PGM] images are 8,
 * 16, or 32-bits, one band. [enum@Vips.ForeignPpmFormat.PPM] images are
 * 8, 16, or 32-bits, three bands. [enum@Vips.ForeignPpmFormat.PFM]
 * images are 32-bit float pixels. [enum@Vips.ForeignPpmFormat.PNM]
 * images are anymap images -- the image format is used to pick the
 * saver.
 */
public enum VipsForeignPpmFormat {
    /** portable bitmap */
    Pbm(0),
    /** portable greymap */
    Pgm(1),
    /** portable pixmap */
    Ppm(2),
    /** portable float map */
    Pfm(3),
    /** portable anymap */
    Pnm(4);

    private int value;
    private static Map map = new HashMap<VipsForeignPpmFormat, Integer>();

    private VipsForeignPpmFormat(int i) {
      value = i;
    }

    static {
        for (VipsForeignPpmFormat e : VipsForeignPpmFormat.values()) {
            map.put(e.value, e);
        }
    }

    public static VipsForeignPpmFormat valueOf(int i) {
        return (VipsForeignPpmFormat) map.get(i);
    }

    public int getValue() {
      return value;
    }
}
