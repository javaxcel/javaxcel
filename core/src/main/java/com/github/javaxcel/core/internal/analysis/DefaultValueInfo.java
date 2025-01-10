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

package com.github.javaxcel.core.internal.analysis;

import org.jetbrains.annotations.Nullable;

/**
 * Default value information
 *
 * @since 0.9.0
 */
public interface DefaultValueInfo {

    /**
     * Returns default value.
     *
     * @return default value for the field value
     */
    @Nullable
    String getValue();

    /**
     * Returns source.
     *
     * @return source of the default value
     */
    Source getSource();

    /**
     * Source of default value information
     *
     * @since 0.9.0
     */
    enum Source {
        /**
         * From nowhere.
         */
        NONE,

        /**
         * From class.
         */
        MODEL,

        /**
         * From field.
         */
        COLUMN,

        /**
         * From strategy.
         */
        OPTION
    }

}
