package com.cai.verticle.http

class HttpConstants {

    enum ContentType {
        JSON("application/json; charset=utf-8")

        ContentType(String value) {
            this.value = value
        }

        String value
    }

    public static String PERMISSION_TYPE = 'PERMISSION_TYPE' // 操作类型


//    static class PermissionType {
//        String
//    }
}
