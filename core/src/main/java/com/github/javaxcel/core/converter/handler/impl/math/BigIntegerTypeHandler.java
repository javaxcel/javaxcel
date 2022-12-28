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

import com.github.javaxcel.core.converter.handler.AbstractExcelTypeHandler;
import io.github.imsejin.common.util.NumberUtils;

import java.math.BigInteger;

/**
 * Handler for type of {@link BigInteger}
 *
 * @since 0.8.0
 */
public class BigIntegerTypeHandler extends AbstractExcelTypeHandler<BigInteger> {

    private static final int THRESHOLD = NumberUtils.getNumOfPlaces(Long.MAX_VALUE);

    public BigIntegerTypeHandler() {
        super(BigInteger.class);
    }

    @Override
    protected String writeInternal(BigInteger value, Object... arguments) {
        return value.toString();
    }

    /**
     * Returns a big integer by reading the string.
     *
     * <p> The following table is a benchmark of the way to implement on 20 or more digits of string.
     * It doesn't seem to be a significant difference.
     *
     * <pre>
     *     Benchmark                                   Mode  Cnt    Score    Error  Units
     *     ------------------------------------------------------------------------------
     *     new BigInteger(String)                      avgt    5  273.174 ± 37.939  ns/op
     *     BigInteger.valueOf(Long.parseLong(String))  avgt    5  276.014 ±  5.460  ns/op
     * </pre>
     *
     * <p> When a string consists of 19 or less digits, there is a significant difference.
     *
     * <pre>
     *     Benchmark                                   Mode  Cnt    Score    Error  Units
     *     ------------------------------------------------------------------------------
     *     new BigInteger(String)                      avgt    5  156.571 ± 3.964  ns/op
     *     BigInteger.valueOf(Long.parseLong(String))  avgt    5   83.206 ± 8.982  ns/op
     * </pre>
     *
     * @param value     string value
     * @param arguments optional arguments
     * @return big integer
     */
    @Override
    public BigInteger read(String value, Object... arguments) {
        if (value.length() <= THRESHOLD) {
            long number = Long.parseLong(value);
            return BigInteger.valueOf(number);
        }

        // When a negative 19 digits:
        if (value.charAt(0) == '-' && value.length() - 1 == THRESHOLD) {
            long number = Long.parseLong(value);
            return BigInteger.valueOf(number);
        }

        return new BigInteger(value);
    }

}
