package com.cai.verticle.http.interceptor

import com.cai.core.ResponseMessageFactory
import com.cai.verticle.HttpServerVerticle
import com.cai.verticle.MainVerticle
import groovy.transform.CompileStatic
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.Json
import io.vertx.ext.web.RoutingContext
import io.vertx.mysqlclient.MySQLPool
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Tuple
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.CyclicBarrier
import java.util.regex.Matcher
import java.util.regex.Pattern


/**
 * 权限检查
 */
class PermissionInterceptor implements Handler<RoutingContext>{

    private static final Logger logger = LoggerFactory.getLogger(PermissionInterceptor.class);

    Pattern resolvePermissionPatten = Pattern.compile(/^\/([^\/]+)\//)

    static List<String> PermissionType = ['DOWNLOAD','FIND','DELETE','UPLOAD']

    static Pool jdbc = MainVerticle.jdbc()

    @CompileStatic
    @Override
    void handle(RoutingContext ctx) {
        String user = ctx.request().getHeader('x-user')

        if (!user) {
            ctx.response().setStatusCode(400).putHeader("Content-Type", "application/json; charset=utf-8")
                    .end(Json.encode(ResponseMessageFactory.error("无权限")))
            return
        }

        String path = ctx.request().path()

        if (path) {
            Matcher permissionMatcher = resolvePermissionPatten.matcher(path)
            boolean b = permissionMatcher.find()
            if (!b)
                ctx.response().setStatusCode(400).putHeader("Content-Type", "application/json; charset=utf-8")
                        .end(Json.encode(ResponseMessageFactory.error("无权限")))
            else {
                String ps = permissionMatcher.group(1).toUpperCase()
                if (PermissionType.contains(ps)) {
                    jdbc.getConnection().onSuccess {conn->
                        conn
                            .prepare(/select count(1) from user_permission where user_name = ? and permission = ? limit 1/)
                            .onSuccess {statement->
                                statement.query()
                                    .execute(Tuple.of(user, ps))
                                    .onSuccess {rows->
                                        if (rows.isEmpty())
                                            ctx.response().setStatusCode(400).putHeader("Content-Type", "application/json; charset=utf-8")
                                                .end(Json.encode(ResponseMessageFactory.error("无权限")))
                                        else {
                                            if (rows[0].get(Integer,0) != 0)
                                                ctx.next()
                                            else
                                                ctx.response().setStatusCode(400).putHeader("Content-Type", "application/json; charset=utf-8")
                                                    .end(Json.encode(ResponseMessageFactory.error("无权限")))

                                        }
                                        statement.close()
                                    }
                            }
                            .onFailure {
                                it.printStackTrace()
                                ctx.response().setStatusCode(500).end()
                            }
                        conn.close()

                    }
                } else {
                    //不属于基本操作，检查是否有admin权限
                    jdbc.getConnection().onSuccess {conn->
                        conn
                            .prepare(/select count(1) from user_permission where user_name = ? and permission = ? limit 1/)
                            .onSuccess {statement->
                                statement.query().execute(Tuple.of(user,'ADMIN')){r->
                                    if (r.succeeded()) {
                                        r.result().isEmpty() ? ctx.response().setStatusCode(400).putHeader("Content-Type", "application/json; charset=utf-8")
                                            .end(Json.encode(ResponseMessageFactory.error("无权限"))) : ctx.next()
                                    }
                                }
                                statement.close()
                            }
                            .onFailure {
                                it.printStackTrace()
                                ctx.response().setStatusCode(500).end()
                            }
                        conn.close()
                    }
                }
            }

        } else
            ctx.response().setStatusCode(400).putHeader("Content-Type", "application/json; charset=utf-8")
                    .end(Json.encode(ResponseMessageFactory.error("无权限")))
    }
}
