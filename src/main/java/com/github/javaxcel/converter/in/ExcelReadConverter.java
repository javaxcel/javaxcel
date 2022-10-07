/*
 * Copyright 2020 Javaxcel
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

package com.github.javaxcel.converter.in;

import java.lang.reflect.Field;
import java.util.Map;

public interface ExcelReadConverter {

    /**
     * Converts a string in cell to the type of field.
     *
     * @param variables {@link Map} in which key is the model's field name and
     *                  value is the model's field value
     * @param field     targeted field of model
     * @return value converted to the type of field
     */
    Object convert(Map<String, String> variables, Field field);

}
