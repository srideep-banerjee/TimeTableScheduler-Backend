package org.example.pojo;

import org.example.algorithms.io.ChromosomeReader;
import org.example.dao.SubjectDao;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

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

    /**
     * Empties the schedule by setting all entries to null without modifying
     * schedule solution structure
     */
    public void resetData() {
        empty = true;
        ScheduleStructure ss = ScheduleStructure.getInstance();
        data = new ArrayList<>();
        for (int i = 0; i < ss.getSemesterCount(); i++) {
            List<List<List<List<String>>>> dataSection = new ArrayList<>();
            for (int j = 0; j < ss.getSectionCount(i * 2 + 1); j++) {
                List<List<List<String>>> dataDay = new ArrayList<>();
                for (int k = 0; k < ss.getDayCount(); k++) {
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

    /**
     * Updates the schedule solution structure according to current schedule structure
     * by truncating values or adding null values
     */
    public void updateStructure() {
        ScheduleStructure ss = ScheduleStructure.getInstance();
        List<List<List<List<List<String>>>>> previousData = data;
        boolean isEmpty = empty;
        resetData();
        empty = isEmpty;
        for (int year = 0; year < ss.getSemesterCount() && year < previousData.size(); year++) {
            for (int sec = 0; sec < ss.getSectionCount(year * 2 + 1) && sec < previousData.get(year).size(); sec++) {
                for (int day = 0; day < ss.getDayCount(); day++) {
                    for (int period = 0; period < ss.getPeriodCount() && period < previousData.get(year).get(sec).get(day).size(); period++) {
                        boolean in = true;
                        for (byte brk : ss.getBreakLocations(year * 2 + 1)) {
                            if (brk - 1 == period) {
                                in = false;
                                break;
                            }
                        }
                        if (in)
                            data.get(year).get(sec).get(day).set(period, previousData.get(year).get(sec).get(day).get(period));
                    }
                }
            }
        }
    }

    public static class SolutionAccumulator {
        private final List<List<List<List<List<String>>>>> accumulated;

        public SolutionAccumulator() {
            // Setting accumulated to an empty solution :
            var temp = getInstance().data; // creating backup of current solution
            getInstance().resetData(); // emptying current solution
            accumulated = getInstance().data; // setting accumulated to current empty solution
            getInstance().setData(temp); // restoring current solution from backup
        }

        public void add(
                int sem,
                int sec,
                int day,
                int period,
                String subjectCode,
                String teacherName,
                String roomCode
        ) {
            accumulated.get(sem).get(sec).get(day).set(period, Arrays.asList(teacherName, subjectCode, roomCode));
        }

        public List<List<List<List<List<String>>>>> accumulate() {
            SubjectDao subjectDao = SubjectDao.getInstance();
            Stream<List<List<String>>> linearPeriodDataStream = accumulated
                    .stream()
                    .flatMap(Collection::stream)
                    .flatMap(Collection::stream);

            linearPeriodDataStream.forEach(dayData -> {

                String previousPracticalSubjectCode = null;
                LinkedHashSet<String> practicalTeachers = new LinkedHashSet<>();
                ArrayList<List<String>> practicalPeriodList = new ArrayList<>();

                for (List<String> periodData: dayData) {
                    String teacherName = periodData.get(0);
                    String subjectCode = periodData.get(1);
                    Subject subject = subjectDao.get(subjectCode);

                    if (previousPracticalSubjectCode != null) {

                        if (subjectCode != null && subjectCode.equalsIgnoreCase(previousPracticalSubjectCode)) {
                            practicalTeachers.add(teacherName);
                            practicalPeriodList.add(periodData);

                        } else {
                            String presentedTeacherString = String.join("+", practicalTeachers);
                            for (List<String> practicalPeriod: practicalPeriodList) {
                                practicalPeriod.set(0, presentedTeacherString);
                            }
                            previousPracticalSubjectCode = null;
                            practicalTeachers.clear();
                            practicalPeriodList.clear();
                        }
                    } else if (subject != null && subject.isPractical() && !subject.isFree()) {
                        previousPracticalSubjectCode = subjectCode;
                        practicalTeachers.add(teacherName);
                        practicalPeriodList.add(periodData);
                    }
                }
                if (previousPracticalSubjectCode != null) {
                    String presentedTeacherString = String.join("+", practicalTeachers);
                    for (List<String> practicalPeriod: practicalPeriodList) {
                        practicalPeriod.set(0, presentedTeacherString);
                    }
                }
            });
            return accumulated;
        }
    }

    public static class SolutionIterator {
        public final boolean teachersCombined;
        private final SolutionIteratorCallback iteratorCallback;

        public SolutionIterator(SolutionIteratorCallback iteratorCallback) {
            teachersCombined = false;
            this.iteratorCallback = iteratorCallback;
        }

        public void iterate() {
            var data = instance.data;

            for (int sem = 0; sem < data.size(); sem++) {
                var semData = data.get(sem);
                for (int sec = 0; sec < semData.size(); sec++) {
                    var secData = semData.get(sec);
                    for (int day = 0; day < secData.size(); day++) {
                        var dayData = secData.get(day);

                        int period = 0;
                        while (period < dayData.size()) {

                            List<String> periodData = dayData.get(period);
                            String teacher = periodData.get(0);
                            String subjectCode = periodData.get(1);
                            String roomCode = periodData.get(2);

                            if (teacher != null && teacher.contains("+")) {
                                String[] teachersArray = teacher.split("[+]");
                                for (String splitTeacher: teachersArray)
                                    iteratorCallback.callback(sem, sec, day, period++, subjectCode, splitTeacher, roomCode);
                                continue;
                            }

                            if (subjectCode != null)
                                iteratorCallback.callback(sem, sec, day, period, subjectCode, teacher, roomCode);

                            period++;
                        }
                    }
                }
            }
        }
    }

    public interface SolutionIteratorCallback {
        void callback(int sem, int sec, int day, int period, String subjectCode, String teacherName, String roomCode);
    }

    /**
     * Responsible for parsing a generator chromosome and converting it to a
     * schedule solution array, updating the current schedule solution
     * @param chromosomeReader A ChromosomeReader reading the chromosome
     */
    public void parseChromo(ChromosomeReader chromosomeReader) {
        this.resetData();
        SolutionAccumulator solutionAccumulator = new SolutionAccumulator();
        try (chromosomeReader) {
            chromosomeReader.readAll(solutionAccumulator::add);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setData(solutionAccumulator.accumulate());
    }

    public void removeAllTeachers() {
        for (int i = 0; i < data.size(); i++) {
            var iData = data.get(i);
            for (int j = 0; j < iData.size(); j++) {
                var jData = iData.get(j);
                for (int k = 0; k < jData.size(); k++) {
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
                for (int k = 0; k < data.get(i).get(j).size(); k++) {
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
        ScheduleStructure ss = ScheduleStructure.getInstance();
        String[][][] sch = new String[ss.getDayCount()][ss.getPeriodCount()][];
        for (int i = 0; i < data.size(); i++) {
            for (int j = 0; j < data.get(i).size(); j++) {
                for (int k = 0; k < data.get(i).get(j).size(); k++) {
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
                for (int k = 0; k < data.get(i).get(j).size(); k++) {
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

    public List<List<List<String>>> getData(int year, int section) {
        try {
            return this.data.get(year - 1).get(section - 1);
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
        int dayCount = ScheduleStructure.getInstance().getDayCount();
        if (data.size() != dayCount) {
            return String.format("Number of days is %d, but should be equal to %d", data.size(), dayCount);
        }
        int periodCount = ScheduleStructure.getInstance().getPeriodCount();
        for (int i = 0; i < dayCount; i++) {
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

    public List<List<List<List<String>>>> getData(int year) {
        try {
            return this.data.get(year - 1);
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
