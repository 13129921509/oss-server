package com.cai.verticle.core;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;

public class AsyncTaskVerticle extends AbstractVerticle {

    WorkerExecutor executor;

    @Override
    public void start() throws Exception {
        executor = vertx.createSharedWorkerExecutor("task-worker", 100);

    }
}
