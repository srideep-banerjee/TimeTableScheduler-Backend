package org.example.algorithms;

import org.example.dao.SubjectDao;
import org.example.dao.TeacherDao;
import org.example.pojo.ScheduleStructure;
import org.example.pojo.Subject;
import org.example.pojo.Teacher;

import java.util.*;

public class ChromosomeAnalyzer {
    private final String[] subjectCodeArray;
    private final String[] teacherNameArray;
    private boolean stopped;
    private final Random random;

    HashMap<SemesterSection, HashMap<DayPeriod, String>> sectionAllocationTable;//value = subject code
    HashMap<SemesterSection, HashMap<DayPeriod, HashMap<String, Short>>> sectionConflictTable;//value = {subject code:count}
    HashMap<TeacherTimeSlot, SemesterSection> teacherTimeAllocationTable;
    HashMap<TeacherTimeSlot, HashMap<SemesterSection, Short>> teacherTimeConflictTable;//value = {semester-section:count}
    HashMap<String, HashSet<DayPeriod>> practicalLabAllocationTable;

    public ChromosomeAnalyzer(String[] subjectCodeArray, String[] teacherNameArray) {
        this.subjectCodeArray = subjectCodeArray;
        this.teacherNameArray = teacherNameArray;

        sectionAllocationTable = new HashMap<>();
        sectionConflictTable = new HashMap<>();
        teacherTimeAllocationTable = new HashMap<>();
        teacherTimeConflictTable = new HashMap<>();
        practicalLabAllocationTable = new HashMap<>();

        this.random = new Random();
    }

    //Incomplete
    public void analyze(Scanner sc) {
        SubjectDao subjectDao = SubjectDao.getInstance();
        ScheduleStructure scheduleData = ScheduleStructure.getInstance();

        sectionAllocationTable = new HashMap<>();
        sectionConflictTable = new HashMap<>();
        teacherTimeAllocationTable = new HashMap<>();
        teacherTimeConflictTable = new HashMap<>();
        practicalLabAllocationTable = new HashMap<>();

        OuterLoop:
        for (String subject : subjectCodeArray) {
            Subject sub = subjectDao.get(subject);
            byte semester = (byte) sub.getSem();
            byte secCount = scheduleData.getSectionCount(semester);
            int lectureCount = sub.getLectureCount();

            for (byte section = 1; section <= secCount; section++) {
                short teacherIndex = -1;
                short val = -1;
                short value;
                if (sub.isPractical()) {
                    val = sc.nextShort();
                } else {
                    teacherIndex = sc.nextShort();
                }

                for (int j = 0; j < lectureCount; j++) {
                    if (stopped) break OuterLoop;
                    if (sub.isPractical()) {
                        teacherIndex = sc.nextShort();
                        value = (short) (val + j);
                    } else {
                        value = sc.nextShort();
                    }

                    DayPeriod dayPeriod = new DayPeriod(value);
                    SemesterSection semesterSection = new SemesterSection(semester, section);

                    //assign(semesterSection, dayPeriod, teacherIndex, subject);
                }
            }
        }
    }

    public boolean isSectionFree(SemesterSection semesterSection, DayPeriod dayPeriod) {
        byte[] breakLocations = ScheduleStructure.getInstance().getBreakLocations(semesterSection.semester);
        for (byte brk : breakLocations) if (brk == dayPeriod.period + 1) return false;
        HashMap<DayPeriod, String> hm = sectionAllocationTable.get(semesterSection);
        if (hm == null) return true;
        return hm.get(dayPeriod) == null;
    }

    public boolean isTeacherFree(DayPeriod dayPeriod, short teacherIndex) {
        Teacher teacher = TeacherDao.getInstance().get(teacherNameArray[teacherIndex]);
        if (!teacher.getFreeTime().isEmpty() && !teacher.getFreeTime().contains(new int[]{dayPeriod.day, dayPeriod.period}))
            return false;
        return !teacherTimeAllocationTable.containsKey(new TeacherTimeSlot(dayPeriod, teacherIndex));
    }

    public boolean isPracticalLabFree(DayPeriod dayPeriod, String roomCode) {
        if (!practicalLabAllocationTable.containsKey(roomCode)) return true;
        return !practicalLabAllocationTable.get(roomCode).contains(dayPeriod);
    }

    public void assignTheory(SemesterSection semesterSection, DayPeriod dayPeriod, short teacherIndex, String subject) {
//        TeacherTimeSlot teacherTimeSlot = new TeacherTimeSlot(dayPeriod, teacherIndex);
        Subject subjectObj = SubjectDao.getInstance().get(subject);
        if (subjectObj.isPractical()) throw new IllegalArgumentException(subject + " is a practical subject");

        boolean sectionFree = isSectionFree(semesterSection, dayPeriod);
        boolean teacherFree = isTeacherFree(dayPeriod, teacherIndex);

        if (sectionFree) {
            HashMap<DayPeriod, String> hm = sectionAllocationTable.computeIfAbsent(semesterSection, k -> new HashMap<>());
            hm.put(dayPeriod, subject);
//            return;
        }
        if (teacherFree) {
            teacherTimeAllocationTable.put(new TeacherTimeSlot(dayPeriod, teacherIndex), semesterSection);
        }
//        if (!sectionFree) {
//            HashMap<DayPeriod, HashMap<String, Short>> hm1 = sectionConflictTable.computeIfAbsent(semesterSection, k -> new HashMap<>());
//            HashMap<String, Short> dayPeriodConflicts = hm1.computeIfAbsent(dayPeriod, k -> new HashMap<>());
//            Short count = dayPeriodConflicts.get(subject);
//            if (count == null) count = 0;
//            dayPeriodConflicts.put(subject, ++count);
//        }
//        if (!teacherFree) {
//            if (!teacherTimeConflictTable.containsKey(teacherTimeSlot)) {
//                HashMap<SemesterSection, Short> hm = new HashMap<>();
//                hm.put(semesterSection, (short) 0);
//                teacherTimeConflictTable.put(teacherTimeSlot, hm);
//            }
//            HashMap<SemesterSection, Short> hm = teacherTimeConflictTable.get(teacherTimeSlot);
//            hm.put(semesterSection, (short) (hm.get(semesterSection) + 1));
//        }
    }

    public void assignPractical(SemesterSection semesterSection, DayPeriod startDayPeriod, short[] teacherIndices, String subject) {
        Subject subjectObj = SubjectDao.getInstance().get(subject);
        if(!subjectObj.isPractical())  throw new IllegalArgumentException(subject + " is not a practical subject");

        byte lectureCount = (byte) subjectObj.getLectureCount();
        short compact = startDayPeriod.getCompact();

        HashMap<DayPeriod, String> sectionSlots = sectionAllocationTable.computeIfAbsent(semesterSection, k -> new HashMap<>());
        HashSet<DayPeriod> labAllocDayPeriods = practicalLabAllocationTable.computeIfAbsent(subjectObj.getRoomCode(), k -> new HashSet<>());

        //iterate through all day-period of practical class
        for (short com = compact; com < compact + lectureCount; com++) {
            DayPeriod dayPeriod = new DayPeriod(com);

            //check if section free during period
            if (isSectionFree(semesterSection, dayPeriod))
                sectionSlots.put(dayPeriod, subject);

            //check if all teacher free during period
            for (short teacherIndex: teacherIndices) {
                if(isTeacherFree(dayPeriod, teacherIndex)) {
                    teacherTimeAllocationTable.put(new TeacherTimeSlot(dayPeriod, teacherIndex), semesterSection);
                }
            }

            //check if practical lab free during period
            if(isPracticalLabFree(dayPeriod, subjectObj.getRoomCode()))
                labAllocDayPeriods.add(dayPeriod);

        }
    }

    public boolean isAssignConflicting(SemesterSection semesterSection, DayPeriod dayPeriod, short teacherIndex, String subject) {
        Subject subjectObj = SubjectDao.getInstance().get(subject);
        boolean sectionFree = isSectionFree(semesterSection, dayPeriod);
        boolean teacherFree = isTeacherFree(dayPeriod, teacherIndex);
        boolean practicalLabFree = !subjectObj.isPractical() ||
                isPracticalLabFree(dayPeriod, subjectObj.getRoomCode());
        return sectionFree && teacherFree && practicalLabFree;
    }

    public DayPeriod suggestPracticalDayPeriod(SemesterSection semesterSection, short[] teacherIndices, String subject) {
        Subject sub = SubjectDao.getInstance().get(subject);
        if (!sub.isPractical()) throw new IllegalArgumentException(subject + " is not a practical subject");

        ArrayList<DayPeriod> choices = new ArrayList<>();

        //get starting period location
        byte startPeriod = Util.getPracticalStartingPeriodLocation(subject);

        //iterate through each day and check if allocation conflict free
        for (byte day = 0; day < 5; day++) {
            boolean valid = true;
            PeriodCheckerLoop:
            for (byte period = startPeriod; period < startPeriod + sub.getLectureCount(); period++) {
                DayPeriod dayPeriod = new DayPeriod(day, period);
                for (short teacherIndex : teacherIndices) {
                    if (!isAssignConflicting(semesterSection, dayPeriod, teacherIndex, subject)) {
                        valid = false;
                        break PeriodCheckerLoop;
                    }
                }
            }

            //if allocation possible, add the starting dayPeriod to choices
            if (valid) choices.add(new DayPeriod(day, startPeriod));
        }
        if(choices.size() == 0) return new DayPeriod((byte) random.nextInt(5), startPeriod);
        return choices.get(random.nextInt(choices.size()));
    }

    public ArrayList<DayPeriod> suggestTheoryDayPeriod(SemesterSection semesterSection, short teacherIndex, String subject) {
        Subject sub = SubjectDao.getInstance().get(subject);
        if (sub.isPractical()) throw new IllegalArgumentException(subject + " is a practical subject");

        ArrayList<DayPeriod> dayPeriods = new ArrayList<>();
        int periodCount = ScheduleStructure.getInstance().getPeriodCount();
        int[] values = Util.shuffle(periodCount * 5);

        //if semesterSection isn't allocated
        for (int i = 0; i < values.length && dayPeriods.size() < sub.getLectureCount(); i++) {
            DayPeriod dayPeriod = new DayPeriod((short) values[i]);
            if (isAssignConflicting(semesterSection, dayPeriod, teacherIndex, subject)) {
                dayPeriods.add(dayPeriod);
            }
        }
        while (dayPeriods.size() < sub.getLectureCount()) {
            dayPeriods.add(new DayPeriod((short) random.nextInt(periodCount * 5)));
        }
        return dayPeriods;
    }

    public static class TeacherTimeSlot {
        public final DayPeriod dayPeriod;
        public final short teacherIndex;

        public TeacherTimeSlot(DayPeriod dayPeriod, short teacherIndex) {
            this.dayPeriod = dayPeriod;
            this.teacherIndex = teacherIndex;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TeacherTimeSlot that)) return false;
            return teacherIndex == that.teacherIndex && dayPeriod.equals(that.dayPeriod);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dayPeriod, teacherIndex);
        }
    }
}
