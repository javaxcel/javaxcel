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

package com.github.javaxcel.core.analysis;

import java.lang.reflect.Field;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.github.javaxcel.core.analysis.in.ExcelReadAnalyzer;
import com.github.javaxcel.core.analysis.out.ExcelWriteAnalyzer;
import com.github.javaxcel.core.converter.handler.ExcelTypeHandler;
import com.github.javaxcel.core.validator.ExcelColumnValidator;

/**
 * Result of the field from analyzer
 *
 * @since 0.9.0
 */
public interface ExcelAnalysis {

    /**
     * Returns analyzed field.
     *
     * @return field
     */
    Field getField();

    /**
     * Returns flags.
     *
     * @return flags
     * @see ExcelWriteAnalyzer
     * @see ExcelReadAnalyzer
     */
    int getFlags();

    /**
     * Returns default meta information.
     *
     * @return default meta information
     */
    DefaultMeta getDefaultMeta();

    /**
     * Returns handler.
     *
     * @return type handler
     */
    @Nullable
    ExcelTypeHandler<?> getHandler();

    /**
     * Returns validators.
     *
     * @return column validators
     * @since 0.10.0
     */
    List<ExcelColumnValidator> getValidators();

    /**
     * Has flag boolean.
     *
     * @param flag the flag
     * @return the boolean
     */
    default boolean hasFlag(int flag) {
        int flags = getFlags();
        return (flags & flag) == flag;
    }

    /**
     * Does handler resolved boolean.
     *
     * @return result of resolution of handler for the field
     */
    default boolean doesHandlerResolved() {
        ExcelTypeHandler<?> handler = getHandler();
        return handler != null && handler.getType() != Object.class;
    }

    // -------------------------------------------------------------------------------------------------

    /**
     * Default meta information
     *
     * @since 0.9.0
     */
    interface DefaultMeta {
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
         * Source of default meta information
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

}
