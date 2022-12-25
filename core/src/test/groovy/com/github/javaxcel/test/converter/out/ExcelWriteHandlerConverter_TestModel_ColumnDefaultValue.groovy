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

import com.github.javaxcel.core.annotation.ExcelColumn
import com.github.javaxcel.core.converter.out.ExcelWriteHandlerConverter
import spock.lang.Subject

@Subject(ExcelWriteHandlerConverter)
@SuppressWarnings("unused")
class ExcelWriteHandlerConverter_TestModel_ColumnDefaultValue {

    @ExcelColumn(defaultValue = "true")
    boolean _boolean

    @ExcelColumn(defaultValue = "127")
    byte _byte

    @ExcelColumn(defaultValue = "32767")
    short _short

    @ExcelColumn(defaultValue = "A")
    char _char

    @ExcelColumn(defaultValue = "1048576")
    int _int

    @ExcelColumn(defaultValue = "-1073741824")
    long _long

    @ExcelColumn(defaultValue = "3.141592")
    float _float

    @ExcelColumn(defaultValue = "-2.718281828459045")
    double _double

    @ExcelColumn(defaultValue = "true")
    Boolean boolean_wrapper

    @ExcelColumn(defaultValue = "-128")
    Byte byte_wrapper

    @ExcelColumn(defaultValue = "-32768")
    Short short_wrapper

    @ExcelColumn(defaultValue = "B")
    Character char_wrapper

    @ExcelColumn(defaultValue = "-1048576")
    Integer int_wrapper

    @ExcelColumn(defaultValue = "1073741824")
    Long long_wrapper

    @ExcelColumn(defaultValue = "-3.141592")
    Float float_wrapper

    @ExcelColumn(defaultValue = "2.718281828459045")
    Double double_wrapper

    @ExcelColumn(defaultValue = "[1]")
    String string

    @ExcelColumn(defaultValue = "en_US")
    Locale locale

    @ExcelColumn(defaultValue = "[]")
    Object[] objects

    @ExcelColumn(defaultValue = "[A, B, C]")
    char[] chars

    @ExcelColumn(defaultValue = "[alpha, beta]")
    List<String> strings

    @ExcelColumn(defaultValue = "[[en_US], [ko_KR]]")
    List<List<Locale>> locales

}
