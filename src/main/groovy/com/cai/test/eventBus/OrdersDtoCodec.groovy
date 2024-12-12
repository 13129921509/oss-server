//package com.cai.test.eventBus
//
//import io.vertx.core.buffer.Buffer
//import io.vertx.core.eventbus.MessageCodec
//
//import java.util.regex.Matcher
//import java.util.regex.Pattern
//
///**
// * code:a1,id:1|code:a2,id:2|
// */
//class OrdersDtoCodec implements MessageCodec<OrdersDTO,OrdersDTO> {
//
//    @Override
//    void encodeToWire(Buffer buffer, OrdersDTO orderDTOs) {
//        String sendStr = orderDTOs.orders.collect({'code:'+ it.code + 'id:'+ it.id +'|'}).join('')
//        buffer.appendString(sendStr)
//    }
//
//    @Override
//    OrdersDTO decodeFromWire(int i, Buffer buffer) {
//        return parse(buffer.getString(0,buffer.length()))
//    }
//
//    @Override
//    OrdersDTO transform(OrdersDTO orderDTOS) {
//        return orderDTOS
//    }
//
//    @Override
//    String name() {
//        return this.class.simpleName
//    }
//
//    @Override
//    byte systemCodecID() {
//        return -1
//    }
//
//    OrdersDTO parse(String ordersStr) {
//        OrdersDTO ordersDTO = new OrdersDTO()
//        ordersDTO.orders = []
//        String matchReg = '(code)\\:([0-9a-zA-Z]+)\\,(id)\\:([0-9]+)\\|'
//        Pattern pattern = Pattern.compile(matchReg)
//        Matcher matcher = pattern.matcher(ordersStr)
//        if (matcher.find()){
//            ordersDTO.orders.add(new OrderDTO(
//                    matcher.group(2),
//                    matcher.group(4)
//            ))
//        }
//        return ordersDTO
//    }
//}