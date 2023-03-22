/*
 * Copyright 2021 Javaxcel
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

package com.github.javaxcel.core;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Workbook;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.tool.TypeClassifier;
import io.github.imsejin.common.util.MathUtils;

import com.github.javaxcel.core.annotation.ExcelIgnore;
import com.github.javaxcel.core.util.ExcelUtils;
import com.github.javaxcel.core.util.FieldUtils;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;
import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.*;

public class TestUtils {

    public static final String MAP_KEY_PREFIX = "FIELD_";

    public static final Javaxcel JAVAXCEL = Javaxcel.newInstance();

    private static final EasyRandom generator;

    private static final Class<?>[] classes = Stream.of(
                    TypeClassifier.Types.NUMBER.getClasses(),
                    TypeClassifier.Types.DATETIME.getClasses(),
                    Arrays.asList(char.class, boolean.class, Character.class, Boolean.class, String.class))
            .flatMap(Collection::stream).toArray(Class[]::new);

    static {
        EasyRandomParameters parameters =
                new EasyRandomParameters()
                        .charset(StandardCharsets.UTF_8)
                        .dateRange(LocalDate.of(1000, Month.JANUARY, 1), LocalDate.now())
                        .timeRange(LocalTime.MIN, LocalTime.MAX)
                        .stringLengthRange(1, 15) // Not allow empty string.
                        .collectionSizeRange(1, 10) // Not allow empty array or collection.
                        .excludeField(new FieldExclusionPredicate())
                        .overrideDefaultInitialization(false)
                        .scanClasspathForConcreteTypes(true);
        generator = new EasyRandom(parameters);
    }

    public static Random getRandom() {
        return generator;
    }

    public static <T> T randomize(Class<T> type) {
        return generator.nextObject(type);
    }

    public static <T> List<T> getMocks(Class<T> type, int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Size cannot be negative");
        }
        if (size == 0) {
            return Collections.emptyList();
        }

        return IntStream.range(0, size).parallel()
                .mapToObj(i -> randomize(type)).collect(toList());
    }

    public static Map<String, Object> randomMap(int numOfEntries) {
        if (numOfEntries < 0) {
            throw new IllegalArgumentException("Number of entries cannot be negative");
        }
        if (numOfEntries == 0) {
            return Collections.emptyMap();
        }

        Map<String, Object> map = new HashMap<>();

        for (int i = 0; i < numOfEntries; i++) {
            int index = generator.nextInt(classes.length);
            Object randomized = randomize(classes[index]);

            // For abundant test cases, some empty strings will be converted to null.
            if (MathUtils.isOdd(i) && "".equals(randomized)) {
                randomized = null;
            }

            String key = MAP_KEY_PREFIX + (i + 1);
            map.put(key, randomized);
        }

        return map;
    }

    public static List<Map<String, Object>> getRandomMaps(int size, int numOfEntries) {
        if (size < 0) {
            throw new IllegalArgumentException("Size cannot be negative");
        }
        if (numOfEntries < 0) {
            throw new IllegalArgumentException("Number of entries cannot be negative");
        }

        return IntStream.range(0, size).parallel()
                .mapToObj(i -> randomMap(numOfEntries)).collect(toList());
    }

    /**
     * Excludes the field depending on given percentage.
     */
    @Target(FIELD)
    @Retention(RUNTIME)
    public @interface ExcludeOnPercentage {
        double value();
    }

    private static class FieldExclusionPredicate implements Predicate<Field> {
        private final Random random = new Random();

        @Override
        public boolean test(Field field) {
            // Excludes.
            if (field.isAnnotationPresent(ExcelIgnore.class)) {
                return true;
            }

            // Includes.
            ExcludeOnPercentage annotation = field.getAnnotation(ExcludeOnPercentage.class);
            if (annotation == null) {
                return false;
            }

            // Excludes it depending on percentage.
            double percentage = annotation.value();
            Asserts.that(percentage)
                    .describedAs("ConditionalOnPercentage.value must be between 0.0 and 1.0")
                    .isBetween(0.0, 1.0);
            return percentage > this.random.nextFloat();
        }
    }

    // -------------------------------------------------------------------------------------------------

    public static void assertNotEmptyFile(File file) {
        assertNotEmptyFile(file, "File must be created and have content");
    }

    public static void assertNotEmptyFile(File file, String description, Object... args) {
        assertThat(file)
                .as(description, args)
                .isNotNull().exists().canRead().isNotEmpty();
    }

    public static void assertEqualsNumOfModels(Workbook workbook, List<?> models) {
        assertThat(ExcelUtils.getNumOfModels(workbook))
                .as("The number of actually written rows is equal to the number of models")
                .isEqualTo(models.size());
    }

    public static void assertEqualsNumOfModels(Workbook workbook, List<?> models, String description, Object... args) {
        assertThat(ExcelUtils.getNumOfModels(workbook))
                .as(description, args)
                .isEqualTo(models.size());
    }

    public static void assertEqualsHeaderSize(Workbook workbook, Class<?> type) {
        assertThat((double) FieldUtils.getTargetedFields(type).size())
                .as("Header size is equal to the number of targeted fields in '%s'", type.getSimpleName())
                .isEqualTo(ExcelUtils.getSheets(workbook).stream()
                        .mapToInt(sheet -> sheet.getRow(0).getPhysicalNumberOfCells())
                        .average().orElse(-1));
    }

    public static void assertEqualsHeaderSize(Workbook workbook, Class<?> type, String description, Object... args) {
        assertThat((double) FieldUtils.getTargetedFields(type).size())
                .as(description, args)
                .isEqualTo(ExcelUtils.getSheets(workbook).stream()
                        .mapToInt(sheet -> sheet.getRow(0).getPhysicalNumberOfCells())
                        .average().orElse(-1));
    }

}
