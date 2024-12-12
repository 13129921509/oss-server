package com.cai.bootstrap


import io.vertx.core.AbstractVerticle
import io.vertx.core.Launcher
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler

class Bootstrap extends AbstractVerticle{
    static void main(String[] args) {
        // 初始化核心类
//        Core.instance
//                .setMappingPath("com/cai/mapping/")
//                .init()
//        // 自定义launcher
//        Launcher launcher = new com.cai.launcher.Launcher()
//        launcher.dispatch(['run',Bootstrap.class.name])
        Launcher.executeCommand('run', Bootstrap.class.name)
    }

    @Override
    void start() throws Exception {
        Router router = Router.router(vertx)

        router.route().handler(StaticHandler.create("client"))
        router.post("/upload").handler {ctx->

        }
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(9000)

    }
}
