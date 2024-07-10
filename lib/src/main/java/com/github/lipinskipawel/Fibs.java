package com.github.lipinskipawel;

public final class Fibs {

    public static int fib(int n) {
        if (n == 0) {
            return 0;
        }
        if (n == 1) {
            return 1;
        }
        return fib(n - 1) + fib(n - 2);
    }

    public static int dynamicFib(int n) {
        int a = 0;
        int b = 1;

        for (int i = 1; i < n; i++) {
            int temp = a;
            a = b;
            b = temp + b;
        }

        return b;
    }
}
