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

/** The SDF to generate, ::: seealso [ctor@Image.sdf]. */
public enum VipsSdfShape {
    /** a circle at @a, radius @r */
    Circle(0),
    /** a box from @a to @b */
    Box(1),
    /** a box with rounded @corners from @a to @b */
    RoundedBox(2),
    /** a line from @a to @b */
    Line(3);

    private int value;
    private static Map map = new HashMap<VipsSdfShape, Integer>();

    private VipsSdfShape(int i) {
      value = i;
    }

    static {
        for (VipsSdfShape e : VipsSdfShape.values()) {
            map.put(e.value, e);
        }
    }

    public static VipsSdfShape valueOf(int i) {
        return (VipsSdfShape) map.get(i);
    }

    public int getValue() {
      return value;
    }
}
