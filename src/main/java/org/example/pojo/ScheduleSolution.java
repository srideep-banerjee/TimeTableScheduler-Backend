package org.example.pojo;

import org.example.dao.SubjectDao;

import java.util.*;

public class ScheduleSolution {

    //format: data[semester][section][day][period]=new String[]{"teacherName","subjectCode"}
    private List<List<List<List<List<String>>>>> data;
    private static ScheduleSolution instance = null;

    private boolean empty=true;

    private ScheduleSolution() {
        this.resetData();
    }

    public static ScheduleSolution getInstance() {
        if (instance == null) instance = new ScheduleSolution();
        return instance;
    }

    public void resetData() {
        empty=true;
        ScheduleStructure ss = ScheduleStructure.getInstance();
        data = new ArrayList<>();
        byte periodCount = ss.getPeriodCount();
        for (int i = 0; i < ss.getSemesterCount(); i++) {
            List<List<List<List<String>>>> dataSection = new ArrayList<>();
            for (int j = 0; j < ss.getSectionCount(i * 2 + 1); j++) {
                List<List<List<String>>> dataDay = new ArrayList<>();
                for (int k = 0; k < 5; k++) {
                    List<List<String>> dataPeriod = new ArrayList<>();
                    for (int l = 0; l < periodCount; l++) {
                        List<String> dataSlot = new ArrayList<>();
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

    public void parseChromo(Scanner sc, String[] subjects, String[] teachers) {
        this.resetData();
        SubjectDao subjectDao = SubjectDao.getInstance();
        HashMap<String, List<Short>> practicalPeriods = new HashMap<>();
        HashMap<String, HashSet<String>> practicalTeachers = new HashMap<>();

        for (String subject : subjects) {
            byte sem = (byte) subjectDao.get(subject).getSem();
            byte secCount = ScheduleStructure.getInstance().getSectionCount(sem);
            sem = (byte) (sem % 2 == 0 ? sem / 2 : (sem + 1) / 2);
            boolean practical = subjectDao.get(subject).isPractical();

            for (byte sec = 0; sec < secCount; sec++) {
                String teacher = null;
                short val=-1;
                short value;
                if (!practical) teacher = teachers[sc.nextShort()];
                else val=sc.nextShort();
                int lectureCount = subjectDao.get(subject).getLectureCount();

                for (int j = 0; j < lectureCount; j++) {
                    if (practical) {
                        teacher = teachers[sc.nextShort()];
                        String key = String.format("%d,%d,%s", sem - 1, sec, subject);
                        value= (short) (val+j);
                        if (!practicalPeriods.containsKey(key)) practicalPeriods.put(key, new ArrayList<>());
                        practicalPeriods.get(key).add(value);
                        if (!practicalTeachers.containsKey(key)) practicalTeachers.put(key, new HashSet<>());
                        practicalTeachers.get(key).add(teacher);
                    }
                    else {
                        value = sc.nextShort();
                    }
                    data.get(sem - 1).get(sec).get(value / 10).set(value % 10, Arrays.asList(teacher, subject));
                }
            }
            empty=false;
        }
        for (String key : practicalPeriods.keySet()) {
            String[] keyData = key.split(",");
            String teachersCombined = String.join("+", practicalTeachers.get(key));
            for (Short value : practicalPeriods.get(key)) {
                data.get(Short.parseShort(keyData[0]))
                        .get(Short.parseShort(keyData[1]))
                        .get(value / 10)
                        .get(value % 10)
                        .set(0, teachersCombined);
            }
        }
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
                            String subject=data.get(i).get(j).get(k).get(l).get(1);
                            sch[k][l] = new String[]{String.valueOf(SubjectDao.getInstance().get(subject).getSem()), String.valueOf(j), subject};
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
                            data.get(i).get(j).get(k).get(l).set(1, null);
                            data.get(i).get(j).get(k).get(l).set(0, null);
                        }
                    }
                }
            }
        }
    }

    public List<List<List<List<List<String>>>>> getData() {
        return data;
    }

    public List<List<List<String>>> getData(int semester, int section) {
        return this.data.get(semester - 1).get(section - 1);
    }

    public void setData(int semester, int section, List<List<List<String>>> data) {
        this.data.get(semester - 1).set(section - 1, data);
    }

    public List<List<List<List<String>>>> getData(int semester) {
        return this.data.get(semester - 1);
    }

    public boolean isEmpty(){
        return empty;
    }
}
