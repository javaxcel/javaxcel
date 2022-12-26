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

import com.github.javaxcel.core.converter.handler.impl.time.temporal.AbstractTemporalAccessorTypeHandler;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalQuery;

/**
 * Handler for type of {@link OffsetDateTime}
 *
 * @since 0.8.0
 */
public class OffsetDateTimeTypeHandler extends AbstractTemporalAccessorTypeHandler<OffsetDateTime> {

    public OffsetDateTimeTypeHandler() {
        super(OffsetDateTime.class, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z"));
    }

    @Override
    protected TemporalQuery<OffsetDateTime> getTemporalQuery() {
        return OffsetDateTime::from;
    }

}
