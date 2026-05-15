/*
 * Copyright 2026 Javaxcel
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

package com.github.javaxcel.core.internal.descriptor;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import io.github.imsejin.common.assertion.Asserts;
import io.github.imsejin.common.util.ReflectionUtils;

import com.github.javaxcel.core.annotation.ExcelColumn;
import com.github.javaxcel.core.annotation.ExcelModel;
import com.github.javaxcel.core.converter.handler.registry.ExcelTypeHandlerRegistry;
import com.github.javaxcel.core.exception.NoTargetedFieldException;
import com.github.javaxcel.core.in.strategy.ExcelReadStrategy;
import com.github.javaxcel.core.internal.analysis.ExcelAnalysis;
import com.github.javaxcel.core.internal.analysis.in.ExcelReadAnalyzer;
import com.github.javaxcel.core.internal.analysis.out.ExcelWriteAnalyzer;
import com.github.javaxcel.core.internal.converter.in.ExcelReadConverter;
import com.github.javaxcel.core.internal.converter.in.support.ExcelReadConverters;
import com.github.javaxcel.core.internal.converter.out.ExcelWriteConverter;
import com.github.javaxcel.core.internal.converter.out.support.ExcelWriteConverters;
import com.github.javaxcel.core.internal.util.FieldUtils;
import com.github.javaxcel.core.out.strategy.ExcelWriteStrategy;
import com.github.javaxcel.core.out.strategy.impl.EnumDropdown;
import com.github.javaxcel.styler.ExcelStyleConfig;
import com.github.javaxcel.styler.NoStyleConfig;

/**
 * Builds {@link ColumnDescriptor} lists for a model class.
 *
 * <p>Replaces the constructor-time + {@code prepare}-time logic that used to live
 * inside {@code ModelReader} and {@code ModelWriter}: field discovery, analysis,
 * converter wiring, and (for write) the {@link ExcelModel}/{@link ExcelColumn}
 * style + enum-dropdown fallback.
 *
 * @since 0.x
 */
public final class ModelDescriptorFactory {

    private static final ExcelStyleConfig DEFAULT_STYLE_CONFIG = new NoStyleConfig();

    private ModelDescriptorFactory() {
    }

    /**
     * Builds descriptors for the read direction. Returned descriptors throw
     * {@link UnsupportedOperationException} on {@code writeValue} calls.
     */
    public static <T> List<ColumnDescriptor<T>> forRead(
            Class<T> type,
            ExcelTypeHandlerRegistry registry,
            Map<Class<? extends ExcelReadStrategy>, ExcelReadStrategy> strategies
    ) {
        Asserts.that(type)
                .describedAs("ModelDescriptorFactory.type is not allowed to be null")
                .isNotNull();
        Asserts.that(registry)
                .describedAs("ModelDescriptorFactory.registry is not allowed to be null")
                .isNotNull();

        List<Field> fields = resolveFields(type);

        ExcelReadAnalyzer analyzer = new ExcelReadAnalyzer(registry);
        Object[] arguments = strategies.values().toArray();
        List<ExcelAnalysis> analyses = analyzer.analyze(fields, arguments);

        ExcelReadConverter converter = new ExcelReadConverters(analyses, registry);

        List<ColumnDescriptor<T>> descriptors = new ArrayList<>(fields.size());
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            ExcelAnalysis analysis = analyses.get(i);

            // Read direction ignores @ExcelColumn.name() because converters key by Field#getName().
            String headerName = field.getName();

            descriptors.add(new FieldColumnDescriptor<>(
                    field,
                    headerName,
                    converter,
                    null,
                    null,
                    null,
                    analysis.getValidators(),
                    null,
                    analysis));
        }

        return Collections.unmodifiableList(descriptors);
    }

    /**
     * Builds descriptors for the write direction. Returned descriptors throw
     * {@link UnsupportedOperationException} on {@code readValue} calls.
     */
    public static <T> List<ColumnDescriptor<T>> forWrite(
            Class<T> type,
            ExcelTypeHandlerRegistry registry,
            Map<Class<? extends ExcelWriteStrategy>, ExcelWriteStrategy> strategies
    ) {
        Asserts.that(type)
                .describedAs("ModelDescriptorFactory.type is not allowed to be null")
                .isNotNull();
        Asserts.that(registry)
                .describedAs("ModelDescriptorFactory.registry is not allowed to be null")
                .isNotNull();

        List<Field> fields = resolveFields(type);

        ExcelWriteAnalyzer analyzer = new ExcelWriteAnalyzer(registry);
        Object[] arguments = strategies.values().toArray();
        List<ExcelAnalysis> analyses = analyzer.analyze(fields, arguments);

        ExcelWriteConverter converter = new ExcelWriteConverters(analyses, registry);

        ExcelModel modelAnnotation = type.getAnnotation(ExcelModel.class);
        boolean enumDropdownEnabled = strategies.containsKey(EnumDropdown.class)
                || (modelAnnotation != null && modelAnnotation.enumDropdown());

        // Instantiate model-level styles once so all descriptors share the same instance
        // (downstream identity-based CellStyle cache then collapses them into a single
        // workbook CellStyle, preserving the legacy ModelWriter behavior).
        ExcelStyleConfig sharedModelHeaderStyle = instantiateModelStyle(modelAnnotation, true);
        ExcelStyleConfig sharedModelBodyStyle = instantiateModelStyle(modelAnnotation, false);

        List<ColumnDescriptor<T>> descriptors = new ArrayList<>(fields.size());
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            ExcelAnalysis analysis = analyses.get(i);
            ExcelColumn columnAnnotation = field.getAnnotation(ExcelColumn.class);

            String headerName = FieldUtils.toHeaderName(field, false);
            ExcelStyleConfig headerStyle = resolveHeaderStyle(columnAnnotation, sharedModelHeaderStyle);
            ExcelStyleConfig bodyStyle = resolveBodyStyle(columnAnnotation, sharedModelBodyStyle);

            // Sticky enum-dropdown enablement preserves the historical
            // ModelWriter.resolveEnumDropdown(...) accumulator behavior.
            String[] dropdownItems = null;
            if (field.getType().isEnum()) {
                if (columnAnnotation != null && columnAnnotation.enumDropdown()) {
                    enumDropdownEnabled = true;
                }
                if (enumDropdownEnabled) {
                    dropdownItems = resolveDropdownItems(field, columnAnnotation);
                }
            }

            descriptors.add(new FieldColumnDescriptor<T>(
                    field,
                    headerName,
                    null,
                    converter,
                    headerStyle,
                    bodyStyle,
                    null,
                    dropdownItems,
                    analysis));
        }

        return Collections.unmodifiableList(descriptors);
    }

    private static List<Field> resolveFields(Class<?> type) {
        List<Field> fields = FieldUtils.getTargetedFields(type);
        Asserts.that(fields)
                .describedAs("ModelDescriptorFactory cannot find the targeted fields in the class: {0}",
                        type.getName())
                .thrownBy(desc -> new NoTargetedFieldException(type, desc))
                .isNotEmpty()
                .describedAs("ModelDescriptorFactory.fields cannot have null element: {0}", fields)
                .doesNotContainNull();

        // To prevent exception from occurring on multi-threaded environment,
        // permits access to fields that are not accessible.
        fields.forEach(AccessibleObject::trySetAccessible);

        return Collections.unmodifiableList(fields);
    }

    @Nullable
    private static ExcelStyleConfig instantiateModelStyle(@Nullable ExcelModel modelAnnotation, boolean header) {
        if (modelAnnotation == null) {
            return null;
        }
        Class<? extends ExcelStyleConfig> styleClass = header ? modelAnnotation.headerStyle() : modelAnnotation.bodyStyle();
        return styleClass == NoStyleConfig.class ? DEFAULT_STYLE_CONFIG : ReflectionUtils.instantiate(styleClass);
    }

    @Nullable
    private static ExcelStyleConfig resolveHeaderStyle(
            @Nullable ExcelColumn columnAnnotation, @Nullable ExcelStyleConfig sharedModelHeaderStyle) {
        if (columnAnnotation != null && columnAnnotation.headerStyle() != NoStyleConfig.class) {
            return ReflectionUtils.instantiate(columnAnnotation.headerStyle());
        }
        return sharedModelHeaderStyle;
    }

    @Nullable
    private static ExcelStyleConfig resolveBodyStyle(
            @Nullable ExcelColumn columnAnnotation, @Nullable ExcelStyleConfig sharedModelBodyStyle) {
        if (columnAnnotation != null && columnAnnotation.bodyStyle() != NoStyleConfig.class) {
            return ReflectionUtils.instantiate(columnAnnotation.bodyStyle());
        }
        return sharedModelBodyStyle;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static String[] resolveDropdownItems(Field field, @Nullable ExcelColumn columnAnnotation) {
        if (columnAnnotation != null && columnAnnotation.dropdownItems().length > 0) {
            return columnAnnotation.dropdownItems();
        }
        Class<? extends Enum> enumType = (Class<? extends Enum>) field.getType();
        return (String[]) EnumSet.allOf(enumType).stream()
                .map(e -> ((Enum) e).name())
                .toArray(String[]::new);
    }

}
