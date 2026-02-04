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
 * Sets the word wrapping style for [ctor@Image.text] when used with a
 * maximum width. ::: seealso [ctor@Image.text].
 */
public enum VipsTextWrap {
    /** wrap at word boundaries */
    Word(0),
    /** wrap at character boundaries */
    Char(1),
    /**
     * wrap at word boundaries, but fall back to character boundaries if
     * there is not enough space for a full word
     */
    WordChar(2),
    /** no wrapping */
    None(3);

    private int value;
    private static Map map = new HashMap<VipsTextWrap, Integer>();

    private VipsTextWrap(int i) {
      value = i;
    }

    static {
        for (VipsTextWrap e : VipsTextWrap.values()) {
            map.put(e.value, e);
        }
    }

    public static VipsTextWrap valueOf(int i) {
        return (VipsTextWrap) map.get(i);
    }

    public int getValue() {
      return value;
    }
}
