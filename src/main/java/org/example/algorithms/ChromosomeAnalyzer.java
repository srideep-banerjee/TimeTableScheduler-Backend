package org.example.algorithms;

import org.example.dao.SubjectDao;
import org.example.pojo.ScheduleStructure;
import org.example.pojo.Subject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class ChromosomeAnalyzer {
    private String[] subjectCodeArray;
    private String[] teacherNameArray;
    private boolean stopped;

    HashMap<String, Short> theorySectionSubjectTeacherMap;
    HashMap<String, HashSet<Short>> practicalLabAllotment;

    public ChromosomeAnalyzer(String[] subjectCodeArray, String[] teacherNameArray) {
        this.subjectCodeArray=subjectCodeArray;
        this.teacherNameArray=teacherNameArray;
    }

    public void analyze(Scanner sc) {

        SubjectDao subjectDao = SubjectDao.getInstance();
        ScheduleStructure scheduleData = ScheduleStructure.getInstance();

        theorySectionSubjectTeacherMap = new HashMap<>();
        practicalLabAllotment = new HashMap<>();

        OuterLoop:
        for (short subjectIndex = 0; subjectIndex < subjectCodeArray.length; subjectIndex++) {
            String subject = subjectCodeArray[subjectIndex];
            Subject sub = subjectDao.get(subject);
            short semester = (short) sub.getSem();
            byte secCount = scheduleData.getSectionCount(semester);
            int lectureCount = sub.getLectureCount();

            for (short section = 1; section <= secCount; section++) {
                short teacherIndex = -1;
                short val = -1;
                short value;
                String teacher = null;
                if (sub.isPractical()) {
                    val = sc.nextShort();
                } else {
                    teacherIndex = sc.nextShort();
                    teacher = teacherNameArray[teacherIndex];
                }

                for (int j = 0; j < lectureCount; j++) {
                    if (stopped) break OuterLoop;
                    if (sub.isPractical()) {
                        teacherIndex = sc.nextShort();
                        teacher = teacherNameArray[teacherIndex];
                        value = (short) (val + j);
                    } else {
                        value = sc.nextShort();
                    }
                    short period = (short) (value % 10 + 1);
                    short day = (short) (value / 10 + 1);

                    if(!subjectDao.get(subject).isPractical()) {

                    } else {
                        String key = String.format("%d,%s", section, subject);
                        theorySectionSubjectTeacherMap.put(key, teacherIndex);
                    }
                }
            }
        }
    }

    public void stop() {
        stopped = true;
    }
}
