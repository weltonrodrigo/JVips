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
 * See [method@Image.join] and so on. Operations like [method@Image.join]
 * need to be told whether to align images on the low or high coordinate
 * edge, or centre. ::: seealso [method@Image.join].
 */
public enum VipsAlign {
    /** align low coordinate edge */
    Low(0),
    /** align centre */
    Centre(1),
    /** align high coordinate edge */
    High(2);

    private int value;
    private static Map map = new HashMap<VipsAlign, Integer>();

    private VipsAlign(int i) {
      value = i;
    }

    static {
        for (VipsAlign e : VipsAlign.values()) {
            map.put(e.value, e);
        }
    }

    public static VipsAlign valueOf(int i) {
        return (VipsAlign) map.get(i);
    }

    public int getValue() {
      return value;
    }
}
