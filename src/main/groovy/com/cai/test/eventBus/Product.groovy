//package com.cai.test.eventBus
//
//import groovy.transform.CompileStatic
//import io.vertx.core.AbstractVerticle
//import io.vertx.core.Launcher
//import io.vertx.core.eventbus.EventBus
//
//@CompileStatic
//class Product extends AbstractVerticle {
//    static void main(String[] args) {
//        Launcher.executeCommand('run', Product.class.name)
//    }
//
//    @Override
//    void start() throws Exception {
//        EventBus eventBus = vertx.eventBus()
//        List<OrderDTO> orders = [
//                new OrderDTO('a1','1'),
//                new OrderDTO('a2','2')
//        ]
//
//        eventBus.registerDefaultCodec(OrdersDTO.class, new OrdersDtoCodec())
//        eventBus.request(EventBusTest.KEY,new OrdersDTO(orders)).onComplete { res->
//            if (res.succeeded()){
//                println "Received reply: " + res.result().body()
//            } else {
//                throw res.cause()
//            }
//        }
//    }
//}