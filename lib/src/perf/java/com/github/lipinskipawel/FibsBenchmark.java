package com.github.lipinskipawel;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import static com.github.lipinskipawel.Fibs.dynamicFib;
import static com.github.lipinskipawel.Fibs.fib;

@State(Scope.Benchmark)
public class FibsBenchmark {

    @Benchmark
    public void fibClassic(Blackhole bh) {
        bh.consume(fib(30));
    }

    @Benchmark
    public void fibDynamicRec(Blackhole bh) {
        bh.consume(dynamicFib(30));
    }
}
