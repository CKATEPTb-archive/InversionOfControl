package ru.ckateptb.commons.ioc.guava.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public interface ListeningExecutorService extends ExecutorService {
    <T> ListenableFuture<T> submit(Callable<T> var1);

    ListenableFuture<?> submit(Runnable var1);

    <T> ListenableFuture<T> submit(Runnable var1, T var2);
}
