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

package com.github.javaxcel.benchmark.converter.handler.impl.math;

import com.github.javaxcel.core.converter.handler.impl.math.BigIntegerTypeHandler;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class BigIntegerTypeHandlerBenchmark {

    private final Random random = new Random();
    private final BigIntegerTypeHandler handler = new BigIntegerTypeHandler();
    private String value;

    @Setup(Level.Invocation)
    public void initialize_LongString() {
        this.value = String.valueOf(random.nextLong());
    }

//    @Setup(Level.Invocation)
//    public void initialize_BigIntegerString() {
//        long n1 = random.nextLong();
//        long n2 = random.nextLong();
//        if (n2 < 0) {
//            String s2 = String.valueOf(n2).substring(1);
//            this.value = n1 + s2;
//        } else {
//            this.value = String.valueOf(n1) + n2;
//        }
//    }

    @Benchmark
    public void target_newBigInteger(Blackhole blackhole) {
        BigInteger bigInteger = new BigInteger(this.value);
        blackhole.consume(bigInteger);
    }

    @Benchmark
    public void target_parseLongAndValueOfBigInteger(Blackhole blackhole) {
        BigInteger bigInteger = handler.read(this.value);
        blackhole.consume(bigInteger);
    }

    @Test
    void test() throws RunnerException {
        Options options = new OptionsBuilder()
                .include(getClass().getSimpleName())
                .warmupIterations(2)
                .measurementIterations(5)
                .forks(1)
                .build();

        new Runner(options).run();
    }

}
