package org.example;

import org.example.dao.TeacherDao;
import org.example.network.LocalServer;

import java.awt.*;
import java.net.URI;

public class Main {
    public static void main(String[] args) {
        int port=5999;
        LocalServer ls=new LocalServer(port);
        try {
            Desktop.getDesktop().browse(new URI("http://localhost:"+port+"/"));
        } catch (Exception e) {
            System.out.println(e);
        }
        while(true);
    }
}