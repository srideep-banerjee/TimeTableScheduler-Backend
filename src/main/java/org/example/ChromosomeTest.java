package org.example;

import org.example.algorithms.DayPeriod;
import org.example.algorithms.Util;
import org.example.algorithms.io.PopulationStorage;
import org.example.dao.SubjectDao;
import org.example.dao.TeacherDao;
import org.example.pojo.ScheduleStructure;
import org.example.pojo.Subject;

import java.io.FileNotFoundException;
import java.util.*;

public class ChromosomeTest {
    private String[] teacherNameArray;
    private String[] subjectCodeArray;
    private String[] practicalRoomCodeArray;

    public ChromosomeTest(String[] subjectCodeArray, String[] teacherNamesArray, String[] practicalRoomCodeArray) {
        this.teacherNameArray = teacherNamesArray;

        this.subjectCodeArray = subjectCodeArray;

        this.practicalRoomCodeArray = practicalRoomCodeArray;
    }

    public void testGenerator(int population, int index) {
        PopulationStorage populationStorage = new PopulationStorage(population);

        SubjectDao subjectDao = SubjectDao.getInstance();
        TeacherDao teacherDao = TeacherDao.getInstance();
        ScheduleStructure scheduleData = ScheduleStructure.getInstance();

        int hCount = 0, sCount = 0;
        boolean stopped = false;

        //HashMap<String, Integer> h3 = new HashMap<>();

        HashSet<String> h4 = new HashSet<>();

        HashMap<String, Short> h5 = new HashMap<>();

        HashSet<String> h6 = new HashSet<>();

        HashSet<String> h7 = new HashSet<>();

        HashMap<String, List<short[]>> h89 = new HashMap<>();

        boolean[] h10 = new boolean[teacherNameArray.length];

        Scanner sc;
        try {
            sc = populationStorage.getChromosomeReader(index);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        OuterLoop:
        for (short subjectIndex = 0; subjectIndex < subjectCodeArray.length; subjectIndex++) {
            System.out.println("<---------------- " + subjectCodeArray[subjectIndex] + " ---------------->");
            String subject = subjectCodeArray[subjectIndex];
            Subject sub = subjectDao.get(subject);
            short semester = (short) sub.getSem();
            byte secCount = scheduleData.getSectionCount(semester);
            int lectureCount = sub.getLectureCount();

            for (short section = 1; section <= secCount; section++) {
                System.out.println("<---------------- Section " + section + " ---------------->");
                short teacherIndex = -1;
                short val = -1;
                short value;
                String teacher = null;
                String roomCode = null;
                if (sub.isPractical()) {
                    val = sc.nextShort();
                    roomCode = sub.isFree()? null : practicalRoomCodeArray[sc.nextShort()];
                } else if (!sub.isFree())  {
                    teacherIndex = sc.nextShort();
                    teacher = teacherNameArray[teacherIndex];
                }

                for (int j = 0; j < lectureCount; j++) {
                    if (stopped) break OuterLoop;
                    if (sub.isPractical()) {
                        if (!sub.isFree()) {
                            teacherIndex = sc.nextShort();
                            teacher = teacherNameArray[teacherIndex];
                        }
                        value = (short) (val + j);
                    } else {
                        value = sc.nextShort();
                    }
                    DayPeriod dp= new DayPeriod(value);
                    short period = (short) (dp.period + 1);
                    short day = (short) (dp.day + 1);

                    //evaluating h6
                    String key = String.format("%d,%d,%d,%d", day, period, semester, section);
                    if (h6.contains(key)) hCount++;
                    else h6.add(key);
                    System.out.println("h6 hCount = " + hCount);

                    //none other constraints required if subject is free
                    if (sub.isFree()) continue;

                    //evaluating h2
                    if (!teacherDao.get(teacher).getFreeTime().contains(Arrays.asList(day, period)) && !teacherDao.get(teacher).getFreeTime().isEmpty())
                        hCount++;
                    System.out.println("h2 hCount = " + hCount);

                    //evaluating h4
                    if (subjectDao.get(subject).isPractical()) {
                        key = String.format("%d,%d,%s", day, period, roomCode);
                        if (h4.contains(key)) hCount++;
                        else h4.add(key);
                        System.out.println("h4 hCount = " + hCount);
                    }

                    //evaluating h5
                    else {
                        key = String.format("%d,%s", section, subject);
                        /*if (!h5.containsKey(key)) */
                        h5.put(key, teacherIndex);
                        //else if (h5.get(key) != teacherIndex) count++;
                        System.out.println("h5 hCount = " + hCount);
                    }

                    //evaluating h7
                    key = String.format("%d,%d,%d", teacherIndex, day, period);
                    if (h7.contains(key)) hCount++;
                    else h7.add(key);
                    System.out.println("h7 hCount = " + hCount);

                    //processing h8, h9, h12, h13 and s1
                    if (subjectDao.get(subject).isPractical()) {
                        key = String.format("%d,%s", section, subject);
                        if (!h89.containsKey(key))
                            h89.put(key, new ArrayList<>());
                        h89.get(key).add(new short[]{day, period, teacherIndex});
                    }

                    //processing h10
                    h10[teacherIndex] = true;
                }
            }
        }

        System.out.println("sc.hasNext = " + sc.hasNext());
        sc.close();
        System.out.println("final hCount = "+ hCount);

        //evaluating h8, h9, h12 and h13
        for (var entries : h89.entrySet()) {
            String key = entries.getKey();
            if (stopped) break;
            List<short[]> slots = entries.getValue();
            HashSet<Short> teachers = new HashSet<>();
            StringBuilder sb = new StringBuilder(key.substring(key.indexOf(",") + 1));
            sb.setCharAt(sb.length() - 2, '0');
            boolean hasTheory = SubjectDao.getInstance().containsKey(sb.toString());

            //finding count of slot with different days
            /*float sum = 0;
            for (short[] slot : slots) sum += slot[0];
            float mean = sum / slots.size();
            sum = 0;
            for (short[] slot : slots) sum += Math.abs(slot[0] - mean);
            count += Math.round(sum);*/

            slots.sort(Comparator.comparingInt(a -> a[1]));

            //evaluating h8
            /*
            for (byte i = 1; i < slots.size(); i++)
                if (slots.get(i)[1] - 1 != slots.get(i - 1)[1]) count++;*/

            for (short[] slot : slots) teachers.add(slot[2]);

            //evaluating h12
            if (hasTheory && !teachers.contains(h5.get(key.substring(0, key.indexOf(',')) + "," + sb))) hCount++;

            //evaluating h9
            for (Short teacherIndex : teachers) {
                for (short[] slot : slots) {
                    if (slot[2] == teacherIndex) continue;
                    if (h7.contains(String.format("%d,%d,%d", teacherIndex, slot[0], slot[1]))) hCount++;
                }
            }

            //evaluating h13
            hCount += Math.abs(slots.size() - teachers.size());

            //evaluating s1
            if(slots.get(0)[1] != Util.getPracticalStartingPeriodLocation(key.substring(key.indexOf(',') + 1)))
                sCount++;
        }

        //evaluating h10
        for (boolean b : h10)
            if (!b) hCount++;
    }

    public static void startTest() {
        ChromosomeTest ct = new ChromosomeTest(new String[]{"PCC-CS592", "ESC591", "PCC-CS593", "LIB", "HSMC-501", "MC501", "PEC-ITB", "RPI", "ESC501", "PCC-CS503", "PCC-CS502", "PCC-CS501", "LET", "GD"}, new String[]{"DG", "SAR", "MRM", "J", "DS", "BR", "SC", "SG", "SBG", "MG", "SS", "AC", "PKC", "SKB", "LKM", "AP", "AS", "PC", "PD", "PKP", "SKHC", "RG", "RKM", "SKS", "TP"}, new String[]{"LAB-13,14", "LAB-3,4", "LH123", "LAB-7,8"});
        ct.testGenerator(0,0);
    }
}
