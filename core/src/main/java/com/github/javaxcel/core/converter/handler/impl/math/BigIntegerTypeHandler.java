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

import java.math.BigInteger;

/**
 * Handler for type of {@link BigInteger}
 *
 * @since 0.8.0
 */
public class BigIntegerTypeHandler extends AbstractExcelTypeHandler<BigInteger> {

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
     * <p> The following table is a benchmark with each 2.5 million randomized numbers.
     *
     * <pre>
     *     {@code new BigInteger(String)}
     *
     *     TOTAL = 5986.9188 ms
     *     AVERAGE = 598.69188 ms
     *     ---------------
     *     ms        %
     *     ---------------
     *     851.6429  14.23
     *     668.1394  11.16
     *     602.9215  10.07
     *     672.3112  11.23
     *     551.5461  9.21
     *     573.9861  9.59
     *     488.5583  8.16
     *     543.9001  9.08
     *     537.2404  8.97
     *     496.6728  8.30
     *
     *     ---------------
     *
     *     {@code BigInteger.valueOf(Long.parseLong(String))}
     *
     *     TOTAL = 4128.9385 ms
     *     AVERAGE = 412.89385 ms
     *     ---------------
     *     ms        %
     *     ---------------
     *     505.5069  12.24
     *     879.2133  21.29
     *     386.4251  9.36
     *     368.237   8.92
     *     361.3653  8.75
     *     342.8972  8.30
     *     306.1711  7.42
     *     294.0744  7.12
     *     376.186   9.11
     *     308.8622  7.48
     * </pre>
     *
     * @param value     string value
     * @param arguments optional arguments
     * @return big integer
     */
    @Override
    public BigInteger read(String value, Object... arguments) {
        try {
            long number = Long.parseLong(value);
            return BigInteger.valueOf(number);
        } catch (NumberFormatException ignored) {
            return new BigInteger(value);
        }
    }

}
