package org.example.pojo;

import org.example.interfaces.OnModifiedListener;

import java.util.*;

public class Teacher {
    private final String name;
    private HashSet<List<Integer>> freeTime;
    private HashSet<String> subjects;
    private OnModifiedListener onModifiedListener;

    public Teacher(String name, HashSet<List<Integer>> freeTime, HashSet<String> subjects) {
        this.name = name;
        this.freeTime=freeTime;
        this.subjects = subjects;
    }

    public String getName() {
        return name;
    }

    public HashSet<List<Integer>> getFreeTime() {
        return freeTime;
    }

    public void setFreeTime(HashSet<List<Integer>> freeTime) {
        this.freeTime = freeTime;
        onModifiedListener.onModified();
    }

    public HashSet<String> getSubjects() {
        return subjects;
    }

    public void setSubjects(HashSet<String> subjects){
        this.subjects=subjects;
        onModifiedListener.onModified();
    }

    public void setOnModifiedListener(OnModifiedListener onModifiedListener) {
        this.onModifiedListener = onModifiedListener;
    }
}
