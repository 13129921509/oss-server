package com.cai.verticle;

import com.cai.core.ResponseMessageFactory;
import com.cai.utils.FileWatcher;
import com.cai.verticle.http.HttpConstants;
import com.cai.verticle.http.interceptor.PermissionInterceptor;
import com.google.common.base.Strings;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Timer;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.file.FileSystemOptions;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class HttpServerVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(HttpServerVerticle.class);

    private int port = 8000;

    private String dirPath = "src/main/resources/oss/uploads";

    private String tmpPath = "src/main/resources/oss/tmp";

    private String appLogDir = "./log";

    private FileWatcher fileWatcher;

    private HttpServer httpServer;


    @Override
    public void start() throws Exception {
        port = config().getInteger("port", port);
        dirPath = config().getString("dirPath", dirPath);
        fileWatcher = new FileWatcher(dirPath);
//        HttpServerOptions options = HttpServerOptions.
        PermissionInterceptor permissionInterceptor = new PermissionInterceptor(); // 权限拦截器
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create().setBodyLimit(-1).setUploadsDirectory(tmpPath).setDeleteUploadedFilesOnEnd(true));
        router.get("/find/:partFileName").handler(permissionInterceptor).handler(HttpServerVerticle.this::findFileName);
        router.get("/download/:fileName").handler(permissionInterceptor).handler(HttpServerVerticle.this::downloadFile);
        router.put("/upload").handler(BodyHandler.create()).handler(permissionInterceptor).handler(this::upload);
        router.delete("/delete").handler(permissionInterceptor).handler(this::deleteFile);
        router.get("/showLog").handler(permissionInterceptor).handler(this::showLog);
        router.put("/asyncUpload").handler(permissionInterceptor).handler(this::asyncUpload);

        httpServer = vertx.createHttpServer(new HttpServerOptions().setIdleTimeout(300).setReadIdleTimeout(300).setWriteIdleTimeout(300));
        httpServer.requestHandler(router)
                .listen(port).onSuccess((HttpServer httpServer) ->{
                    logger.info("HttpServerVertical listening on port {}", port);
                })
                .onFailure((Throwable throwable)->{
                    logger.error("HttpServerVertical listening on port {}", port, throwable);
                });
    }


    private void findFileName(RoutingContext context) {


        String partFileName = context.request().getParam("partFileName");
        HttpServerResponse response = context.response();
        if (Strings.isNullOrEmpty(partFileName)) {
            response.setStatusCode(500)
                    .putHeader("Content-Type", "application/json; charset=utf-8")
                    .end(Json.encode(ResponseMessageFactory.error("不允许为空")));
        } else {

            response.setStatusCode(200)
                    .putHeader("Content-Type", "application/json; charset=utf-8")
                    .end(Json.encode(ResponseMessageFactory.success(null,fileWatcher.findFileName(partFileName))));
        }
    }

    private void downloadFile(RoutingContext context) {
        String fileName = context.request().getParam("fileName");
        HttpServerResponse response = context.response();
        String path = dirPath + "/" + fileName;
        vertx.fileSystem().exists(dirPath + "/" + fileName)
                        .onComplete((exist)->{
                            if (exist.result()) {
                                response.putHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"")
//                                        .putHeader("content-length", "application/octet-stream")
                                        .sendFile(dirPath + "/" + fileName, (result)->{
                                    if (result.cause() != null) {
                                        logger.error(result.cause().getMessage());
                                    } else {
                                        logger.info("download success -> {}", dirPath + "/" + fileName);
                                    }
                            });
                            } else {
                                response.putHeader("Content-Type", HttpConstants.ContentType.JSON.getValue()).end(ResponseMessageFactory.error("未找到文件").toJson());
                            }
                        });
    }

    private void upload(RoutingContext context) {
        List<String> uploadFailedName = new ArrayList<>();
        List<Future> futures = context.fileUploads().stream().map(upload -> {
            return vertx.fileSystem().copy(upload.uploadedFileName(), dirPath + "/" + upload.name()).onComplete(r->{
                if (r.cause() != null) {
                    logger.error(r.cause().getMessage());
                    uploadFailedName.add(upload.name());
                } else {
                    logger.info("upload success -> {}", upload.name());
                }
            });
        }).collect(Collectors.toList());
        futures.forEach(Future::await);
        if (uploadFailedName.isEmpty())
            context.response().putHeader("Content-Type", "application/json; charset=utf-8").end(ResponseMessageFactory.success().toJson());
        else
            context.response().putHeader("Content-Type", "application/json; charset=utf-8").end(ResponseMessageFactory.error("failed upload" + uploadFailedName.toString()).toJson());

    }

    private void deleteFile(RoutingContext context) {
        HttpServerResponse response = context.response();
        context.body().asJsonObject().getJsonArray("items", new JsonArray())
                .iterator().forEachRemaining(v->{
                    String fileName = v.toString();
                    vertx.fileSystem().delete(dirPath + "/" + fileName).onSuccess(m->{
                        logger.info("delete success -> {}", dirPath + "/" + fileName);
                    });
                });
        response.putHeader("Content-Type", "application/json; charset=utf-8").end(ResponseMessageFactory.success().toJson());
    }


    private void showLog(RoutingContext context) {
        HttpServerResponse response = context.response();
        vertx.fileSystem().readFile(appLogDir + "/app.log")
                .onSuccess(buf->{ response.setChunked(true).end(buf.toString()); })
                .onFailure(err->{ response.setChunked(true).end(err.toString()); });

    }

    @Deprecated
    private void asyncUpload(RoutingContext context) {
        JsonArray codes = new JsonArray();
        context.request().setExpectMultipart(true);
        context.request().uploadHandler((upload)->{
            upload.handler(buf->{
                vertx.eventBus().<String>request("async.upload",  buf, new DeliveryOptions().addHeader("dir", dirPath).addHeader("name", upload.name()));
            });
        }).endHandler(v->{
            context.response().putHeader("Content-Type", "application/json; charset=utf-8").end(new JsonObject().put("codes", codes).toBuffer());
        });

    }


    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        super.stop(stopPromise);

        httpServer.close();
    }

}



