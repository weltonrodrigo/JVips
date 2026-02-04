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
 * Flags we associate with an operation.
 * [flags@Vips.OperationFlags.SEQUENTIAL] means that the operation works
 * like [method@Image.conv]: it can process images top-to-bottom with
 * only small non-local references. Every scan-line must be requested,
 * you are not allowed to skip ahead, but as a special case, the very
 * first request can be for a region not at the top of the image. In this
 * case, the first part of the image will be read and discarded Every
 * scan-line must be requested, you are not allowed to skip ahead, but as
 * a special case, the very first request can be for a region not at the
 * top of the image. In this case, the first part of the image will be
 * read and discarded [flags@Vips.OperationFlags.NOCACHE] means that the
 * operation must not be cached by vips.
 * [flags@Vips.OperationFlags.DEPRECATED] means this is an old operation
 * kept in vips for compatibility only and should be hidden from users.
 * [flags@Vips.OperationFlags.UNTRUSTED] means the operation depends on
 * external libraries which have not been hardened against attack. It
 * should probably not be used on untrusted input. Use
 * [func@block_untrusted_set] to block all untrusted operations.
 * [flags@Vips.OperationFlags.BLOCKED] means the operation is prevented
 * from executing. Use [func@Operation.block_set] to enable and disable
 * groups of operations. [flags@Vips.OperationFlags.REVALIDATE] force the
 * operation to run, updating the cache with the new value. This is used
 * by eg. VipsForeignLoad to implement the "revalidate" argument.
 */
public enum VipsOperationFlags {
    /** no flags */
    OperationNone(0),
    /** can work sequentially with a small buffer */
    OperationSequential(1),
    /** deprecated, use [flags@Vips.OperationFlags.SEQUENTIAL] instead */
    OperationSequentialUnbuffered(2),
    /** must not be cached */
    OperationNocache(4),
    /** a compatibility thing */
    OperationDeprecated(8),
    /** not hardened for untrusted input */
    OperationUntrusted(16),
    /** prevent this operation from running */
    OperationBlocked(32),
    /** force the operation to run */
    OperationRevalidate(64);

    private int value;
    private static Map map = new HashMap<VipsOperationFlags, Integer>();

    private VipsOperationFlags(int i) {
      value = i;
    }

    static {
        for (VipsOperationFlags e : VipsOperationFlags.values()) {
            map.put(e.value, e);
        }
    }

    public static VipsOperationFlags valueOf(int i) {
        return (VipsOperationFlags) map.get(i);
    }

    public int getValue() {
      return value;
    }
}
