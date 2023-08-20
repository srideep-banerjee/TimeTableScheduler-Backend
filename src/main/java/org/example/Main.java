package org.example;

import org.example.dao.SubjectDao;
import org.example.dao.TeacherDao;
import org.example.network.ApiActionHelper;
import org.example.network.LocalServer;

import java.awt.Desktop;
import java.net.URI;

public class Main {
    static LocalServer ls;
    public static void main(String[] args) {
        int port=5999;
        ls=new LocalServer(port);

        ApiActionHelper aah= ApiActionHelper.getInstance();

        aah.setAction("shutdown",()->{
            ls.stop();
            System.exit(0);
        });

        aah.setAction("reset",()->{
            SubjectDao.getInstance().clear();
            TeacherDao.getInstance().clear();
        });

        try {
            Desktop.getDesktop().browse(new URI("http://localhost:"+port+"/"));
        } catch (Exception e) {
            System.out.println(e);
        }
        while(true);
    }
}