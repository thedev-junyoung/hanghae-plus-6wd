package kr.hhplus.be.server.common.lock;

import java.util.concurrent.Callable;

public interface DistributedLockExecutor {
    <T> T execute(String key, Callable<T> action);
}
