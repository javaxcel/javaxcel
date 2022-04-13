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

package com.github.javaxcel.internal.model;

import com.github.javaxcel.annotation.ExcelModelCreator;
import com.github.javaxcel.annotation.ExcelModelCreator.FieldName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.AccessMode;
import java.nio.file.Path;
import java.util.List;

public class ExcelModelCreatorTester {

    public static class PublicNoArgsConstructor {
    }

    @RequiredArgsConstructor(access = AccessLevel.PUBLIC)
    public static class PublicConstructor {
        private final Integer id;
        private final String title;
        private final URL url;
    }

    @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ProtectedConstructor {
        private final String name;
    }

    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    public static class PackagePrivateConstructor {
        private final String name;
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class PrivateConstructor {
        private final String name;
    }

    public static class ConstructorArgsWithoutOrder {
        private final long number;
        private final String name;
        private final Path path;

        public ConstructorArgsWithoutOrder(String name, Path path, long number) {
            this.number = number;
            this.name = name;
            this.path = path;
        }
    }

    public static class ParamNameDoesNotMatchFieldNameButBothTypeIsUnique {
        private final BigInteger bigInteger;
        private final BigDecimal bigDecimal;
        private Object[] arguments;

        private ParamNameDoesNotMatchFieldNameButBothTypeIsUnique(BigInteger bigInt, BigDecimal decimal) {
            this.bigInteger = bigInt;
            this.bigDecimal = decimal;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    @NoArgsConstructor(access = AccessLevel.PUBLIC)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    public static class AllConstructorsAreNotAnnotated {
        private AccessMode accessMode;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE, onConstructor_ = @ExcelModelCreator)
    @AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor_ = @ExcelModelCreator)
    public static class ConstructorsAreAnnotated {
        private Object object;
        private List<String> strings;
    }

    public static class InvalidFieldName {
        private byte[] bytes;
        private byte[] dummies;
        private char[] characters;

        public InvalidFieldName(@FieldName("") byte[] bytes, char[] characters) {
            this.bytes = bytes;
            this.characters = characters;
        }
    }

}
