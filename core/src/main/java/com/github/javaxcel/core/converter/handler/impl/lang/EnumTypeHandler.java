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

package com.github.javaxcel.core.converter.handler.impl.lang;

import com.github.javaxcel.core.converter.handler.AbstractExcelTypeHandler;
import com.github.javaxcel.core.util.FieldUtils;

import java.lang.reflect.Field;

/**
 * Handler for type of {@link Enum}
 *
 * @since 0.8.0
 */
@SuppressWarnings("rawtypes")
public class EnumTypeHandler extends AbstractExcelTypeHandler<Enum> {

    public EnumTypeHandler() {
        super(Enum.class);
    }

    @Override
    protected String writeInternal(Enum value, Object... arguments) {
        return value.name();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Enum read(String value, Object... arguments) {
        // Resolve field from arguments.
        Field field = FieldUtils.resolveFirst(Field.class, arguments);
        if (field == null) {
            return null;
        }

        Class<? extends Enum> enumType = (Class<? extends Enum>) field.getType();

        try {
            return Enum.valueOf(enumType, value);
        } catch (IllegalArgumentException e) {
            // When the specified enum type has no constant matched the given name.
            return null;
        }
    }

}
