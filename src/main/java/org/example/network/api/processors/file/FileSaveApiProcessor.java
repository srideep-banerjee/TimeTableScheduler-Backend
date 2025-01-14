package org.example.network.api.processors.file;

import org.example.files.SavesHandler;

public class FileSaveApiProcessor extends FileApiProcessor {
    public FileSaveApiProcessor() {
        super("save", SavesHandler.getInstance()::saveData);
    }
}
