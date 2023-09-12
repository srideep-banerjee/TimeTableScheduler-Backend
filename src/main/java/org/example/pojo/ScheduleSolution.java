package org.example.pojo;

import org.example.dao.SubjectDao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ScheduleSolution {

    //format: data[semester][section][day][period]=new String[]{"teacherName","subjectCode"}
    private List<List<List<List<List<String>>>>> data;
    private static ScheduleSolution instance=null;

    private ScheduleSolution(){
        this.resetData();
    }

    public static ScheduleSolution getInstance() {
        if(instance==null)instance=new ScheduleSolution();
        return instance;
    }
    public void resetData(){
        ScheduleStructure ss=ScheduleStructure.getInstance();
        data=new ArrayList<>();
        byte periodCount=ss.getPeriodCount();
        for(int i=0;i<ss.getSemesterCount();i++){
            List<List<List<List<String>>>> dataSection=new ArrayList<>();
            for(int j=0;j<ss.getSectionCount(i*2+1);j++){
                List<List<List<String>>> dataDay=new ArrayList<>();
                for(int k=0;k<5;k++){
                    List<List<String>> dataPeriod=new ArrayList<>();
                    for(int l=0;l<periodCount;l++){
                        List<String> dataSlot=new ArrayList<>();
                        dataSlot.add(null);
                        dataSlot.add(null);
                        dataPeriod.add(dataSlot);
                    }
                    dataDay.add(dataPeriod);
                }
                dataSection.add(dataDay);
            }
            data.add(dataSection);
        }
    }

    public void parseChromo(Scanner sc, String[] subjects, String[] teachers){
        this.resetData();
        SubjectDao subjectDao=SubjectDao.getInstance();

        for(int i=0;i<subjects.length;i++){
            byte sem=(byte)subjectDao.get(subjects[i]).getSem();
            byte secCount=ScheduleStructure.getInstance().getSectionCount(sem);
            sem=(byte)(sem%2==0?sem/2:(sem+1)/2);
            boolean practical=subjectDao.get(subjects[i]).isPractical();

            for(byte sec=0;sec<secCount;sec++){
                String teacher=null;
                if(!practical)teacher=teachers[sc.nextShort()];
                int lectureCount=subjectDao.get(subjects[i]).getLectureCount();

                for(int j=0;j<lectureCount;j++){
                    if(practical)teacher=teachers[sc.nextShort()];
                    short value=sc.nextShort();
                    data
                            .get(sem-1)
                            .get(sec)
                            .get(value/10)
                            .set(value%10,Arrays.asList(teacher,subjects[i]));
                }
            }
        }
    }

    public void removeAllTeachers(){
        for(int i=0;i<data.size();i++){
            var iData=data.get(i);
            for(int j=0;j<iData.size();j++){
                var jData=iData.get(j);
                for(int k=0;k<5;k++){
                    var kData=jData.get(k);
                    for(int l=0;l<kData.size();l++){
                        kData.get(l).set(0,null);
                    }
                }
            }
        }
    }

    public void removeTeacherByName(String name){
        for(int i=0;i<data.size();i++){
            for(int j=0;j<data.get(i).size();j++){
                for(int k=0;k<5;k++){
                    for(int l=0;l<data.get(i).get(j).get(k).size();l++){
                        if(data.get(i).get(j).get(k).get(l).get(0)!=null && data.get(i).get(j).get(k).get(l).get(0).equals(name))
                            data.get(i).get(j).get(k).get(l).set(0,null);
                    }
                }
            }
        }
    }

    //format of return [day][period]=new String[]{"semester","section","subject code"}
    public String[][][] getTeacherScheduleByName(String name){
        byte periodCount=ScheduleStructure.getInstance().getPeriodCount();
        String[][][] sch=new String[5][periodCount][];
        for(int i=0;i<data.size();i++){
            for(int j=0;j<data.get(i).size();j++){
                for(int k=0;k<5;k++){
                    for(int l=0;l<data.get(i).get(j).get(k).size();l++){
                        if(data.get(i).get(j).get(k).get(l).get(0)!=null && data.get(i).get(j).get(k).get(l).get(0).equals(name))
                            sch[k][l]= new String[]{String.valueOf(i + 1),String.valueOf(j + 1),data.get(i).get(j).get(k).get(l).get(1)};
                    }
                }
            }
        }
        return sch;
    }

    public void removeSubjectByCode(String code){
        for(int i=0;i<data.size();i++){
            for(int j=0;j<data.get(i).size();j++){
                for(int k=0;k<5;k++){
                    for(int l=0;l<data.get(i).get(j).get(k).size();l++){
                        if(data.get(i).get(j).get(k).get(l).get(1)!=null && data.get(i).get(j).get(k).get(l).get(1).equals(code)) {
                            data.get(i).get(j).get(k).get(l).set(1,null);
                            data.get(i).get(j).get(k).get(l).set(0,null);
                        }
                    }
                }
            }
        }
    }

    public List<List<List<List<List<String>>>>> getData(){
        return data;
    }

    public List<List<List<String>>> getData(int semester, int section){
        return this.data.get(semester-1).get(section-1);
    }

    public void setData(int semester, int section, List<List<List<String>>> data){
        this.data.get(semester-1).set(section-1,data);
    }

    public List<List<List<List<String>>>> getData(int semester){
        return this.data.get(semester-1);
    }

}
