package org.example.algorithms;

import org.example.dao.SubjectDao;
import org.example.dao.TeacherDao;
import org.example.pojo.Subject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class PreComputation {
    private String[] subjectCodeArray;
    private String[] teacherNameArray;
    private HashMap<String, Short> indexOfSubject;
    private ArrayList<Integer>[] teachersForSubjects;
    private String[] practicalRoomCodeArray;
    private  HashMap<String, Short> indexOfRoom;
    SubjectDao subjectDao = SubjectDao.getInstance();
    TeacherDao teacherDao = TeacherDao.getInstance();

    public PreComputation(String[] subjectCodeArray, String[] teacherNameArray) {
        this.subjectCodeArray = subjectCodeArray;
        this.teacherNameArray = teacherNameArray;
    }
    public void compute() {
        //Update practical room code array
        HashSet<String> roomCodes = new HashSet<>();
        for(Subject subject: SubjectDao.getInstance().values()) {
            roomCodes.addAll(subject.getRoomCodes());
        }
        this.practicalRoomCodeArray = roomCodes.toArray(String[]::new);
        indexOfRoom = new HashMap<>();
        for(short i = 0;i < practicalRoomCodeArray.length; i++) {
            indexOfRoom.put(practicalRoomCodeArray[i], i);
        }

        //Updating index of subjects
        this.indexOfSubject = new HashMap<>();
        for (short i = 0; i < subjectCodeArray.length; i++)
            indexOfSubject.put(subjectCodeArray[i], i);

        //Updating teachers for subjects
        this.teachersForSubjects = new ArrayList[subjectCodeArray.length];

        for (int i = 0; i < teachersForSubjects.length; i++)
            teachersForSubjects[i] = new ArrayList<>();

        for (int i = 0; i < teacherNameArray.length; i++)
            for (String code : teacherDao.get(teacherNameArray[i]).getSubjects())
                if (subjectDao.containsKey(code))
                    teachersForSubjects[indexOfSubject.get(code)].add(i);

        //Sort subjectCodeArray to shift practical subjects to the front or lesser teacher count subjects to the front
        Arrays.sort(subjectCodeArray, (a,b)->{
            Subject subA = subjectDao.get(a);
            Subject subB = subjectDao.get(b);
            int teacherCountA = teachersForSubjects[indexOfSubject.get(a)].size();
            int teacherCountB = teachersForSubjects[indexOfSubject.get(b)].size();
            if(subA.isPractical() == subB.isPractical()) {
                return teacherCountA - teacherCountB;
            } else if (subA.isPractical()) {
                return -1;
            } else return 1;
        });

        //Update new indices
        for (short i = 0; i < subjectCodeArray.length; i++) {
            indexOfSubject.put(subjectCodeArray[i], i);
        }

        //Updating teacher for subjects to new indices
        this.teachersForSubjects = new ArrayList[subjectCodeArray.length];

        for (int i = 0; i < teachersForSubjects.length; i++)
            teachersForSubjects[i] = new ArrayList<>();

        for (int i = 0; i < teacherNameArray.length; i++)
            for (String code : teacherDao.get(teacherNameArray[i]).getSubjects())
                if (subjectDao.containsKey(code))
                    teachersForSubjects[indexOfSubject.get(code)].add(i);
    }

    public ArrayList<Integer>[] getTeachersForSubjects() {
        return teachersForSubjects;
    }

    public HashMap<String, Short> getIndexOfSubject() {
        return indexOfSubject;
    }

    public String[] getPracticalRoomCodes() {
        return practicalRoomCodeArray;
    }

    public HashMap<String, Short> getIndexOfRoom() {
        return indexOfRoom;
    }
}
