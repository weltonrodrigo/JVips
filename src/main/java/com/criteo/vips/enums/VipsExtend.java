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
 * See [method@Image.embed], [method@Image.conv], [method@Image.affine]
 * and so on. When the edges of an image are extended, you can specify
 * how you want the extension done. [enum@Vips.Extend.BLACK] -- new
 * pixels are black, ie. all bits are zero. [enum@Vips.Extend.COPY] --
 * each new pixel takes the value of the nearest edge pixel
 * [enum@Vips.Extend.REPEAT] -- the image is tiled to fill the new area
 * [enum@Vips.Extend.MIRROR] -- the image is reflected and tiled to
 * reduce hash edges [enum@Vips.Extend.WHITE] -- new pixels are white,
 * ie. all bits are set [enum@Vips.Extend.BACKGROUND] -- colour set from
 * the @background property We have to specify the exact value of each
 * enum member since we have to keep these frozen for back compat with
 * vips7. ::: seealso [method@Image.embed].
 */
public enum VipsExtend {
    /** extend with black (all 0) pixels */
    Black(0),
    /** copy the image edges */
    Copy(1),
    /** repeat the whole image */
    Repeat(2),
    /** mirror the whole image */
    Mirror(3),
    /** extend with white (all bits set) pixels */
    White(4),
    /** extend with colour from the @background property */
    Background(5);

    private int value;
    private static Map map = new HashMap<VipsExtend, Integer>();

    private VipsExtend(int i) {
      value = i;
    }

    static {
        for (VipsExtend e : VipsExtend.values()) {
            map.put(e.value, e);
        }
    }

    public static VipsExtend valueOf(int i) {
        return (VipsExtend) map.get(i);
    }

    public int getValue() {
      return value;
    }
}
