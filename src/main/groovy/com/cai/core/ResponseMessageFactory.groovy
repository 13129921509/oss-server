package com.cai.core

import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject

class ResponseMessageFactory {

    static int SUCCESS_CODE = 0
    static int ERROR_CODE = 1

    static <T> ResponseMessage success(String msg = 'success', T body) {
        ResponseMessage<T> rsp = new ResponseMessage<>()
        rsp.msg = msg
        rsp.body = body
        rsp.code = SUCCESS_CODE
        rsp
    }

    static ResponseMessage success(String msg = 'success') {
        return success(msg, null)
    }


    static <T> ResponseMessage error(String msg = 'error', T body) {
        ResponseMessage<T> rsp = new ResponseMessage<>()
        rsp.msg = msg
        rsp.body = body
        rsp.code = ERROR_CODE
        rsp
    }

    static ResponseMessage error(String msg) {
        return error(msg, null)
    }

}
