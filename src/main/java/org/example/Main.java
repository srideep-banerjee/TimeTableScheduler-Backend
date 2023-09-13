package org.example;

import org.example.dao.SubjectDao;
import org.example.dao.TeacherDao;
import org.example.network.ApiActionHelper;
import org.example.network.LocalServer;
import org.example.pojo.Subject;
import org.example.pojo.Teacher;

import java.awt.Desktop;
import java.net.URI;
import java.util.*;

public class Main {
    static LocalServer ls;
    public static void main(String[] args) {
        ls=new LocalServer();

        ApiActionHelper aah= ApiActionHelper.getInstance();

        aah.setAction("shutdown",()->{
            ls.stop();
            System.exit(0);
        });

        aah.setAction("reset",()->{
            SubjectDao.getInstance().clear();
            TeacherDao.getInstance().clear();
        });

        Teacher t=new Teacher(new HashSet<>(),new HashSet<>(Arrays.asList("ESC501","ESC591")));
        TeacherDao.getInstance().put("SAR",t);
        t=new Teacher(new HashSet<>(),new HashSet<>(Arrays.asList("PCC-CS501")));
        TeacherDao.getInstance().put("AC",t);
        t=new Teacher(new HashSet<>(),new HashSet<>(Arrays.asList("PCC-CS502")));
        TeacherDao.getInstance().put("DG",t);
        t=new Teacher(new HashSet<>(),new HashSet<>(Arrays.asList("PCC-CS503")));
        TeacherDao.getInstance().put("SKB",t);
        t=new Teacher(new HashSet<>(),new HashSet<>(Arrays.asList("PEC-ITB","ESC591")));
        TeacherDao.getInstance().put("PC",t);
        t=new Teacher(new HashSet<>(),new HashSet<>(Arrays.asList("MC501")));
        TeacherDao.getInstance().put("SC",t);
        t=new Teacher(new HashSet<>(),new HashSet<>(Arrays.asList("HSMC-501")));
        TeacherDao.getInstance().put("RG",t);

        Subject s=new Subject(5,4,false,"LH123");
        SubjectDao.getInstance().put("ESC501",s);
        s=new Subject(5,3,true,"LH123");
        SubjectDao.getInstance().put("ESC591",s);
        s=new Subject(5,4,false,"LH123");
        SubjectDao.getInstance().put("PCC-CS502",s);
        s=new Subject(5,3,false,"LH123");
        SubjectDao.getInstance().put("PCC-CS501",s);
        s=new Subject(5,3,false,"LH123");
        SubjectDao.getInstance().put("PCC-CS503",s);
        s=new Subject(5,4,false,"LH123");
        SubjectDao.getInstance().put("PEC-ITB",s);
        s=new Subject(5,2,false,"LH123");
        SubjectDao.getInstance().put("MC501",s);
        s=new Subject(5,3,false,"LH123");
        SubjectDao.getInstance().put("HSMC-501",s);

        try {
            Desktop.getDesktop().browse(new URI("http://localhost:"+ls.getPort()+"/"));
        } catch (Exception e) {
            System.out.println(e);
        }
        while(true);
    }
}