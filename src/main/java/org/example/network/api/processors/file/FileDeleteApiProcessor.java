package org.example.network.api.processors.file;

import org.example.files.SavesHandler;

public class FileDeleteApiProcessor extends FileApiProcessor {
    public FileDeleteApiProcessor() {
        super("delete", SavesHandler.getInstance()::deleteData);
    }
}
