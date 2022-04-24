package ru.ckateptb.commons.ioc.guava.concurrent;

import java.util.concurrent.ScheduledFuture;

public interface ListenableScheduledFuture<V> extends ScheduledFuture<V>, ListenableFuture<V> {
}
