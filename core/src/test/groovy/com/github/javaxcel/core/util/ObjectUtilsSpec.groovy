package com.github.javaxcel.core.util

import spock.lang.Specification
import spock.lang.Subject

import java.nio.file.AccessMode
import java.security.cert.CRLReason
import java.util.concurrent.TimeUnit

@Subject(ObjectUtils)
class ObjectUtilsSpec extends Specification {

    def "Returns whether to be null or empty character sequence"() {
        expect:
        ObjectUtils.isNullOrEmptyCharSequence(value) == expected

        where:
        value                   | expected
        null                    | true
        ""                      | true
        new StringBuffer()      | true
        new StringBuilder()     | true
        " "                     | false
        new StringBuffer(" ")   | false
        new StringBuilder(" ")  | false
        new Object()            | false
        "null"                  | false
        0                       | false
        0L                      | false
        0.0F                    | false
        0.0D                    | false
        BigInteger.valueOf(0)   | false
        BigDecimal.valueOf(0.0) | false
        new Object[0]           | false
        new CharSequence[0]     | false
        new String[0]           | false
        new StringBuffer[0]     | false
        new StringBuilder[0]    | false
        []                      | false
        [] as Set               | false
        [:]                     | false
        (() -> { })             | false
    }

    def "Resolves the first matched object in arguments"() {
        when:
        def resolution = ObjectUtils.resolveFirst(type, arguments as Object[])

        then:
        resolution == expected

        where:
        type       | arguments                                                  || expected
        Object     | []                                                         || null
        Class      | [0, 1, 2, 3]                                               || null
        Object     | [null, 1, 2, 3]                                            || 1
        Number     | [new Object(), "alpha", 0.15, 10]                          || 0.15
        String     | [2, null, "beta", String, "gamma"]                         || "beta"
        Enum       | [AccessMode.READ, TimeUnit.DAYS, CRLReason.UNUSED]         || AccessMode.READ
        Comparable | [0, "delta", 128L, 3.14D, BigInteger.ZERO, BigDecimal.TEN] || 0
    }

    def "Resolves the last matched object in arguments"() {
        when:
        def resolution = ObjectUtils.resolveLast(type, arguments as Object[])

        then:
        resolution == expected

        where:
        type       | arguments                                                  || expected
        Object     | []                                                         || null
        Class      | [0, 1, 2, 3]                                               || null
        Object     | [null, 1, 2, 3]                                            || 3
        Number     | [new Object(), "alpha", 0.15, 10]                          || 10
        String     | [2, null, "beta", String, "gamma"]                         || "gamma"
        Enum       | [AccessMode.READ, TimeUnit.DAYS, CRLReason.UNUSED]         || CRLReason.UNUSED
        Comparable | [0, "delta", 128L, 3.14D, BigInteger.ZERO, BigDecimal.TEN] || BigDecimal.TEN
    }

}
