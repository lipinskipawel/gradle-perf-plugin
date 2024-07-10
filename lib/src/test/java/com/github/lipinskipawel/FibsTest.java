package com.github.lipinskipawel;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import static com.github.lipinskipawel.Fibs.dynamicFib;
import static com.github.lipinskipawel.Fibs.fib;

class FibsTest implements WithAssertions {

    @Test
    void test_fib() {
        final var result = fib(7);

        assertThat(result).isEqualTo(13);
    }

    @Test
    void test_dynamic_fib() {
        final var result = dynamicFib(7);

        assertThat(result).isEqualTo(13);
    }
}