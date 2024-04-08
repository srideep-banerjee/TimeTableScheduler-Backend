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
    private final ArrayList<Integer>[] teachersForSubjects;
    private boolean stopped;
    private final Random random;

    HashMap<SemesterSection, HashMap<DayPeriod, String>> sectionAllocationTable;//value = subject code
    HashMap<SemesterSection, HashMap<DayPeriod, HashMap<String, Short>>> sectionConflictTable;//value = {subject code:count}
    HashMap<TeacherTimeSlot, SemesterSection> teacherTimeAllocationTable;
    HashMap<TeacherTimeSlot, HashMap<SemesterSection, Short>> teacherTimeConflictTable;//value = {semester-section:count}
    HashMap<Short, TeacherSubjectsData> teacherSubjectAllocationTable;//key = teacher index
    HashMap<String, HashSet<DayPeriod>> practicalLabAllocationTable;

    public ChromosomeAnalyzer(String[] subjectCodeArray, String[] teacherNameArray, ArrayList<Integer>[] teachersForSubjects) {
        this.subjectCodeArray = subjectCodeArray;
        this.teacherNameArray = teacherNameArray;
        this.teachersForSubjects = teachersForSubjects;

        sectionAllocationTable = new HashMap<>();
        sectionConflictTable = new HashMap<>();
        teacherTimeAllocationTable = new HashMap<>();
        teacherTimeConflictTable = new HashMap<>();
        teacherSubjectAllocationTable = new HashMap<>();
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

    public boolean isSectionAvailable(SemesterSection semesterSection, DayPeriod dayPeriod) {
        byte[] breakLocations = ScheduleStructure.getInstance().getBreakLocations(semesterSection.semester);
        for (byte brk : breakLocations) if (brk == dayPeriod.period + 1) return false;
        HashMap<DayPeriod, String> hm = sectionAllocationTable.get(semesterSection);
        if (hm == null) return true;
        return hm.get(dayPeriod) == null;
    }

    public boolean isTeacherAvailable(DayPeriod dayPeriod, short teacherIndex) {
        Teacher teacher = TeacherDao.getInstance().get(teacherNameArray[teacherIndex]);
        if (!teacher.getFreeTime().isEmpty() && !teacher.getFreeTime().contains(new int[]{dayPeriod.day, dayPeriod.period}))
            return false;
        return !teacherTimeAllocationTable.containsKey(new TeacherTimeSlot(dayPeriod, teacherIndex));
    }

    public boolean isPracticalLabAvailable(DayPeriod dayPeriod, String roomCode) {
        if (!practicalLabAllocationTable.containsKey(roomCode)) return true;
        return !practicalLabAllocationTable.get(roomCode).contains(dayPeriod);
    }

    public void assignTheory(SemesterSection semesterSection, DayPeriod dayPeriod, short teacherIndex, String subject) {
        Subject subjectObj = SubjectDao.getInstance().get(subject);
        if (subjectObj.isPractical()) throw new IllegalArgumentException(subject + " is a practical subject");
        if (subjectObj.isFree()) throw new IllegalArgumentException(subject + " is a free subject");

        boolean sectionFree = isSectionAvailable(semesterSection, dayPeriod);
        boolean teacherFree = isTeacherAvailable(dayPeriod, teacherIndex);

        if (sectionFree) {
            HashMap<DayPeriod, String> hm = sectionAllocationTable.computeIfAbsent(semesterSection, k -> new HashMap<>());
            hm.put(dayPeriod, subject);
        }
        if (teacherFree) {
            teacherTimeAllocationTable.put(new TeacherTimeSlot(dayPeriod, teacherIndex), semesterSection);
        }

        TeacherSubjectsData tsd = teacherSubjectAllocationTable.computeIfAbsent(teacherIndex, k -> new TeacherSubjectsData());
        HashSet<SemesterSection> subjects = tsd.subjects.computeIfAbsent(subject, k -> new HashSet<>());
        if (!subjects.contains(semesterSection)) {
            subjects.add(semesterSection);
            tsd.theoryCount++;
        }
    }

    public void assignFreeTheory(SemesterSection semesterSection, DayPeriod dayPeriod, String subject) {
        Subject subjectObj = SubjectDao.getInstance().get(subject);
        if (subjectObj.isPractical()) throw new IllegalArgumentException(subject + " is a practical subject");
        if (!subjectObj.isFree()) throw new IllegalArgumentException(subject + " is not a free subject");

        boolean sectionFree = isSectionAvailable(semesterSection, dayPeriod);

        if (sectionFree) {
            HashMap<DayPeriod, String> hm = sectionAllocationTable.computeIfAbsent(semesterSection, k -> new HashMap<>());
            hm.put(dayPeriod, subject);
        }
    }

    public void assignPractical(SemesterSection semesterSection, DayPeriod startDayPeriod, short[] teacherIndices, String subject, String roomCode) {
        Subject subjectObj = SubjectDao.getInstance().get(subject);
        if(!subjectObj.isPractical())  throw new IllegalArgumentException(subject + " is not a practical subject");
        if (subjectObj.isFree()) throw new IllegalArgumentException(subject + " is a free subject");

        byte lectureCount = (byte) subjectObj.getLectureCount();
        short compact = startDayPeriod.getCompact();

        HashMap<DayPeriod, String> sectionSlots = sectionAllocationTable.computeIfAbsent(semesterSection, k -> new HashMap<>());
        HashSet<DayPeriod> labAllocDayPeriods = practicalLabAllocationTable.computeIfAbsent(roomCode, k -> new HashSet<>());

        //iterate through all day-period of practical class
        for (short com = compact; com < compact + lectureCount; com++) {
            DayPeriod dayPeriod = new DayPeriod(com);

            //check if section free during period
            if (isSectionAvailable(semesterSection, dayPeriod))
                sectionSlots.put(dayPeriod, subject);

            //check if all teacher free during period
            for (short teacherIndex: teacherIndices) {
                if(isTeacherAvailable(dayPeriod, teacherIndex)) {
                    teacherTimeAllocationTable.put(new TeacherTimeSlot(dayPeriod, teacherIndex), semesterSection);
                }
            }

            //check if practical lab free during period
            if(isPracticalLabAvailable(dayPeriod, roomCode))
                labAllocDayPeriods.add(dayPeriod);
        }

        for (short teacherIndex: teacherIndices) {
            TeacherSubjectsData tsd = teacherSubjectAllocationTable.computeIfAbsent(teacherIndex, k -> new TeacherSubjectsData());
            HashSet<SemesterSection> subjects = tsd.subjects.computeIfAbsent(subject, k -> new HashSet<>());
            if (!subjects.contains(semesterSection)) {
                subjects.add(semesterSection);
                tsd.practicalCount++;
            }
        }
    }

    public void assignFreePractical(SemesterSection semesterSection, DayPeriod startDayPeriod, String subject) {
        Subject subjectObj = SubjectDao.getInstance().get(subject);
        if(!subjectObj.isPractical())  throw new IllegalArgumentException(subject + " is not a practical subject");
        if (!subjectObj.isFree()) throw new IllegalArgumentException(subject + " is not a free subject");

        byte lectureCount = (byte) subjectObj.getLectureCount();
        short compact = startDayPeriod.getCompact();

        HashMap<DayPeriod, String> sectionSlots = sectionAllocationTable.computeIfAbsent(semesterSection, k -> new HashMap<>());

        //iterate through all day-period of practical class
        for (short com = compact; com < compact + lectureCount; com++) {
            DayPeriod dayPeriod = new DayPeriod(com);

            //check if section free during period
            if (isSectionAvailable(semesterSection, dayPeriod))
                sectionSlots.put(dayPeriod, subject);
        }
    }

    public boolean isAssignNonConflicting(SemesterSection semesterSection, DayPeriod dayPeriod, short teacherIndex, String subject, String roomCode) {
        Subject subjectObj = SubjectDao.getInstance().get(subject);
        boolean sectionFree = isSectionAvailable(semesterSection, dayPeriod);
        boolean teacherFree = isTeacherAvailable(dayPeriod, teacherIndex);
        boolean practicalLabFree = !subjectObj.isPractical() ||
                isPracticalLabAvailable(dayPeriod, roomCode);
        return sectionFree && teacherFree && practicalLabFree;
    }

    public PracticalTimeRoom suggestPracticalTimeRoom(SemesterSection semesterSection, short[] teacherIndices, String subject) {
        Subject sub = SubjectDao.getInstance().get(subject);
        if (!sub.isPractical()) throw new IllegalArgumentException(subject + " is not a practical subject");

        ArrayList<PracticalTimeRoom> choices = new ArrayList<>();

        //Get available room codes
        ArrayList<String> availableRoomCodes = sub.getRoomCodes();

        ArrayList<Byte> allocationPeriods = Util.getAllPracticalPeriodLocations(subject);

        //iterate through each day and check if allocation conflict free
        for (byte startPeriod: allocationPeriods) {
            for (String roomCode : availableRoomCodes) {
                for (byte day = 0; day < 5; day++) {
                    boolean valid = true;
                    PeriodCheckerLoop:
                    for (byte period = startPeriod; period < startPeriod + sub.getLectureCount(); period++) {
                        DayPeriod dayPeriod = new DayPeriod(day, period);
                        for (short teacherIndex : teacherIndices) {
                            if (!isAssignNonConflicting(semesterSection, dayPeriod, teacherIndex, subject, roomCode)) {
                                valid = false;
                                break PeriodCheckerLoop;
                            }
                        }
                    }

                    //if allocation possible, add the starting dayPeriod to choices
                    if (valid) choices.add(new PracticalTimeRoom(roomCode, new DayPeriod(day, startPeriod)));
                }
                if (choices.size() > 0) break;
            }
            if (choices.size() > 0) break;
        }

        if(choices.size() == 0) return new PracticalTimeRoom(availableRoomCodes.get(0), new DayPeriod((byte) random.nextInt(5), allocationPeriods.get(0)));
        return choices.get(random.nextInt(choices.size()));
    }

    public DayPeriod suggestFreePracticalDayPeriod(SemesterSection semesterSection, String subject) {
        Subject sub = SubjectDao.getInstance().get(subject);
        if (!sub.isPractical()) throw new IllegalArgumentException(subject + " is not a practical subject");
        if (!sub.isFree()) throw new IllegalArgumentException(subject + " is not a free subject");

        ArrayList<DayPeriod> choices = new ArrayList<>();

        ArrayList<Byte> allocationPeriods = Util.getAllPracticalPeriodLocations(subject);

        //iterate through each day and check if allocation conflict free
        for (byte startPeriod: allocationPeriods) {
            for (byte day = 0; day < 5; day++) {
                boolean valid = true;
                for (byte period = startPeriod; period < startPeriod + sub.getLectureCount(); period++) {
                    DayPeriod dayPeriod = new DayPeriod(day, period);
                    if (!isSectionAvailable(semesterSection, dayPeriod)) {
                        valid = false;
                        break;
                    }
                }

                //if allocation possible, add the starting dayPeriod to choices
                if (valid) choices.add(new DayPeriod(day, startPeriod));
            }
            if (!choices.isEmpty()) break;
        }

        if(choices.isEmpty()) return new DayPeriod((byte) random.nextInt(5), allocationPeriods.get(0));
        return choices.get(random.nextInt(choices.size()));
    }

    public ArrayList<DayPeriod> suggestTheoryDayPeriod(SemesterSection semesterSection, short teacherIndex, String subject) {
        Subject sub = SubjectDao.getInstance().get(subject);
        if (sub.isPractical()) throw new IllegalArgumentException(subject + " is a practical subject");

        ArrayList<DayPeriod> dayPeriods = new ArrayList<>();
        int periodCount = ScheduleStructure.getInstance().getPeriodCount();
        //Here 5 is the day count
        int[] values = Util.shuffle(periodCount * 5);

        //if semesterSection isn't allocated
        for (int i = 0; i < values.length && dayPeriods.size() < sub.getLectureCount(); i++) {
            DayPeriod dayPeriod = new DayPeriod((short) values[i]);
            if (isAssignNonConflicting(semesterSection, dayPeriod, teacherIndex, subject, null)) {
                dayPeriods.add(dayPeriod);
            }
        }
        while (dayPeriods.size() < sub.getLectureCount()) {
            dayPeriods.add(new DayPeriod((short) random.nextInt(periodCount * 5)));
        }
        return dayPeriods;
    }

    public ArrayList<DayPeriod> suggestFreeTheoryDayPeriod(SemesterSection semesterSection, String subject) {
        Subject sub = SubjectDao.getInstance().get(subject);
        if (sub.isPractical()) throw new IllegalArgumentException(subject + " is a practical subject");
        if (!sub.isFree()) throw new IllegalArgumentException(subject + " is not a free subject");

        ArrayList<DayPeriod> dayPeriods = new ArrayList<>();
        int periodCount = ScheduleStructure.getInstance().getPeriodCount();
        //Here 5 is the day count
        int[] values = Util.shuffle(periodCount * 5);

        //if semesterSection isn't allocated
        for (int i = 0; i < values.length && dayPeriods.size() < sub.getLectureCount(); i++) {
            DayPeriod dayPeriod = new DayPeriod((short) values[i]);
            if (isSectionAvailable(semesterSection, dayPeriod)) {
                dayPeriods.add(dayPeriod);
            }
        }
        while (dayPeriods.size() < sub.getLectureCount()) {
            dayPeriods.add(new DayPeriod((short) random.nextInt(periodCount * 5)));
        }
        return dayPeriods;
    }

    public ArrayList<Short> suggestPracticalTeachers(SemesterSection semesterSection, short subjectIndex) {
        String subject = subjectCodeArray[subjectIndex];
        Subject sub = SubjectDao.getInstance().get(subject);
        TeacherDao teacherDao = TeacherDao.getInstance();

        if (!sub.isPractical()) throw new IllegalArgumentException(subject + " is not a practical subject");

        ArrayList<Integer> availableTeachers = teachersForSubjects[subjectIndex];
        HashSet<Short> res = new HashSet<>();

        //if practical subject doesn't have a theory
        String theoryEquivalent = SubjectDao.getInstance().getTheoryOfPractical(subject);
        if (theoryEquivalent == null) {
            for (int selected: Util.shuffle(availableTeachers.size())) {
                res.add(availableTeachers.get(selected).shortValue());
                if(res.size() == sub.getLectureCount()) break;
            }
            return new ArrayList<>(res);
        }

        //get set of teachers who teach both theory and practical of the subject
        HashSet<Integer> practicalAndTheoryTeachers = new HashSet<>();
        short availableTheoryTeachersCount = 0;
        HashSet<Integer> practicalOnlyTeachers = new HashSet<>();
        short availablePracticalOnlyTeachersCount = 0;

        boolean theoryTeacherAdded = false;

        int[] shuffledIndices = Util.shuffle(availableTeachers.size());
        for(int randomIndex: shuffledIndices) {
            int teacherIndex = availableTeachers.get(randomIndex);
            TeacherSubjectsData tsd = teacherSubjectAllocationTable.computeIfAbsent((short) teacherIndex, k -> new TeacherSubjectsData());
            if(teacherDao.get(teacherNameArray[teacherIndex]).getSubjects().contains(theoryEquivalent)) {
                practicalAndTheoryTeachers.add(teacherIndex);
                if(tsd.practicalCount < 3 && !tsd.subjects.containsKey(subject) && !theoryTeacherAdded) {
                    availableTheoryTeachersCount++;
                    theoryTeacherAdded = true;
                    res.add((short) teacherIndex);
                } else if(tsd.practicalCount < 3) {
                    availableTheoryTeachersCount++;
                }
            } else {
                practicalOnlyTeachers.add(teacherIndex);
                if(tsd.practicalCount < 3) availablePracticalOnlyTeachersCount++;
            }
        }

        for (int randomIndex: shuffledIndices) {
            int teacherIndex = availableTeachers.get(randomIndex);
            if (practicalAndTheoryTeachers.contains(teacherIndex) && !theoryTeacherAdded) {
                theoryTeacherAdded = true;
                res.add((short) teacherIndex);
            } else if (theoryTeacherAdded && practicalOnlyTeachers.contains(teacherIndex)) {
                res.add((short) teacherIndex);
            } else if (res.size() != sub.getLectureCount() - 1){
                res.add((short) teacherIndex);
            }
            if (res.size() == sub.getLectureCount()) break;
        }

        return new ArrayList<>(res);
    }

    public Short suggestTheoryTeacher(SemesterSection semesterSection, short subjectIndex) {
        ArrayList<Integer> availableTeachers = teachersForSubjects[subjectIndex];
        int[] randomizedIndices = Util.shuffle(availableTeachers.size());
        String practicalEquivalent = SubjectDao.getInstance().getPracticalOfTheory(subjectCodeArray[subjectIndex]);
        if(practicalEquivalent == null) {
            return availableTeachers.get(random.nextInt(availableTeachers.size())).shortValue();
        }
        for(int randomIndex: randomizedIndices) {
            short teacherIndex = availableTeachers.get(randomIndex).shortValue();
            TeacherSubjectsData tsd = teacherSubjectAllocationTable.computeIfAbsent(teacherIndex, k -> new TeacherSubjectsData());
            if (tsd.subjects.computeIfAbsent(practicalEquivalent, k -> new HashSet<>()).contains(semesterSection)) {
                return teacherIndex;
            }
        }
        return availableTeachers.get(random.nextInt(availableTeachers.size())).shortValue();
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

    public static class TeacherSubjectsData {
        public HashMap<String, HashSet<SemesterSection>> subjects = new HashMap<>();
        public short practicalCount = 0;
        public short theoryCount = 0;
    }

    public static class PracticalTimeRoom {
        public String roomCode;
        public DayPeriod time;

        public PracticalTimeRoom(String roomCode, DayPeriod time) {
            this.roomCode = roomCode;
            this.time = time;
        }
    }
}
