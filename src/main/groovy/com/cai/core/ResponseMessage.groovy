package com.cai.core

import io.vertx.core.json.Json

class ResponseMessage<T> {

    int code

    String msg

    T body


    String toJson() {
        Json.encode(this)
    }
}
