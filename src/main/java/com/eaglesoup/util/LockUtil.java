package com.eaglesoup.util;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LockUtil {
    public static final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    public static ReentrantReadWriteLock getInstance() {
        return rwl;
    }
}
