package org.example.pojo;

import org.example.dao.SubjectDao;

import java.util.Scanner;

public class ScheduleSolution {

    //format: data[semester][section][day][period]=new String[]{"teacherName","subjectCode"}
    private String[][][][][] data;
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
        data=new String[ss.getSemesterCount()][][][][];
        byte periodCount=ss.getPeriodCount();
        for(int i=0;i<data.length;i++){
            data[i]=new String[ss.getSectionCount(i+1)][5][periodCount][2];
        }
    }

    public void parseChromo(Scanner sc, String[] subjects, String[] teachers){
        this.resetData();
        SubjectDao subjectDao=SubjectDao.getInstance();

        for(int i=0;i<subjects.length;i++){
            byte sem=(byte)subjectDao.get(subjects[i]).getSem();
            sem=(byte)(sem%2==0?sem/2:(sem+1)/2);
            byte secCount=ScheduleStructure.getInstance().getSectionCount(sem);
            boolean practical=subjectDao.get(subjects[i]).isPractical();

            for(byte sec=1;sec<=secCount;sec++){
                String teacher=null;
                if(!practical)teacher=teachers[sc.nextShort()];
                int lectureCount=subjectDao.get(subjects[i]).getLectureCount();

                for(int j=0;j<lectureCount;j++){
                    if(practical)teacher=teachers[sc.nextShort()];
                    short value=sc.nextShort();
                    data[sem][sec][value/10][value%10]=new String[]{teacher,subjects[i]};
                }
            }
        }
    }

    public void removeAllTeachers(){
        for(int i=0;i<data.length;i++){
            for(int j=0;j<data[i].length;j++){
                for(int k=0;k<5;k++){
                    for(int l=0;l<data[i][j][k].length;l++){
                        data[i][j][k][l][0]=null;
                    }
                }
            }
        }
    }

    public void removeTeacherByName(String name){
        for(int i=0;i<data.length;i++){
            for(int j=0;j<data[i].length;j++){
                for(int k=0;k<5;k++){
                    for(int l=0;l<data[i][j][k].length;l++){
                        if(data[i][j][k][l][0]!=null && data[i][j][k][l][0].equals(name))
                            data[i][j][k][l][0]=null;
                    }
                }
            }
        }
    }

    //format of return [day][period]=new String[]{"semester","section","subject code"}
    public String[][][] getTeacherScheduleByName(String name){
        byte periodCount=ScheduleStructure.getInstance().getPeriodCount();
        String[][][] sch=new String[5][periodCount][];
        for(int i=0;i<data.length;i++){
            for(int j=0;j<data[i].length;j++){
                for(int k=0;k<5;k++){
                    for(int l=0;l<data[i][j][k].length;l++){
                        if(data[i][j][k][l][0]!=null && data[i][j][k][l][0].equals(name))
                            sch[k][l]= new String[]{String.valueOf(i + 1),String.valueOf(j + 1),data[i][j][k][l][1]};
                    }
                }
            }
        }
        return sch;
    }

    public void removeSubjectByCode(String code){
        for(int i=0;i<data.length;i++){
            for(int j=0;j<data[i].length;j++){
                for(int k=0;k<5;k++){
                    for(int l=0;l<data[i][j][k].length;l++){
                        if(data[i][j][k][l][1]!=null && data[i][j][k][l][1].equals(code)) {
                            data[i][j][k][l]=new String[2];
                        }
                    }
                }
            }
        }
    }

    public String[][][][][] getData(){
        return data;
    }

    public void setData(String[][][][][] data) {
        this.data = data;
    }

    public String[][][] getData(int semester, int section){
        return this.data[semester-1][section-1];
    }

    public void setData(int semester, int section,String[][][] data){
        this.data[semester-1][section-1]=data;
    }

    public String[][][][] getData(int semester){
        return this.data[semester-1];
    }

    public void setData(int semester, String[][][][] data){
        this.data[semester-1]=data;
    }
}
