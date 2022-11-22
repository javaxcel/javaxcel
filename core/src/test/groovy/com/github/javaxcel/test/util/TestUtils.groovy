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

package com.github.javaxcel.test.util

import com.github.javaxcel.core.annotation.ExcelIgnore
import com.github.javaxcel.test.annotation.ExcludeOnPercentage
import io.github.imsejin.common.assertion.Asserts
import io.github.imsejin.common.util.MathUtils
import org.jeasy.random.EasyRandom
import org.jeasy.random.EasyRandomParameters

import java.lang.reflect.Field
import java.nio.charset.StandardCharsets
import java.nio.file.AccessMode
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.MonthDay
import java.time.Period
import java.time.Year
import java.time.YearMonth
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import java.util.function.Function
import java.util.function.Predicate

final class TestUtils {

    // Groovy won't generate a property — a combination of getter and setter — for a field
    // explicitly specifying access modifier.
    private static final Class<?>[] RANDOMIZED_CLASSES = [
            boolean, byte, short, char, int, long, float, double,
            Boolean, Byte, Short, Character, Integer, Long, Float, Double,
            Object, String, BigInteger, BigDecimal,
            Date, LocalDate, LocalTime, LocalDateTime, ZonedDateTime, Instant,
            Year, YearMonth, DayOfWeek, Period, Duration,
            AccessMode, TimeUnit, Month, MonthDay,
    ].asImmutable()

    // Groovy won't generate a property — a combination of getter and setter — for a field
    // explicitly specifying access modifier.
    private static final EasyRandom GENERATOR = new EasyRandom(new EasyRandomParameters()
            .charset(StandardCharsets.UTF_8)
            .dateRange(LocalDate.of(1000, Month.JANUARY, 1), LocalDate.now())
            .timeRange(LocalTime.MIN, LocalTime.MAX)
            .stringLengthRange(0, 8) // Not allow empty string.
            .collectionSizeRange(0, 10) // Not allow empty array or collection.
            .excludeField(new FieldExclusionPredicate())
            .overrideDefaultInitialization(false)
            .scanClasspathForConcreteTypes(true))

    static <T> T randomize(Class<T> type) {
        GENERATOR.nextObject(type)
    }

    static <T> List<T> randomizeObjects(Class<T> type, int size) {
        Asserts.that(size)
                .describedAs("Size cannot be negative")
                .isZeroOrPositive()

        if (size == 0) {
            return Collections.emptyList()
        }

        (0..<size).collect { randomize(type) }
    }

    static Map<String, Object> randomizeMap(int size) {
        randomizeMap(size, { "$it" })
    }

    static Map<String, Object> randomizeMap(List<String> keys) {
        randomizeMap(keys.size(), { keys[it] })
    }

    static Map<String, Object> randomizeMap(int size, Function<Integer, String> keyFunction) {
        Asserts.that(size)
                .describedAs("Size cannot be negative")
                .isZeroOrPositive()

        if (size == 0) {
            return Collections.emptyMap()
        }

        def map = new HashMap<String, Object>()

        size.times {
            def index = GENERATOR.nextInt(RANDOMIZED_CLASSES.length)
            def randomized = randomize(RANDOMIZED_CLASSES[index] as Class)

            // For abundant test cases, some empty strings will be converted to null.
            if (MathUtils.isOdd(it) && randomized == "") {
                randomized = null
            }

            def key = keyFunction.apply(it)
            map[key] = randomized
        }

        map
    }

    // -------------------------------------------------------------------------------------------------

    private static class FieldExclusionPredicate implements Predicate<Field> {
        final Random random = new Random()

        @Override
        boolean test(Field field) {
            // Excludes.
            if (field.isAnnotationPresent(ExcelIgnore.class)) {
                return true
            }

            // Includes.
            def annotation = field.getAnnotation(ExcludeOnPercentage.class)
            if (annotation == null) {
                return false
            }

            // Excludes it depending on percentage.
            def percentage = annotation.value()
            Asserts.that(percentage)
                    .describedAs("ConditionalOnPercentage.value must be between 0.0 and 1.0")
                    .isBetween(0.0, 1.0)
            percentage > this.random.nextFloat()
        }
    }

}
