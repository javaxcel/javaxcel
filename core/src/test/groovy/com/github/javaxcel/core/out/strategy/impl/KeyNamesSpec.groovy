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

package com.github.javaxcel.core.out.strategy.impl

import com.github.javaxcel.core.out.context.ExcelWriteContext
import com.github.javaxcel.core.out.core.impl.MapWriter
import com.github.javaxcel.core.out.core.impl.ModelWriter
import org.apache.poi.ss.usermodel.Workbook
import spock.lang.Specification
import spock.lang.Subject

import java.util.function.Function

import static java.util.stream.Collectors.toMap

@Subject(KeyNames)
@SuppressWarnings("GroovyResultOfObjectAllocationIgnored")
class KeyNamesSpec extends Specification {

    def "test"() {
        given:
        def legalKeyNames = ["width", "depth", "height"]
        def illegalKeyNames = ["alpha", "beta", "alpha"]
        def contextMap = [
                (ModelWriter.class): new ExcelWriteContext<>(Mock(Workbook), String, ModelWriter),
                (MapWriter.class)  : new ExcelWriteContext<>(Mock(Workbook), String, MapWriter),
        ]

        when: "Create strategy with legal argument"
        def strategy = new KeyNames(legalKeyNames, legalKeyNames)

        then: "Succeed to create strategy"
        !strategy.isSupported(contextMap[ModelWriter])
        strategy.isSupported(contextMap[MapWriter])
        def orders = (0..<legalKeyNames.size()).stream().collect toMap({ legalKeyNames[it] }, Function.identity())
        strategy.execute(null) == [orders: orders, names: legalKeyNames]

        when: "Create strategy with illegal argument"
        new KeyNames(illegalKeyNames)

        then: "Failed to create strategy"
        def e0 = thrown IllegalArgumentException
        e0.message.split("\n")[0] == "keyOrders cannot have duplicated elements: $illegalKeyNames"

        when: "Create strategy with illegal arguments"
        new KeyNames(legalKeyNames, illegalKeyNames)

        then: "Failed to create strategy"
        def e1 = thrown IllegalArgumentException
        e1.message.split("\n")[0] == "newKeyNames cannot have duplicated elements: $illegalKeyNames"
    }

}
