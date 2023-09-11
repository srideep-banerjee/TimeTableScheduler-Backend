package org.example.pojo;

import org.example.dao.SubjectDao;

public class ScheduleStructure {
    private byte[] sectionsPerSemester;
    private byte periodsPerSemester;
    private byte[][] breakPerSemester;//if breakPerSemester[0]=3, semester 1 has break between 3rd and 4th periods
    private byte semesterCount=4;
    private static ScheduleStructure instance;
    private ScheduleStructure(){}
    public static ScheduleStructure getInstance(){
        if(instance==null){
            instance=new ScheduleStructure();
            instance.sectionsPerSemester=new byte[]{0,0,1};
            instance.periodsPerSemester=9;
            instance.breakPerSemester=new byte[][]{{5},{5},{5}};
        }
        return instance;
    }

    public byte getSectionCount(int semester){
        semester=semester%2==0?semester/2:(semester+1)/2;
        return this.sectionsPerSemester[semester-1];
    }

    public byte[] getBreakLocations(int semester){
        semester=semester%2==0?semester/2:(semester+1)/2;
        return this.breakPerSemester[semester-1];
    }

    public byte getPeriodCount(){
        return this.periodsPerSemester;
    }

    public byte getSemesterCount(){
        return this.semesterCount;
    }


    public void setSectionsPerSemester(byte[] sectionsPerSemester){
        this.sectionsPerSemester=sectionsPerSemester;
    }

    public void setBreakPerSemester(byte[][] breakPerSemester) {
        this.breakPerSemester = breakPerSemester;
    }

    public void setPeriodsPerSemester(byte periodsPerSemester){
        this.periodsPerSemester=periodsPerSemester;
    }

    public int getTotalLectureCount(){
        int count=0;
        for(Subject subject:SubjectDao.getInstance().values())
            count+=subject.getLectureCount()*getSectionCount(subject.getSem());
        return count;
    }


}
