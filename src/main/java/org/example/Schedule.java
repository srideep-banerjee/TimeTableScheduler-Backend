package org.example;

import org.example.pojo.Subject;
import org.example.pojo.Teacher;

public class Schedule {
    //format [semester][section][day][period][subject code,teacher name]
    private String data[][][][][];

    public Schedule(int semesterCount,int sectionCount){
        this.data=new String[semesterCount][sectionCount][7][8][2];
    }
    public String[] get(int semester,int section, int day, int period){
        return data[semester][section][day][period];
    }
    public void set(int semester, int section, int day, int period,String subjectCode,String teacherName){
        String[] temp={subjectCode,teacherName};
        data[semester][section][day][period]=temp;
    }
}
