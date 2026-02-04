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
 * How the values in an image should be interpreted. For example, a
 * three-band float image of type [enum@Vips.Interpretation.LAB] should
 * have its pixels interpreted as coordinates in CIE Lab space. RGB and
 * sRGB are treated in the same way. Use the colourspace functions if you
 * want some other behaviour. The gaps in numbering are historical and
 * must be maintained. Allocate new numbers from the end.
 */
public enum VipsInterpretation {
    Error(-1),
    /** generic many-band image */
    Multiband(0),
    /** some kind of single-band image */
    BW(1),
    /** a 1D image, eg. histogram or lookup table */
    Histogram(10),
    /** the first three bands are CIE XYZ */
    Xyz(12),
    /** pixels are in CIE Lab space */
    Lab(13),
    /** the first four bands are in CMYK space */
    Cmyk(15),
    /** implies [enum@Vips.Coding.LABQ] */
    Labq(16),
    /** generic RGB space */
    Rgb(17),
    /** a uniform colourspace based on CMC(1:1) */
    Cmc(18),
    /** pixels are in CIE LCh space */
    Lch(19),
    /** CIE LAB coded as three signed 16-bit values */
    Labs(21),
    /** pixels are sRGB */
    Srgb(22),
    /** pixels are CIE Yxy */
    Yxy(23),
    /** image is in fourier space */
    Fourier(24),
    /** generic 16-bit RGB */
    Rgb16(25),
    /** generic 16-bit mono */
    Grey16(26),
    /** a matrix */
    Matrix(27),
    /** pixels are scRGB */
    Scrgb(28),
    /** pixels are HSV */
    Hsv(29),
    /** pixels are in Oklab colourspace */
    Oklab(30),
    /** pixels are in Oklch colourspace */
    Oklch(31);

    private int value;
    private static Map map = new HashMap<VipsInterpretation, Integer>();

    private VipsInterpretation(int i) {
      value = i;
    }

    static {
        for (VipsInterpretation e : VipsInterpretation.values()) {
            map.put(e.value, e);
        }
    }

    public static VipsInterpretation valueOf(int i) {
        return (VipsInterpretation) map.get(i);
    }

    public int getValue() {
      return value;
    }
}
