/*
 * Copyright 2022 Javaxcel
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

package com.github.javaxcel.core.converter.in.support;

import java.util.ArrayList;
import java.util.List;

import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.StringUtils;

/**
 * Splitter of string which is array-like
 *
 * @since 0.9.0
 */
public class StringArraySplitter {

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private final char opener;

    private final char closer;

    private final String delimiter;

    /**
     * Creates a new instance of splitter.
     *
     * @param delimiter delimiter of array
     */
    public StringArraySplitter(String delimiter) {
        this('[', ']', delimiter);
    }

    /**
     * Creates a new instance of splitter.
     *
     * @param opener    character of opening bracket
     * @param closer    character of closing bracket
     * @param delimiter delimiter of array
     */
    public StringArraySplitter(char opener, char closer, String delimiter) {
        Asserts.that(delimiter)
                .describedAs("StringArraySplitter.delimiter is not allowed to be null or empty: '{0}'", delimiter)
                .isNotNull()
                .isNotEmpty();

        this.opener = opener;
        this.closer = closer;
        this.delimiter = delimiter;
    }

    /**
     * Splits the string from only elements in one-dimensional array.
     *
     * @param src array-like string
     * @return separated strings
     */
    public String[] shallowSplit(String src) {
        Asserts.that(src)
                .describedAs("src must be array-like string, but it isn't: '{0}'", src)
                .isNotNull()
                .startsWith(String.valueOf(this.opener))
                .endsWith(String.valueOf(this.closer));

        // Fast return.
        String emptyArrayString = this.opener + "" + this.closer;
        if (src.equals(emptyArrayString)) {
            return EMPTY_STRING_ARRAY;
        }

        StringBuilder sb = new StringBuilder();
        List<String> list = new ArrayList<>();

        for (int i = 0, depth = 0; i < src.length(); i++) {
            char c = src.charAt(i);

            if (c == this.opener) {
                int index = StringUtils.indexOfCurrentClosingBracket(src, i, this.opener, this.closer);
                if (index == -1) {
                    throw new IllegalArgumentException("Unclosed bracket: index " + i + " of " + src);
                }

                depth++;

                // Skips characters of nested array.
                if (depth > 1) {
                    list.add(src.substring(i, index + 1));
                    i = index - 1;
                }

                continue;
            }

            if (c == this.closer) {
                depth--;
                continue;
            }

            if (depth == 1) {
                if (isDelimiterByChar(src, i, this.delimiter)) {
                    // '], '
                    if (src.charAt(i - 1) != this.closer) {
                        list.add(sb.toString());
                        sb.setLength(0);
                    }

                    // Skips characters of delimiter.
                    i = i + this.delimiter.length() - 1;
                } else {
                    sb.append(c);
                }
            }
        }

        // Adds not flushed string as the last element.
        if (sb.length() > 0) {
            list.add(sb.toString());
        }

        // Adds an empty string as null on the last element.
        if (src.endsWith(this.delimiter + this.closer)) {
            list.add("");
        }

        return list.toArray(new String[0]);
    }

    /**
     * Returns length of one-dimensional array.
     *
     * @param str array-like string
     * @return array length
     */
    public int getShallowLength(String str) {
        int length = 0;
        boolean isEmpty = false;

        for (int i = 0, depth = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if (c == this.opener) {
                int index = StringUtils.indexOfCurrentClosingBracket(str, i, this.opener, this.closer);
                if (index == -1) {
                    throw new IllegalArgumentException("Unclosed bracket: index " + i + " of " + str);
                }

                depth++;

                // Checks if str is '[]'.
                if (depth == 1 && str.charAt(i + 1) == this.closer) {
                    isEmpty = true;
                }

                // Skips characters until inner closer.
                if (depth > 1) {
                    i = index - 1;
                }
            }

            if (c == this.closer) {
                depth--;
            }

            if (depth == 1 && isDelimiterByChar(str, i, ", ")) {
                length++;
            }
        }

        if (!isEmpty) {
            length++;
        }

        return length;
    }

    // -------------------------------------------------------------------------------------------------

    private static boolean isDelimiterByChar(String src, int pos, String delimiter) {
        if (StringUtils.isNullOrEmpty(src)) {
            return false;
        }

        if (StringUtils.isNullOrEmpty(delimiter)) {
            return false;
        }

        if (src.length() < delimiter.length()) {
            return false;
        }

        for (int i = 0; i < delimiter.length(); i++) {
            char c0 = delimiter.charAt(i);
            char c1 = src.charAt(pos + i);

            if (c0 != c1) {
                return false;
            }
        }

        return true;
    }

}
