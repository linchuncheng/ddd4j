package com.ddd4j.cloud.kit.lang;

import com.ddd4j.cloud.core.contract.exception.ServiceException;
import com.ddd4j.cloud.kit.cache.RedisKit;
import lombok.experimental.UtilityClass;

import java.util.function.Consumer;

/**
 * @author sky
 * <p>
 * 分布式锁工具类
 * 使用说明：
 * 1. 获取锁：boolean locked = DistributedLock.tryLock("order:123", "driver_456", 30);
 * 2. 释放锁：DistributedLock.releaseLock("order:123", "driver_456");
 */
/**
 * 分布式锁工具类
 * 使用说明：
 * 1. 获取锁：boolean locked = DistributedLock.tryLock("order:123", "driver_456", 30);
 * 2. 释放锁：DistributedLock.releaseLock("order:123", "driver_456");
 */

/**
 * 分布式锁工具类（完全静态方法）
 */
@UtilityClass
public class DistributedLock {
    public final String LOCK_PREFIX = "lock:";
    public final long DEFAULT_EXPIRE_SECONDS = 30;
    public final long DEFAULT_WAIT_MILLIS = 3000;
    public final long SLEEP_MILLIS = 100;

    // 修改为直接使用类名调用
    public String lockValue(String id) {
        return String.format("%s:%d:%d", id, Thread.currentThread().getId(), System.currentTimeMillis());
    }

    public boolean tryLock(String bizKey, String lockValue) {
        return tryLock(bizKey, lockValue, DEFAULT_EXPIRE_SECONDS);
    }

    public boolean tryLock(String businessKey, String lockValue, long expireSeconds) {
        String lockKey = LOCK_PREFIX + businessKey;
        return RedisKit.setIfAbsent(lockKey, lockValue, expireSeconds);
    }

    public boolean tryLock(String businessKey, String lockValue, long expireSeconds, long waitMillis) throws InterruptedException {
        long endTime = System.currentTimeMillis() + waitMillis;
        while (System.currentTimeMillis() < endTime) {
            if (tryLock(businessKey, lockValue, expireSeconds)) {
                return true;
            }
            Thread.sleep(SLEEP_MILLIS);
        }
        return false;
    }

    public boolean releaseLock(String bizKey, String lockValue) {
        String lockKey = LOCK_PREFIX + bizKey;
        String storedValue = RedisKit.get(lockKey);

        if (lockValue.equals(storedValue)) {
            return RedisKit.delete(lockKey) > 0;
        }
        return false;
    }

    public void sync(String bizKey, long expireSeconds, long waitMillis, Consumer<Boolean> gotLock) {
        String lockValue = lockValue(bizKey);
        boolean locked = false;
        try {
            locked = tryLock(bizKey, lockValue, expireSeconds, waitMillis);
            gotLock.accept(locked);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException("系统繁忙，请稍后再试");
        } finally {
            if (locked) {
                releaseLock(bizKey, lockValue);
            }
        }

    }
}