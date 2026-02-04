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


public enum VipsImageType {
    ImageError(-1),
    ImageNone(0),
    ImageSetbuf(1),
    ImageSetbufForeign(2),
    ImageOpenin(3),
    ImageMmapin(4),
    ImageMmapinrw(5),
    ImageOpenout(6),
    ImagePartial(7);

    private int value;
    private static Map map = new HashMap<VipsImageType, Integer>();

    private VipsImageType(int i) {
      value = i;
    }

    static {
        for (VipsImageType e : VipsImageType.values()) {
            map.put(e.value, e);
        }
    }

    public static VipsImageType valueOf(int i) {
        return (VipsImageType) map.get(i);
    }

    public int getValue() {
      return value;
    }
}
