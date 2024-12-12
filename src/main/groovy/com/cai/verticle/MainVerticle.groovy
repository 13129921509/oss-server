package com.cai.verticle

import com.cai.verticle.core.AsyncTaskVerticle
import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.ThreadingModel
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.mysqlclient.MySQLBuilder
import io.vertx.mysqlclient.MySQLClient
import io.vertx.mysqlclient.MySQLConnectOptions
import io.vertx.mysqlclient.impl.MySQLPoolOptions
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.PoolOptions
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MainVerticle extends AbstractVerticle{

    private static Vertx vertx
    private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class)
    private static Pool jdbc

    static void main(String[] args) {
        vertx = Vertx.vertx()
        // 初始化sql服务
        initJDBC() {
            // 初始化http服务
            initHttpServer()
            // 初始化Async Task
            initAsyncTaskServer()
        }
    }

    private static void initHttpServer() {
        DeploymentOptions options = new DeploymentOptions().setInstances(1).setThreadingModel(ThreadingModel.WORKER)
        options.setConfig(JsonObject.of('port',9000))
        vertx.deployVerticle(HttpServerVerticle.class, options).onComplete(ar -> {
            if (ar.succeeded()) {
                logger.info("http-server Verticle deployed with ID: " + ar.result())
            } else{
                logger.info("Deployment failed: " + ar.cause().getMessage())
            }
        })
    }

    private static void initJDBC(Closure call) {
        vertx.deployVerticle(new AbstractVerticle() {
            @Override
            void start() throws Exception {
                jdbc = MySQLBuilder.pool {
                    it.with(new PoolOptions()
                        .setMaxSize(10)
                        .setShared(true)
                        .setName('jdbc-pool'))
                    .connectingTo(new MySQLConnectOptions()
                        .setHost('127.0.0.1')
                        .setPort(33306)
                        .setDatabase('oss')
                        .setUser('root')
                        .setPassword('test001')
                        .setReconnectAttempts(3)
                        .setReconnectInterval(1000)
                        .setCharset('utf8'))
                    .using(vertx)
                }
            }
        }).onSuccess {
            logger.info("jdbc Verticle deployed with ID: " + it)
            call()
        }.onFailure {
            throw new RuntimeException(it)
        }
    }
    private static void initAsyncTaskServer() {
        DeploymentOptions options = new DeploymentOptions().setInstances(1).setThreadingModel(ThreadingModel.WORKER)
        vertx.deployVerticle(AsyncTaskVerticle.class, options).onComplete(ar-> {
            if (ar.succeeded()) {
                logger.info("async-task Verticle deployed with ID: " + ar.result())
            } else{
                logger.info("Deployment failed: " + ar.cause().getMessage())
            }
        })
    }

    public static Pool jdbc() {
        return jdbc;
    }
}
