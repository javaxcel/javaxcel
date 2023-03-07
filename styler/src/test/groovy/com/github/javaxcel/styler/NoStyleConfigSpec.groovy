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

package com.github.javaxcel.styler

import spock.lang.Specification

import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Font

import com.github.javaxcel.styler.config.Configurer

class NoStyleConfigSpec extends Specification {

    def "Configures nothing"() {
        given:
        def cellStyle = Mock(CellStyle)
        def font = Mock(Font)
        def configurer = new Configurer(cellStyle, font)
        def config = new NoStyleConfig()

        when:
        config.configure(configurer)

        then:
        0 * configurer.alignment()
        0 * configurer.background()
        0 * configurer.border()
        0 * configurer.font()
    }

}
