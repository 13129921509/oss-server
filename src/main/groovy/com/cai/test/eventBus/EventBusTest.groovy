//package com.cai.test.eventBus
//
//import groovy.transform.CompileStatic
//import io.vertx.core.AbstractVerticle
//import io.vertx.core.Launcher
//import io.vertx.core.buffer.Buffer
//import io.vertx.core.eventbus.EventBus
//import io.vertx.core.eventbus.MessageCodec
//import org.omg.CORBA.REBIND
//
//import java.util.regex.Matcher
//import java.util.regex.Pattern
//
//@CompileStatic
//class EventBusTest extends AbstractVerticle {
//    static String KEY = "orders"
//
//    static void main(String[] args) {
//        Launcher.executeCommand('run', this.name)
//    }
//
//    @Override
//    void start() throws Exception {
//        vertx.deployVerticle(new Consumer())
//        vertx.timer(3000).onComplete {
//            vertx.deployVerticle(new Product())
//        }
//    }
//
//
//
//
//
//
//}
