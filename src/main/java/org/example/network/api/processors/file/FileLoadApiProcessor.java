package org.example.network.api.processors.file;

import org.example.files.SavesHandler;

public class FileLoadApiProcessor extends FileApiProcessor {
    public FileLoadApiProcessor() {
        super("load", SavesHandler.getInstance()::loadData);
    }
}
