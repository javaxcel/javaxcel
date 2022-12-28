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
     *     TOTAL = 6789.4045 ms
     *     AVERAGE = 678.94045 ms
     *     ---------------
     *     ms        %
     *     ---------------
     *     962.0761  14.17
     *     757.3948  11.16
     *     675.5414  9.95
     *     786.0683  11.58
     *     617.8743  9.10
     *     649.5789  9.57
     *     563.056   8.29
     *     624.8379  9.20
     *     603.3569  8.89
     *     549.6199  8.10
     *
     *     ---------------
     *
     *     {@code BigInteger.valueOf(Long.parseLong(String))}
     *
     *     TOTAL = 4703.5069 ms
     *     AVERAGE = 470.35069 ms
     *     ---------------
     *     ms        %
     *     ---------------
     *     637.7051  13.56
     *     977.6699  20.79
     *     393.7896  8.37
     *     455.6069  9.69
     *     344.7192  7.33
     *     439.1477  9.34
     *     348.1275  7.40
     *     391.2891  8.32
     *     359.5352  7.64
     *     355.9167  7.57
     * </pre>
     *
     * @param value     string value
     * @param arguments optional arguments
     * @return big integer
     */
    @Override
    public BigInteger read(String value, Object... arguments) {
        long number = Long.parseLong(value);
        return BigInteger.valueOf(number);
    }

}
