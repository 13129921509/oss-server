//package com.cai.test.eventBus
//
//import groovy.transform.CompileStatic
//import io.vertx.core.AbstractVerticle
//import io.vertx.core.Launcher
//import io.vertx.core.eventbus.EventBus
//
//@CompileStatic
//class Consumer extends AbstractVerticle {
//    static void main(String[] args) {
//        Launcher.executeCommand('run', Consumer.class.name)
//    }
//
//    @Override
//    void start() throws Exception {
//        EventBus eventBus = vertx.eventBus()
//        eventBus.consumer(EventBusTest.KEY, {msg->
//            OrdersDTO orders = msg.body() as OrdersDTO
//            println orders
//            msg.reply("receive [${orders.orders.collect {it.code}.join(',')}]")
//        })
//        println 'consumer success'
//    }
//}
