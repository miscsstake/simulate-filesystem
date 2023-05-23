package com.eaglesoup.util;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LockUtil {
    public static final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    public static ReentrantReadWriteLock getInstance() {
        return rwl;
    }

    public static <T> T rLock(Callable<T> callable) {
        Lock readLock = getInstance().readLock();
        readLock.lock();
        T result;
        try {
            result = callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            readLock.unlock();
        }
        return result;
    }

    public static <T> T wLock(Callable<T> callable) {
        Lock writeLock = getInstance().writeLock();
        writeLock.lock();
        T result;
        try {
            result = callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            writeLock.unlock();
        }
        return result;
    }
}
