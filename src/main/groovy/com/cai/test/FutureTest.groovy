package com.cai.test

import groovy.transform.CompileStatic
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Launcher
import io.vertx.core.buffer.Buffer
import io.vertx.core.file.FileSystem
import io.vertx.core.file.OpenOptions

import java.nio.file.Files

@CompileStatic
class FutureTest extends AbstractVerticle{

    static void main(String[] args) {
        Launcher.executeCommand('run', FutureTest.class.name)
    }

    @Override
    void start() throws Exception {
//        String fileName = "src/main/resources/oss/uploads/z.txt"
//        FileSystem fs = vertx.fileSystem()
//        fs
//                .exists(fileName)
//                .compose { exist ->
//                    if (exist) {
//                        println "exist but delete"
//                        fs.delete(fileName)
//                    }
//                    fs
//                            .createFile(fileName)
//                            .compose {
//                                fs.writeFile(fileName, Buffer.buffer().appendString("hello,world!!!"))
//                            }
//                }
//                .onFailure(Throwable::printStackTrace)
//

        new File('/').eachFileRecurse {
            if (!it.isDirectory() && it.name.matches(/^z[0-9]?\.txt$/))
                it.delete()
        }
        // close
        vertx.close()
    }
}
