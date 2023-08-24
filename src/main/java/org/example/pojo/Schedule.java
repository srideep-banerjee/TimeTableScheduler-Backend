package org.example.pojo;

import org.example.dao.SubjectDao;

public class Schedule {
    private int[] sectionsPerSemester;
    private int[] periodsPerSemester;
    private int[] breakPerSemester;//if breakPerSemester[0]=3, semester 1 has break between 3rd and 4th periods
    private int semesterCount=4;
    private static Schedule instance;
    private Schedule(){}
    public static Schedule getInstance(){
        if(instance==null){
            instance=new Schedule();
            instance.sectionsPerSemester=new int[]{3,3,3,3};
            instance.periodsPerSemester=new int[]{7,8,8,8};
            instance.breakPerSemester=new int[]{3,4,4,4};
        }
        return instance;
    }

    public int getSectionCount(int semester){
        return this.sectionsPerSemester[semester-1];
    }

    public int getBreakLocation(int semester){
        return this.breakPerSemester[semester-1];
    }

    public int getPeriodCount(int semester){
        return this.periodsPerSemester[semester-1];
    }

    public void setSectionsPerSemester(int[] sectionsPerSemester){
        this.sectionsPerSemester=sectionsPerSemester;
    }

    public void setBreakPerSemester(int[] breakPerSemester) {
        this.breakPerSemester = breakPerSemester;
    }

    public int getTotalLectureCount(){
        int count=0;
        for(Subject subject:SubjectDao.getInstance().values())
            count+=subject.getLectureCount()*getSectionCount(subject.getSem());
        return count;
    }

    public int getSemesterCount() {
        return semesterCount;
    }
}
