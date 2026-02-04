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
 * The set of coding types supported by a saver. ::: seealso
 * [enum@Coding].
 */
public enum VipsForeignCoding {
    /** saver supports [enum@Vips.Coding.NONE] */
    None(1),
    /** saver supports [enum@Vips.Coding.LABQ] */
    Labq(2),
    /** saver supports [enum@Vips.Coding.RAD] */
    Rad(4),
    /** saver supports all coding types */
    All(7);

    private int value;
    private static Map map = new HashMap<VipsForeignCoding, Integer>();

    private VipsForeignCoding(int i) {
      value = i;
    }

    static {
        for (VipsForeignCoding e : VipsForeignCoding.values()) {
            map.put(e.value, e);
        }
    }

    public static VipsForeignCoding valueOf(int i) {
        return (VipsForeignCoding) map.get(i);
    }

    public int getValue() {
      return value;
    }
}
