package com.cai.service

import com.cai.verticle.MainVerticle
import io.vertx.mysqlclient.MySQLPool

class UserPermissionService {

    MySQLPool jdbc = MainVerticle.jdbc()


}
