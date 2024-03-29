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

package com.github.javaxcel.test.converter.out

import spock.lang.Subject

import com.github.javaxcel.core.annotation.ExcelModel
import com.github.javaxcel.core.converter.out.ExcelWriteHandlerConverter

@Subject(ExcelWriteHandlerConverter)
@SuppressWarnings("unused")
@ExcelModel(defaultValue = "<null>")
class ExcelWriteHandlerConverter_TestModel_ModelDefaultValue {

    boolean _boolean
    byte _byte
    short _short
    char _char
    int _int
    long _long
    float _float
    double _double

    Boolean boolean_wrapper
    Byte byte_wrapper
    Short short_wrapper
    Character char_wrapper
    Integer int_wrapper
    Long long_wrapper
    Float float_wrapper
    Double double_wrapper

    String string
    Locale locale
    Object[] objects
    char[] chars
    List<String> strings
    List<List<Locale>> locales

}
