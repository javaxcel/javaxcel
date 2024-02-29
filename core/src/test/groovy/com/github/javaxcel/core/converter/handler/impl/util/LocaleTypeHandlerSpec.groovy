/*
 * Copyright 2024 Javaxcel
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

package com.github.javaxcel.core.converter.handler.impl.util

import spock.lang.Specification
import spock.lang.Subject

@Subject(LocaleTypeHandler)
class LocaleTypeHandlerSpec extends Specification {

    def "read"() {
        given:
        def handler = new LocaleTypeHandler()

        when:
        def readValue = handler.read(origin)

        then:
        readValue == expected

        where:
        origin            || expected
        ""                || new Locale("")
        "en"              || new Locale("en")
        "ninewords"       || new Locale("ninewords")
        "no.1"            || new Locale("no.1")
        "en_US"           || new Locale("en", "US")
        "01_US"           || new Locale("01", "US")
        "en_01"           || new Locale("en", "01")
        "01_02"           || new Locale("01", "02")
        "en_US_beta"      || new Locale("en", "US", "beta")
        "01_US_alpha"     || new Locale("01", "US", "alpha")
        "en_01_alpha"     || new Locale("en", "01", "alpha")
        "en_US_ninewords" || new Locale("en", "US", "ninewords")
        "01_02_alpha"     || new Locale("01", "02", "alpha")
        "01_US_02"        || new Locale("01", "US", "02")
        "en_01_02"        || new Locale("en", "01", "02")
        "01_02_03"        || new Locale("01", "02", "03")
    }

}
