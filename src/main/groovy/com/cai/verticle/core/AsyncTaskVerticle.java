package com.cai.verticle.core;

import com.cai.verticle.HttpServerVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.*;

public class AsyncTaskVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(AsyncTaskVerticle.class);

    ExecutorService executor;

    Map<String, String> result = new HashMap<>();

    Map<String, Queue<byte[]>> file2bytesQueue = new HashMap<>();

    Map<String, Boolean> file2running = new HashMap<>();


    @Override
    public void start() throws Exception {
        executor = Executors.newFixedThreadPool(100);

        EventBus eventBus = vertx.eventBus();

        eventBus.<byte[]>consumer("async.upload", msg->{
            String dir = msg.headers().get("dir");
            String name = msg.headers().get("name");
            boolean isClose = msg.headers().contains("closeFlag") && Boolean.parseBoolean(msg.headers().get("closeFlag"));
            if (isClose) {

            }
            if (file2bytesQueue.containsKey(name)) {
                file2bytesQueue.get(name).add(msg.body());
            } else {
                file2bytesQueue.put(name, new ConcurrentLinkedQueue<>());
                file2bytesQueue.get(name).add(msg.body());
                file2running.put(name, true);
                executor.submit(()->{
                    try(
                            FileChannel channel = new FileInputStream(dir + "/" + name).getChannel();
                    ){
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        while (true) {
                            byte[] bytes = file2bytesQueue.get(name).poll();
                            if (bytes != null) {
                                try {
                                    buffer.put(bytes);
                                    buffer.flip();
                                    channel.write(buffer);
                                    buffer.clear();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            if (!file2running.get(name) && file2bytesQueue.get(name).isEmpty()) {
                                break;
                            }
                        }
                        file2running.remove(name);
                        file2bytesQueue.remove(name);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }

        });
    }


}
