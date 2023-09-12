package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.algorithms.Generator;
import org.example.algorithms.PopulationStorage;
import org.example.dao.SubjectDao;
import org.example.dao.TeacherDao;
import org.example.interfaces.OnResultListener;
import org.example.network.ApiActionHelper;
import org.example.network.LocalServer;
import org.example.pojo.ScheduleSolution;
import org.example.pojo.ScheduleStructure;
import org.example.pojo.Subject;
import org.example.pojo.Teacher;

import java.awt.Desktop;
import java.io.FileNotFoundException;
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

        /*new Generator(new OnResultListener() {
            @Override
            public void onResult() {

                try {
                    new ObjectMapper().writeValueAsString(ScheduleSolution.getInstance().getData());
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String msg) {

            }
        }).generate();*/
        /*String[] subjectCodeArray={"PCC-CS503", "PCC-CS502", "PCC-CS501", "ESC501"};
        String[] teacherNameArray={"AC", "DG", "SAR", "SKB"};
        SubjectDao subjectDao=SubjectDao.getInstance();
        TeacherDao teacherDao=TeacherDao.getInstance();
        PopulationStorage populationStorage=new PopulationStorage(53);
        ScheduleStructure scheduleData=ScheduleStructure.getInstance();
        int index=0;
        int count=0;

        HashMap<String,Integer> h3=new HashMap<>();

        HashSet<String> h4=new HashSet<>();

        HashMap<String,Short> h5=new HashMap<>();

        HashSet<String> h6=new HashSet<>();

        HashSet<String> h7=new HashSet<>();

        HashMap<String, List<short[]>> h89=new HashMap<>();

        boolean[] h10=new boolean[teacherNameArray.length];

        Scanner sc= null;
        try {
            sc = populationStorage.getChromosomeReader(index);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        for(int i=0;i<14;i++){
            short i1=sc.nextShort();
            short i2=sc.nextShort();
            short i3=sc.nextShort();
            short i4=sc.nextShort();
            short day=(short)(i1/10+1);
            short period=(short)(i1%10+1);
            short semester=(short) subjectDao.get(subjectCodeArray[i4]).getSem();
            short section=(short)(i2+1);
            short teacherIndex= i3;
            short subjectIndex=i4;
            String teacher=teacherNameArray[i3];
            String subject=subjectCodeArray[i4];

            System.out.println("index: "+i);
            //evaluating h2
            if(!teacherDao.get(teacher).getFreeTime().contains(new int[]{day,period}) && !teacherDao.get(teacher).getFreeTime().isEmpty())
                count++;
            System.out.println("h2: "+count);

            //processing h3
            String key=String.format("%s,%d",subject,section);
            if(!h3.containsKey(key))h3.put(key,1);
            else h3.put(key,h3.get(key)+1);

            //evaluating h4
            if(subjectDao.get(subject).isPractical()){
                key=String.format("%d,%d,%s",day,period,subjectDao.get(subject).getRoomCode());
                if(h4.contains(key))count++;
                else h4.add(key);
                System.out.println("h4: "+count);
            }

            //evaluating h5
            else{
                key=String.format("%d,%d",section,subjectIndex);
                if(!h5.containsKey(key))h5.put(key, teacherIndex);
                else if(h5.get(key)!=teacherIndex)count++;
                System.out.println("h5: "+count);
            }

            //evaluating h6
            key=String.format("%d,%d,%d,%d",day,period,semester,section);
            if(h6.contains(key))count++;
            else h6.add(key);
            System.out.println("h6: "+count);

            //evaluating h7
            key=String.format("%d,%d,%d",teacherIndex,day,period);
            if(h7.contains(key))count++;
            else h7.add(key);
            System.out.println("h7: "+count);

            //processing h8 and h9
            if(subjectDao.get(subject).isPractical()){
                key=String.format("%d,%s",section,subject);
                if(!h89.containsKey(key))
                    h89.put(subject,new ArrayList<>());
                h89.get(key).add(new short[]{day,period,teacherIndex});
            }

            //processing h10
            h10[teacherIndex]=true;

            //evaluating h11
            if(!teacherDao.get(teacher).getSubjects().contains(subject))count++;
            System.out.println("h11: "+count);
        }

        sc.close();

        //evaluating h3
        for(String subject:subjectCodeArray){
            for(int i = 1; i<= scheduleData.getSectionCount(subjectDao.get(subject).getSem()); i++){
                if(!h3.containsKey(String.format("%s,%d",subject,i)))
                    count+=subjectDao.get(subject).getLectureCount();
                else
                    count+=Math.abs(subjectDao.get(subject).getLectureCount()-h3.get(String.format("%s,%d",subject,i)));
            }
        }
        System.out.println("h3: "+count);

        //evaluating h8 and h9
        for(String key:h89.keySet()){
            List<short[]> slots=h89.get(key);
            HashSet<Short> teachers=new HashSet<>();

            //finding count of slot with different days
            float sum=0;
            for(short[] slot:slots)sum+=slot[0];
            float mean=sum / slots.size();
            sum=0;
            for(short[] slot:slots)sum+=Math.abs(slot[0]-mean);
            count+=Math.round(sum);

            slots.sort(Comparator.comparingInt(a -> a[1]));
            for(byte i=1;i< slots.size();i++)
                if(slots.get(i)[1]-1!=slots.get(i-1)[1])count++;

            for(short[] slot:slots)teachers.add(slot[2]);
            for(Short teacherIndex:teachers){
                for(short[] slot:slots){
                    if(slot[2]==teacherIndex)continue;
                    if(h7.contains(String.format("%d,%d,%d",teacherIndex,slot[0],slot[1])))count++;
                }
            }
        }
        System.out.println("h89: "+count);

        //evaluating h10
        for(boolean b:h10)
            if(!b) count++;
        System.out.println("h10: "+count);
        System.out.println();
        System.out.println("Fitness: "+1f/(1f+count)+" Count: "+count);*/
        try {
            Desktop.getDesktop().browse(new URI("http://localhost:"+ls.getPort()+"/"));
        } catch (Exception e) {
            System.out.println(e);
        }
        while(true);
    }
}