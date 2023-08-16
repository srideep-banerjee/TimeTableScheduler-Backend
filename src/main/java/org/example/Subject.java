package org.example;

public class Subject {
    private String subjectCode;
    private int sem;
    private int lectureCount;
    private final boolean isPractical;
    private OnModifiedListener onModifiedListener;

    public Subject(String subjectCode, int sem, int lectureCount, boolean isPractical, OnModifiedListener onModifiedListener) {
        this.subjectCode = subjectCode;
        this.sem = sem;
        this.lectureCount = lectureCount;
        this.isPractical = isPractical;
        this.onModifiedListener = onModifiedListener;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode){
        this.subjectCode=subjectCode;
        onModifiedListener.onModified();
    }

    public int getSem() {
        return sem;
    }

    public void setSem(int sem){
        this.sem=sem;
        onModifiedListener.onModified();
    }

    public int getLectureCount() {
        return lectureCount;
    }

    public void setLectureCount(int lectureCount){
        this.lectureCount=lectureCount;
        onModifiedListener.onModified();
    }

    public boolean isPractical() {
        return isPractical;
    }

    public void setOnModifiedListener(OnModifiedListener onModifiedListener) {
        this.onModifiedListener = onModifiedListener;
    }
}
