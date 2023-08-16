package org.example;

import java.awt.*;
import java.net.URI;

public class Main {
    public static void main(String[] args) {
        LocalServer ls=new LocalServer();
        try {
            Desktop.getDesktop().browse(new URI("http://localhost:8080/"));
        } catch (Exception e) {
            System.out.println(e);
        }
        while(ls.running);
    }
}