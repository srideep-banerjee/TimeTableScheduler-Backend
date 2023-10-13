package org.example.algorithms;

import java.io.*;
import java.util.Scanner;

public class PopulationStorage {
    private boolean deleteOnExit = false;
    private int storageId;

    public PopulationStorage(int id) {
        this.storageId = id;
        File f = new File("Genetic");
        if (!f.exists()) f.mkdir();
        if(deleteOnExit) f.deleteOnExit();
        f = new File("Genetic" + File.separator + "P" + id);
        if (!f.exists()) f.mkdir();
        if(deleteOnExit) f.deleteOnExit();
    }

    public Scanner getChromosomeReader(int index) throws FileNotFoundException {
        File f = new File("Genetic" + File.separator + "P" + storageId, "C" + index + ".txt");
        if (!f.exists()) return null;
        Scanner s = new Scanner(f);
        return s;
    }

    public PrintStream getChromosomeWriter(int index) throws IOException {
        File f = new File("Genetic" + File.separator + "P" + storageId, "C" + index + ".txt");
        if (f.exists()) f.delete();
        f.createNewFile();
        if(deleteOnExit) f.deleteOnExit();
        return new PrintStream(f);
    }
}
