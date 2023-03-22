package com.github.javaxcel.core.in.core

import spock.lang.Specification
import spock.lang.Subject

import org.apache.poi.ss.usermodel.Workbook

import com.github.javaxcel.core.in.context.ExcelReadContext
import com.github.javaxcel.core.in.core.impl.ModelReader
import com.github.javaxcel.core.in.strategy.impl.Limit
import com.github.javaxcel.core.in.strategy.impl.Parallel
import com.github.javaxcel.core.in.strategy.impl.UseSetters

@Subject(AbstractExcelReader)
class AbstractExcelReaderSpec extends Specification {

    def "Sets no option"() {
        given:
        def context = new ExcelReadContext<>(Mock(Workbook), Object, ModelReader)
        def reader = Spy(AbstractExcelReader, constructorArgs: [context]) as AbstractExcelReader

        when:
        reader.options()

        then:
        context.strategyMap.isEmpty()
    }

    def "Sets options"() {
        given:
        def context = new ExcelReadContext<>(Mock(Workbook), Object, ModelReader)
        def reader = Spy(AbstractExcelReader, constructorArgs: [context]) as AbstractExcelReader

        when:
        reader.options(new Limit(10), new Parallel(), new UseSetters())

        then:
        context.strategyMap.size() == 3
    }

    def "Sets one of duplicated options"() {
        given:
        def context = new ExcelReadContext<>(Mock(Workbook), Object, ModelReader)
        def reader = Spy(AbstractExcelReader, constructorArgs: [context]) as AbstractExcelReader

        when:
        reader.options(new Parallel(), new Limit(5), new Parallel(), new Limit(10))

        then:
        context.strategyMap.size() == 2
    }

    def "Discards previous options"() {
        given:
        def context = new ExcelReadContext<>(Mock(Workbook), Object, ModelReader)
        def reader = Spy(AbstractExcelReader, constructorArgs: [context]) as AbstractExcelReader

        when:
        reader.options(new Parallel(), new Limit(5), new UseSetters(), new Limit(10))

        then:
        context.strategyMap.size() == 3

        when:
        reader.options(new UseSetters())

        then:
        context.strategyMap.size() == 1
    }

}
