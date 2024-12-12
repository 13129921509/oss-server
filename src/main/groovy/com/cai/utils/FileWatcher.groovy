package com.cai.utils

import groovy.transform.CompileStatic

import java.nio.file.Files
import java.nio.file.LinkOption

@CompileStatic
class FileWatcher {

    String path
    File dir

    FileWatcher(String path) {
        this.path = path
        dir = new File(path)
        if (!Files.exists(dir.toPath()))
            throw new IllegalStateException("path不存在")

        if (!Files.isDirectory(dir.toPath()))
            throw new IllegalStateException("path不是一个路径")
    }


    List<String> findFileName(String partFileName) {
        List<String> fileNames = []
        dir.eachFile {File it->
            if (it.name.find(partFileName))
                fileNames.add(it.name)
        }
        return fileNames
    }



}
