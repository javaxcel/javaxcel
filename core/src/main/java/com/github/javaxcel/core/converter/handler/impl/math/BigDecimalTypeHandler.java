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

package com.github.javaxcel.core.converter.handler.impl.math;

import java.math.BigDecimal;

import com.github.javaxcel.core.converter.handler.AbstractExcelTypeHandler;

/**
 * Handler for type of {@link BigDecimal}
 *
 * @since 0.8.0
 */
public class BigDecimalTypeHandler extends AbstractExcelTypeHandler<BigDecimal> {

    public BigDecimalTypeHandler() {
        super(BigDecimal.class);
    }

    @Override
    public String write(BigDecimal value, Object... arguments) {
        return value.stripTrailingZeros().toString();
    }

    @Override
    public BigDecimal read(String value, Object... arguments) {
        return new BigDecimal(value);
    }

}
