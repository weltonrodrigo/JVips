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
 * Flags we associate with each object argument. Have separate input &
 * output flags. Both set is an error; neither set is OK. Input gobjects
 * are automatically reffed, output gobjects automatically ref us. We
 * also automatically watch for "destroy" and unlink.
 * [flags@Vips.ArgumentFlags.SET_ALWAYS] is handy for arguments which are
 * set from C. For example, [property@Image:width] is a property that
 * gives access to the Xsize member of struct _VipsImage. We default its
 * 'assigned' to `TRUE` since the field is always set directly by C.
 * [flags@Vips.ArgumentFlags.DEPRECATED] arguments are not shown in help
 * text, are not looked for if required, are not checked for "have-been-
 * set". You can deprecate a required argument, but you must obviously
 * add a new required argument if you do. Input args with
 * [flags@Vips.ArgumentFlags.MODIFY] will be modified by the operation.
 * This is used for things like the in-place drawing operations.
 * [flags@Vips.ArgumentFlags.NON_HASHABLE] stops the argument being used
 * in hash and equality tests. It's useful for arguments like
 * `revalidate` which control the behaviour of the operator cache.
 */
public enum VipsArgumentFlags {
    /** no flags */
    ArgumentNone(0),
    /** must be set in the constructor */
    ArgumentRequired(1),
    /** can only be set in the constructor */
    ArgumentConstruct(2),
    /** can only be set once */
    ArgumentSetOnce(4),
    /** don't do use-before-set checks */
    ArgumentSetAlways(8),
    /** is an input argument (one we depend on) */
    ArgumentInput(16),
    /** is an output argument (depends on us) */
    ArgumentOutput(32),
    /** just there for back-compat, hide */
    ArgumentDeprecated(64),
    /** the input argument will be modified */
    ArgumentModify(128),
    /** the argument is non-hashable */
    ArgumentNonHashable(256);

    private int value;
    private static Map map = new HashMap<VipsArgumentFlags, Integer>();

    private VipsArgumentFlags(int i) {
      value = i;
    }

    static {
        for (VipsArgumentFlags e : VipsArgumentFlags.values()) {
            map.put(e.value, e);
        }
    }

    public static VipsArgumentFlags valueOf(int i) {
        return (VipsArgumentFlags) map.get(i);
    }

    public int getValue() {
      return value;
    }
}
