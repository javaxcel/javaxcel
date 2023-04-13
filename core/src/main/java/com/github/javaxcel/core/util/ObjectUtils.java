/*
 * Copyright 2023 Javaxcel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.javaxcel.core.util;

import org.jetbrains.annotations.Nullable;

import io.github.imsejin.common.annotation.ExcludeFromGeneratedJacocoReport;

/**
 * Utilities for {@link Object}.
 *
 * @since 0.10.0
 */
public final class ObjectUtils {

    @ExcludeFromGeneratedJacocoReport
    private ObjectUtils() {
        throw new UnsupportedOperationException(getClass().getName() + " is not allowed to instantiate");
    }

    /**
     * Checks whether the object is null or empty character sequence.
     *
     * <pre><code>
     *     isNullOrEmptyCharSequence(null);                  // true
     *     isNullOrEmptyCharSequence("");                    // true
     *     isNullOrEmptyCharSequence(new StringBuffer();     // true
     *     isNullOrEmptyCharSequence(new StringBuilder();    // true
     *     isNullOrEmptyCharSequence(" ");                   // false
     *     isNullOrEmptyCharSequence(new StringBuffer(" ");  // false
     *     isNullOrEmptyCharSequence(new StringBuilder(" "); // false
     * </code></pre>
     *
     * @param object object
     * @return whether the object is null or empty character sequence
     */
    public static boolean isNullOrEmptyCharSequence(@Nullable Object object) {
        if (object == null) {
            return true;
        }

        if (object instanceof CharSequence) {
            return ((CharSequence) object).length() == 0;
        }

        return false;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T resolveFirst(Class<T> type, Object... arguments) {
        for (Object argument : arguments) {
            if (type.isInstance(argument)) {
                return (T) argument;
            }
        }

        return null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T resolveLast(Class<T> type, Object... arguments) {
        for (int i = arguments.length - 1; i >= 0; i--) {
            Object argument = arguments[i];

            if (type.isInstance(argument)) {
                return (T) argument;
            }
        }

        return null;
    }

}
