package org.example.pojo;

import org.example.algorithms.DayPeriod;
import org.example.dao.SubjectDao;

import java.util.*;

public class ScheduleSolution {

    //format: data[semester][section][day][period]=new String[]{"teacherName","subjectCode","roomCode"}
    private List<List<List<List<List<String>>>>> data;
    private static ScheduleSolution instance = null;

    private boolean empty = true;

    private ScheduleSolution() {
        this.resetData();
    }

    public static ScheduleSolution getInstance() {
        if (instance == null) instance = new ScheduleSolution();
        return instance;
    }

    public void resetData() {
        empty = true;
        ScheduleStructure ss = ScheduleStructure.getInstance();
        data = new ArrayList<>();
        for (int i = 0; i < ss.getSemesterCount(); i++) {
            List<List<List<List<String>>>> dataSection = new ArrayList<>();
            for (int j = 0; j < ss.getSectionCount(i * 2 + 1); j++) {
                List<List<List<String>>> dataDay = new ArrayList<>();
                for (int k = 0; k < 5; k++) {
                    List<List<String>> dataPeriod = new ArrayList<>();
                    for (int l = 0; l < ss.getPeriodCount(); l++) {
                        List<String> dataSlot = new ArrayList<>();
                        dataSlot.add(null);
                        dataSlot.add(null);
                        dataSlot.add(null);
                        dataPeriod.add(dataSlot);
                    }
                    dataDay.add(dataPeriod);
                }
                dataSection.add(dataDay);
            }
            data.add(dataSection);
        }
    }

    public void updateStructure() {
        ScheduleStructure ss = ScheduleStructure.getInstance();
        List<List<List<List<List<String>>>>> previousData = data;
        boolean isEmpty = empty;
        resetData();
        empty = isEmpty;
        for (int i = 0; i < ss.getSemesterCount() && i < previousData.size(); i++) {
            for (int j = 0; j < ss.getSectionCount(i * 2 + 1) && j < previousData.get(i).size(); j++) {
                for (int k = 0; k < 5; k++) {
                    for (int l = 0; l < ss.getPeriodCount() && l < previousData.get(i).get(j).get(k).size(); l++) {
                        boolean in = true;
                        for (byte brk : ss.getBreakLocations(i * 2 + 1)) {
                            if (brk - 1 == l) {
                                in = false;
                                break;
                            }
                        }
                        if (in)
                            data.get(i).get(j).get(k).set(l, previousData.get(i).get(j).get(k).get(l));
                    }
                }
            }
        }
    }

    public void parseChromo(Scanner sc, String[] subjects, String[] teachers, String[] roomCodes) {
        this.resetData();
        SubjectDao subjectDao = SubjectDao.getInstance();
        HashMap<String, List<Short>> practicalPeriods = new HashMap<>();
        HashMap<String, HashSet<String>> practicalTeachers = new HashMap<>();

        for (String subject : subjects) {
            byte sem = (byte) subjectDao.get(subject).getSem();
            byte secCount = ScheduleStructure.getInstance().getSectionCount(sem);
            sem = (byte) (sem % 2 == 0 ? sem / 2 : (sem + 1) / 2);
            boolean practical = subjectDao.get(subject).isPractical();
            boolean free = subjectDao.get(subject).isFree();

            for (byte sec = 0; sec < secCount; sec++) {
                String teacher = null;
                short val = -1;
                short value;
                String roomCode;
                if (!practical) {
                    if (!free) teacher = teachers[sc.nextShort()];
                    roomCode = subjectDao.get(subject).getRoomCodes().get(0);
                }
                else {
                    val = sc.nextShort();
                    if (!free) roomCode = roomCodes[sc.nextShort()];
                    else roomCode = subjectDao.get(subject).getRoomCodes().get(0);
                }
                int lectureCount = subjectDao.get(subject).getLectureCount();

                for (int j = 0; j < lectureCount; j++) {
                    if (practical) {
                        value = (short) (val + j);
                        if (!free) {
                            teacher = teachers[sc.nextShort()];
                            String key = String.format("%d,%d,%s", sem - 1, sec, subject);
                            if (!practicalPeriods.containsKey(key)) practicalPeriods.put(key, new ArrayList<>());
                            practicalPeriods.get(key).add(value);
                            if (!practicalTeachers.containsKey(key)) practicalTeachers.put(key, new HashSet<>());
                            practicalTeachers.get(key).add(teacher);
                        }
                    } else {
                        value = sc.nextShort();
                    }
                    DayPeriod dayPeriod = new DayPeriod(value);
                    data.get(sem - 1).get(sec).get(dayPeriod.day).set(dayPeriod.period, Arrays.asList(teacher, subject, roomCode));
                }
            }
            empty = false;
        }
        for (String key : practicalPeriods.keySet()) {
            String[] keyData = key.split(",");
            String teachersCombined = String.join("+", practicalTeachers.get(key));
            for (Short value : practicalPeriods.get(key)) {
                DayPeriod dp= new DayPeriod(value);
                data.get(Short.parseShort(keyData[0]))
                        .get(Short.parseShort(keyData[1]))
                        .get(dp.day)
                        .get(dp.period)
                        .set(0, teachersCombined);
            }
        }

        sc.close();
    }

    public void removeAllTeachers() {
        for (int i = 0; i < data.size(); i++) {
            var iData = data.get(i);
            for (int j = 0; j < iData.size(); j++) {
                var jData = iData.get(j);
                for (int k = 0; k < 5; k++) {
                    var kData = jData.get(k);
                    for (int l = 0; l < kData.size(); l++) {
                        kData.get(l).set(0, null);
                    }
                }
            }
        }
    }

    public void removeTeacherByName(String name) {
        for (int i = 0; i < data.size(); i++) {
            for (int j = 0; j < data.get(i).size(); j++) {
                for (int k = 0; k < 5; k++) {
                    for (int l = 0; l < data.get(i).get(j).get(k).size(); l++) {
                        if (data.get(i).get(j).get(k).get(l).get(0) != null && data.get(i).get(j).get(k).get(l).get(0).contains(name))
                            data.get(i).get(j).get(k).get(l).set(0, null);
                    }
                }
            }
        }
    }

    //format of return [day][period]=new String[]{"semester","section","subject code"}
    public String[][][] getTeacherScheduleByName(String name) {
        byte periodCount = ScheduleStructure.getInstance().getPeriodCount();
        String[][][] sch = new String[5][periodCount][];
        for (int i = 0; i < data.size(); i++) {
            for (int j = 0; j < data.get(i).size(); j++) {
                for (int k = 0; k < 5; k++) {
                    for (int l = 0; l < data.get(i).get(j).get(k).size(); l++) {
                        if (data.get(i).get(j).get(k).get(l).get(0) != null && data.get(i).get(j).get(k).get(l).get(0).contains(name)) {
                            String subject = data.get(i).get(j).get(k).get(l).get(1);
                            String roomCode = data.get(i).get(j).get(k).get(l).get(2);
                            sch[k][l] = new String[]{
                                    String.valueOf(SubjectDao.getInstance().get(subject).getSem()),
                                    String.valueOf(j),
                                    subject,
                                    roomCode
                            };
                        }
                    }
                }
            }
        }
        return sch;
    }

    public void removeSubjectByCode(String code) {
        for (int i = 0; i < data.size(); i++) {
            for (int j = 0; j < data.get(i).size(); j++) {
                for (int k = 0; k < 5; k++) {
                    for (int l = 0; l < data.get(i).get(j).get(k).size(); l++) {
                        if (data.get(i).get(j).get(k).get(l).get(1) != null && data.get(i).get(j).get(k).get(l).get(1).equals(code)) {
                            data.get(i).get(j).get(k).get(l).set(0, null);
                            data.get(i).get(j).get(k).get(l).set(1, null);
                            data.get(i).get(j).get(k).get(l).set(2, null);
                        }
                    }
                }
            }
        }
    }

    public List<List<List<List<List<String>>>>> getData() {
        return data;
    }

    public void setData(List<List<List<List<List<String>>>>> data) {
        empty = false;
        this.data = data;
    }

    public List<List<List<String>>> getData(int semester, int section) {
        semester = (byte) (semester % 2 == 0 ? semester / 2 : (semester + 1) / 2);
        try {
            return this.data.get(semester - 1).get(section - 1);
        } catch (RuntimeException e) {
            return null;
        }
    }

    public String setData(int year, int section, List<List<List<String>>> data) {
        int sectionCount = ScheduleStructure.getInstance().getSectionCount(year * 2);
        if (section > sectionCount) {
            return String.format(
                    "Section: %d is grater than section count: %d for year %d",
                    section, sectionCount,
                    year
            );
        }
        int yearCount = ScheduleStructure.getInstance().getSemesterCount();
        if (year > yearCount) {
            return String.format("Year: %d is greater than year count: %d", year, yearCount);
        }
        if (data.size() != 5) {
            return String.format("Number of days is %d, but should be equal to 5", data.size());
        };
        int periodCount = ScheduleStructure.getInstance().getPeriodCount();
        for (int i = 0; i < 5; i++) {
            if (data.get(i).size() != periodCount) {
                return String.format("Period count is %d, but should be equal to %d", data.get(i).size(), periodCount);
            }
            for (int j = 0; j < periodCount; j++) {
                if (data.get(i).get(j).size() != 3) {
                    return String.format("Expecting 3 items per period, got %d", data.get(i).get(j).size());
                }
            }
        }
        this.data.get(year - 1).set(section - 1, data);
        return null;
    }

    public List<List<List<List<String>>>> getData(int semester) {
        semester = (byte) (semester % 2 == 0 ? semester / 2 : (semester + 1) / 2);
        try {
            return this.data.get(semester - 1);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }
}
