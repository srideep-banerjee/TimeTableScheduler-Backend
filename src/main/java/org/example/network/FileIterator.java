package org.example.network;

import java.io.File;
import java.util.ArrayList;

public class FileIterator {
    public static ArrayList<String> getPathList(){
        ArrayList<String> list=new ArrayList<String>();

        File f=new File("web/styles");
        for(String s:f.list()) list.add("/styles/"+s);

        f=new File("web/scripts");
        for(String s:f.list()) if(s.endsWith(".js")) list.add("/scripts/"+s);

        f=new File("web");
        for(String s:f.list()) if(s.endsWith(".html")) list.add("/"+s);

        return list;
    }
}
