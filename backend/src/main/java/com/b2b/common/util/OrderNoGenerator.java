package com.b2b.common.util;

import java.util.concurrent.ThreadLocalRandom;

public final class OrderNoGenerator {

    private OrderNoGenerator() {}

    public static String next() {
        return "O"
                + System.currentTimeMillis()
                + ThreadLocalRandom.current().nextInt(1000, 9999);
    }
}
