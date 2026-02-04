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

/** Which metadata to retain. */
public enum VipsForeignKeep {
    /** don't attach metadata */
    None(0),
    /** keep Exif metadata */
    Exif(1),
    /** keep XMP metadata */
    Xmp(2),
    /** keep IPTC metadata */
    Iptc(4),
    /** keep ICC metadata */
    Icc(8),
    /** keep other metadata (e.g. PNG comments) */
    Other(16),
    /** keep the gainmap metadata */
    Gainmap(32),
    /** keep all metadata */
    All(63);

    private int value;
    private static Map map = new HashMap<VipsForeignKeep, Integer>();

    private VipsForeignKeep(int i) {
      value = i;
    }

    static {
        for (VipsForeignKeep e : VipsForeignKeep.values()) {
            map.put(e.value, e);
        }
    }

    public static VipsForeignKeep valueOf(int i) {
        return (VipsForeignKeep) map.get(i);
    }

    public int getValue() {
      return value;
    }
}
