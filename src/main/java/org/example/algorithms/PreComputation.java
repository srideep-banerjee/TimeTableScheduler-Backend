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
    private ArrayList<Integer>[] teachersForSubjects;
    private HashMap<String, Short> indexOfTeacher;
    private String[] roomCodes;
    private  HashMap<String, Short> indexOfRoom;
    SubjectDao subjectDao = SubjectDao.getInstance();
    TeacherDao teacherDao = TeacherDao.getInstance();

    public void compute() {

        //update teacherNameArray and subjectCodeArray
        this.subjectCodeArray = subjectDao.keySet().toArray(String[]::new);
        this.teacherNameArray = teacherDao.keySet().toArray(String[]::new);

        //Update practical room code array
        HashSet<String> roomCodeSet = new HashSet<>();
        for(Subject subject: SubjectDao.getInstance().values()) {
            roomCodeSet.addAll(subject.getRoomCodes());
        }
        this.roomCodes = roomCodeSet.toArray(String[]::new);

        //Updating index of room
        indexOfRoom = new HashMap<>();
        for(short i = 0; i < roomCodes.length; i++) {
            indexOfRoom.put(roomCodes[i], i);
        }

        //Evaluating index of subjects
        HashMap<String, Short> indexOfSubject = new HashMap<>();
        for (short i = 0; i < subjectCodeArray.length; i++)
            indexOfSubject.put(subjectCodeArray[i], i);

        //Updating index of teachers
        this.indexOfTeacher = new HashMap<>();
        for (short i = 0; i < teacherNameArray.length; i++)
            indexOfTeacher.put(teacherNameArray[i], i);

        //Updating teachers for subjects
        this.teachersForSubjects = new ArrayList[subjectCodeArray.length];

        for (int i = 0; i < teachersForSubjects.length; i++)
            teachersForSubjects[i] = new ArrayList<>();

        for (int i = 0; i < teacherNameArray.length; i++)
            for (String code : teacherDao.get(teacherNameArray[i]).getSubjects())
                if (subjectDao.containsKey(code))
                    teachersForSubjects[indexOfSubject.get(code)].add(i);
        //Sort subjectCodeArray according to number of available teachers
        Arrays.sort(subjectCodeArray, (a,b) ->
                teachersForSubjects[indexOfSubject.get(a)].size() - teachersForSubjects[indexOfSubject.get(b)].size());

        //Sort subjectCodeArray according to number of available rooms
        Arrays.sort(subjectCodeArray,
                (a,b) -> subjectDao.get(a).getRoomCodes().size() - subjectDao.get(b).getRoomCodes().size());

        //Move non-free subjects to the beginning
        Arrays.sort(subjectCodeArray, (a,b)->{
            boolean aFree = subjectDao.get(a).isFree();
            boolean bFree = subjectDao.get(b).isFree();
            if (aFree == bFree) return 0;
            else if (aFree) return 1;
            else return -1;
        });

        //Move practical subjects to the beginning
        Arrays.sort(subjectCodeArray, (a,b)->{
            boolean aPractical = subjectDao.get(a).isPractical();
            boolean bPractical = subjectDao.get(b).isPractical();
            if (aPractical == bPractical) return 0;
            else if (aPractical) return -1;
            else return 1;
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

    public String[] getSubjectCodes() {
        return this.subjectCodeArray;
    }

    public String[] getTeacherNames() {
        return this.teacherNameArray;
    }

    public String[] getRoomCodes() {
        return roomCodes;
    }

    public HashMap<String, Short> getIndexOfRoom() {
        return indexOfRoom;
    }

    public HashMap<String, Short> getIndexOfTeacher() {
        return indexOfTeacher;
    }
}
