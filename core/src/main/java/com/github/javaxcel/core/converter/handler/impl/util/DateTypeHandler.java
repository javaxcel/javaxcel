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

package com.github.javaxcel.core.converter.handler.impl.util;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.github.imsejin.common.constant.DateType;
import io.github.imsejin.common.util.StringUtils;

import com.github.javaxcel.core.annotation.ExcelDateTimeFormat;
import com.github.javaxcel.core.converter.handler.AbstractExcelTypeHandler;
import com.github.javaxcel.core.util.ObjectUtils;

/**
 * Handler for type of {@link Date}
 *
 * <p> Notice that {@link SimpleDateFormat} is not thread-safe.
 *
 * @since 0.8.0
 */
public class DateTypeHandler extends AbstractExcelTypeHandler<Date> {

    private static final String DEFAULT_PATTERN = DateType.F_DATE_TIME.getPattern();

    public DateTypeHandler() {
        super(Date.class);
    }

    @Override
    public String write(Date value, Object... arguments) {
        // Resolve field from arguments.
        Field field = ObjectUtils.resolveFirst(Field.class, arguments);
        if (field == null) {
            return stringify(value, DEFAULT_PATTERN);
        }

        ExcelDateTimeFormat annotation = field.getAnnotation(ExcelDateTimeFormat.class);
        if (annotation == null || StringUtils.isNullOrEmpty(annotation.pattern())) {
            return stringify(value, DEFAULT_PATTERN);
        } else {
            return stringify(value, annotation.pattern());
        }
    }

    @Override
    public Date read(String value, Object... arguments) throws ParseException {
        // Resolve field from arguments.
        Field field = ObjectUtils.resolveFirst(Field.class, arguments);
        if (field == null) {
            return parse(value, DEFAULT_PATTERN);
        }

        ExcelDateTimeFormat annotation = field.getAnnotation(ExcelDateTimeFormat.class);
        if (annotation == null || StringUtils.isNullOrEmpty(annotation.pattern())) {
            return parse(value, DEFAULT_PATTERN);
        } else {
            return parse(value, annotation.pattern());
        }
    }

    // -------------------------------------------------------------------------------------------------

    private static String stringify(Date value, String pattern) {
        return new SimpleDateFormat(pattern).format(value);
    }

    private static Date parse(String value, String pattern) throws ParseException {
        return new SimpleDateFormat(pattern).parse(value);
    }

}
