package org.example.pojo;

import org.example.interfaces.OnModifiedListener;

public class Teacher {
    private final String name;
    private int[][] freeTime;
    private String[] subjects;
    private OnModifiedListener onModifiedListener;

    public Teacher(String name, int[][] freeTime, String[] subjects) {
        this.name = name;
        this.freeTime = freeTime;
        this.subjects = subjects;
    }

    public String getName() {
        return name;
    }

    public int[][] getFreeTime() {
        return freeTime;
    }

    public void setFreeTime(int[][] freeTime) {
        this.freeTime = freeTime;
        onModifiedListener.onModified();
    }

    public String[] getSubjects() {
        return subjects;
    }

    public void setSubjects(String[] subjects){
        this.subjects=subjects;
        onModifiedListener.onModified();
    }

    public void setOnModifiedListener(OnModifiedListener onModifiedListener) {
        this.onModifiedListener = onModifiedListener;
    }
}
