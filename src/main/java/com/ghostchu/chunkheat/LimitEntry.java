package com.ghostchu.chunkheat;

import java.util.concurrent.atomic.AtomicInteger;

public class LimitEntry {
    public LimitEntry(AtomicInteger integer, long removeLimitTime) {
        this.ainteger = integer;
        this.removeLimitTime = removeLimitTime;
    }

    public AtomicInteger getAInteger() {
        return ainteger;
    }

    public long getRemoveLimitTime() {
        return removeLimitTime;
    }

    public void setRemoveLimitTime(long removeLimitTime) {
        this.removeLimitTime = removeLimitTime;
    }

    private final AtomicInteger ainteger;
    private long removeLimitTime = 0;

    @Override
    public String toString() {
        return "Count=" + ainteger.get();
    }
}
