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
 * The set of image types supported by a saver. ::: seealso
 * [class@ForeignSave].
 */
public enum VipsForeignSaveable {
    /** saver supports everything (eg. TIFF) */
    Any(0),
    /** 1 band */
    Mono(1),
    /** 3 bands */
    Rgb(2),
    /** 4 bands */
    Cmyk(4),
    /** an extra band */
    Alpha(8),
    All(15);

    private int value;
    private static Map map = new HashMap<VipsForeignSaveable, Integer>();

    private VipsForeignSaveable(int i) {
      value = i;
    }

    static {
        for (VipsForeignSaveable e : VipsForeignSaveable.values()) {
            map.put(e.value, e);
        }
    }

    public static VipsForeignSaveable valueOf(int i) {
        return (VipsForeignSaveable) map.get(i);
    }

    public int getValue() {
      return value;
    }
}
