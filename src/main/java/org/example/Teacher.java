package org.example;

public class Teacher {
    private String name;
    private int[] sems;
    private int[][] freeTime;
    private String[] subjects;
    private OnModifiedListener onModifiedListener;

    public Teacher(String name, int[] sems, int[][] freeTime, String[] subjects) {
        this.name = name;
        this.sems = sems;
        this.freeTime = freeTime;
        this.subjects = subjects;
    }

    public String getName() {
        return name;
    }

    public int[] getSems() {
        return sems;
    }

    public int[][] getFreeTime() {
        return freeTime;
    }

    public String[] getSubjects() {
        return subjects;
    }

    public void setOnModifiedListener(OnModifiedListener onModifiedListener) {
        this.onModifiedListener = onModifiedListener;
    }
}
