package org.example.pojo;

import org.example.dao.SubjectDao;

public class ScheduleSolution {

    //format: data[semester][section][day][period][]=new String[]{"teacherName","subjectCode"}
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

    public void parseChromo(short[] chromo, String[] subjects, String[] teachers){
        this.resetData();
        SubjectDao subjectDao=SubjectDao.getInstance();
        if(chromo.length%4!=0) throw new RuntimeException("Invalid chromosome");
        for(int i=0;i<chromo.length/4;i++){
            short day=(short)(chromo[i]/10);
            short period=(short)(chromo[i]%10);
            short semester=(short) subjectDao.get(subjects[chromo[i+3]]).getSem();
            short section=chromo[i+1];
            String teacher=teachers[chromo[i+2]];
            String subject=subjects[chromo[i+3]];
            data[semester][section][day][period]=new String[]{teacher,subject};
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

    public void removeSubjectByCode(String code){
        for(int i=0;i<data.length;i++){
            for(int j=0;j<data[i].length;j++){
                for(int k=0;k<5;k++){
                    for(int l=0;l<data[i][j][k].length;l++){
                        if(data[i][j][k][l][1]!=null && data[i][j][k][l][1].equals(code)) {
                            data[i][j][k][l][0] = null;
                            data[i][j][k][l][1] = null;
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
}
