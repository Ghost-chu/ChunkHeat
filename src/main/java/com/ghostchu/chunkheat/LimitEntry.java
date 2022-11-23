package com.ghostchu.chunkheat;

import java.util.concurrent.atomic.AtomicInteger;

public class LimitEntry {
    private final AtomicInteger counter = new AtomicInteger(0);

    public AtomicInteger getCounter() {
        return counter;
    }

    @Override
    public String toString() {
        return "Count=" + counter.get();
    }
}
