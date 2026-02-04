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
 * The selected encoder to use. If libheif hasn't been compiled with the
 * selected encoder, we will fallback to the default encoder for the
 * compression format.
 */
public enum VipsForeignHeifEncoder {
    /** auto */
    Auto(0),
    /** aom */
    Aom(1),
    /** RAV1E */
    Rav1e(2),
    /** SVT-AV1 */
    Svt(3),
    /** x265 */
    X265(4);

    private int value;
    private static Map map = new HashMap<VipsForeignHeifEncoder, Integer>();

    private VipsForeignHeifEncoder(int i) {
      value = i;
    }

    static {
        for (VipsForeignHeifEncoder e : VipsForeignHeifEncoder.values()) {
            map.put(e.value, e);
        }
    }

    public static VipsForeignHeifEncoder valueOf(int i) {
        return (VipsForeignHeifEncoder) map.get(i);
    }

    public int getValue() {
      return value;
    }
}
