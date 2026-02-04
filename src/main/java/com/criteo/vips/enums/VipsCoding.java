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
 * How pixels are coded. Normally, pixels are uncoded and can be
 * manipulated as you would expect. However some file formats code pixels
 * for compression, and sometimes it's useful to be able to manipulate
 * images in the coded format. The gaps in the numbering are historical
 * and must be maintained. Allocate new numbers from the end.
 */
public enum VipsCoding {
    Error(-1),
    /** pixels are not coded */
    None(0),
    /** pixels encode 3 float CIELAB values as 4 uchar */
    Labq(2),
    /** pixels encode 3 float RGB as 4 uchar (Radiance coding) */
    Rad(6);

    private int value;
    private static Map map = new HashMap<VipsCoding, Integer>();

    private VipsCoding(int i) {
      value = i;
    }

    static {
        for (VipsCoding e : VipsCoding.values()) {
            map.put(e.value, e);
        }
    }

    public static VipsCoding valueOf(int i) {
        return (VipsCoding) map.get(i);
    }

    public int getValue() {
      return value;
    }
}
