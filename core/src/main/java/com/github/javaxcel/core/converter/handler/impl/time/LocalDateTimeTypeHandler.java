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

package com.github.javaxcel.core.converter.handler.impl.time;

import java.time.LocalDateTime;
import java.time.temporal.TemporalQuery;

import io.github.imsejin.common.constant.DateType;

import com.github.javaxcel.core.converter.handler.impl.time.temporal.AbstractTemporalAccessorTypeHandler;

/**
 * Handler for type of {@link LocalDateTime}
 *
 * @since 0.8.0
 */
public class LocalDateTimeTypeHandler extends AbstractTemporalAccessorTypeHandler<LocalDateTime> {

    public LocalDateTimeTypeHandler() {
        super(LocalDateTime.class, DateType.F_DATE_TIME.getFormatter());
    }

    @Override
    protected TemporalQuery<LocalDateTime> getTemporalQuery() {
        return LocalDateTime::from;
    }

}
