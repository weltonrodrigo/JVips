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
 * See [method@Image.pipelinev]. Operations can hint the kind of demand
 * geometry they prefer to the VIPS image IO system. These demand styles
 * are given below in order of increasing specialisation. When demanding
 * output from a pipeline, [method@Image.generate] will use the most
 * general style requested by the operations in the pipeline.
 * [enum@Vips.DemandStyle.SMALLTILE] -- This is the most general demand
 * format. Output is demanded in small (around 100x100 pel) sections.
 * This style works reasonably efficiently, even for bizarre operations
 * like 45 degree rotate. [enum@Vips.DemandStyle.FATSTRIP] -- This
 * operation would like to output strips the width of the image and as
 * high as possible. This option is suitable for area operations which do
 * not violently transform coordinates, such as [method@Image.conv].
 * [enum@Vips.DemandStyle.THINSTRIP] -- This operation would like to
 * output strips the width of the image and a few pels high. This option
 * is suitable for point-to-point operations, such as those in the
 * arithmetic package. [enum@Vips.DemandStyle.ANY] -- This image is not
 * being demand-read from a disc file (even indirectly) so any demand
 * style is OK. It's used for things like [ctor@Image.black] where the
 * pixels are calculated. ::: seealso [method@Image.pipelinev].
 */
public enum VipsDemandStyle {
    Error(-1),
    /** demand in small (typically 128x128 pixel) tiles */
    Smalltile(0),
    /** demand in fat (typically 16 pixel high) strips */
    Fatstrip(1),
    /** demand in thin (typically 1 pixel high) strips */
    Thinstrip(2),
    /** demand geometry does not matter */
    Any(3);

    private int value;
    private static Map map = new HashMap<VipsDemandStyle, Integer>();

    private VipsDemandStyle(int i) {
      value = i;
    }

    static {
        for (VipsDemandStyle e : VipsDemandStyle.values()) {
            map.put(e.value, e);
        }
    }

    public static VipsDemandStyle valueOf(int i) {
        return (VipsDemandStyle) map.get(i);
    }

    public int getValue() {
      return value;
    }
}
