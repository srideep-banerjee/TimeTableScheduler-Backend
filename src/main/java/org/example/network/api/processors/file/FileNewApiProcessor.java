package org.example.network.api.processors.file;

import org.example.files.SavesHandler;

public class FileNewApiProcessor extends FileApiProcessor {
    public FileNewApiProcessor() {
        super("newEmpty", SavesHandler.getInstance()::createNewSave);
    }
}
