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
