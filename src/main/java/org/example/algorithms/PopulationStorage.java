package org.example.algorithms;

import java.io.*;
import java.util.Scanner;

public class PopulationStorage {
    private int storageId;

    public PopulationStorage(int id){
        this.storageId=id;
        File f=new File("Genetic");
        if(!f.exists())f.mkdir();
        f.deleteOnExit();
        f=new File("Genetic"+File.pathSeparator+"P"+id);
        if(!f.exists())f.mkdir();
        f.deleteOnExit();
    }

    public Scanner getChromosomeReader(int index) throws FileNotFoundException {
        File f=new File("Genetic"+File.pathSeparator+"P"+storageId,"C"+index);
        if(!f.exists()) return null;
        Scanner s=new Scanner(f);
        return s;
    }

    public PrintStream getChromosomeWriter(int index) throws IOException {
        File f=new File("Genetic"+File.pathSeparator+"P"+storageId,"C"+index);
        if(f.exists()) f.delete();
        f.createNewFile();
        f.deleteOnExit();
        return new PrintStream(f);
    }
}
