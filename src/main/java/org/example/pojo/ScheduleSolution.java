package org.example.pojo;

import org.example.dao.SubjectDao;

public class ScheduleSolution {

    //format: data[semester][section][day][period][]=new String[]{"teacherName","subjectCode"}
    private String[][][][][] data;

    public ScheduleSolution(){
        ScheduleStructure ss=ScheduleStructure.getInstance();
        data=new String[ss.getSemesterCount()][][][][];
        for(int i=0;i<data.length;i++){
            data[i]=new String[ss.getSectionCount(i+1)][5][ss.getPeriodCount()][2];
        }
    }

    public void parseChromo(short[] chromo, String[] subjects, String[] teachers){
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

    public String[][][][][] getData(){
        return data;
    }
    public String[][][] getData(int semester, int section){
        return this.data[semester-1][section-1];
    }
}
