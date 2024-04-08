package org.example.algorithms;

import org.example.DefaultConfig;
import org.example.algorithms.io.PopulationStorage;
import org.example.dao.SubjectDao;
import org.example.dao.TeacherDao;
import org.example.interfaces.OnResultListener;
import org.example.pojo.ScheduleSolution;
import org.example.pojo.ScheduleStructure;
import org.example.pojo.Subject;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public class Generator {
    private final int populationSize = 400;
    private final int tournamentSize = 5;
    private final float crossoverRate = 0.98f;
    private final float mutationRate = 0.05f;
    private final int stagnantTerminationCount = 75;
    private final int threadCount=4;
    private String[] subjectCodeArray = null;
    private String[] teacherNameArray = null;
    private String[] practicalRoomCodeArray = null;
    private HashMap<String, Short> indexOfRoom = null;
    private HashMap<String, Short> indexOfSubject = null;
    private ArrayList<Integer>[] teachersForSubjects = null;
    private PopulationStorage populationStorage;
    private ArrayList<GeneticThread> geneticThreads=null;
    private final float[] fitness;
    private final Integer[] selectedIndices;
    private float averageFitness = 0;
    private float maxFitness = 0;
    private int maxFitnessIndex = 0;
    private int generation = 0;
    private final OnResultListener onResultListener;
    SubjectDao subjectDao = SubjectDao.getInstance();
    TeacherDao teacherDao = TeacherDao.getInstance();
    ScheduleStructure scheduleData = ScheduleStructure.getInstance();
    boolean stopped = false;

    public Generator(OnResultListener onResultListener) {
        fitness = new float[populationSize];
        selectedIndices = new Integer[populationSize / tournamentSize];
        this.onResultListener = onResultListener;
    }

    public void generate() {
        stopped = false;
        new Thread(() -> {
            try {
                long time=System.currentTimeMillis();
                updateVariables();
                if(stopped) return;
                generateInitialPopulation();
                float prevMaxFitness = maxFitness;
                int stagnantCount = 0;
                calculateFitness();
                String trailer = DefaultConfig.GENERATOR_LOG_SINGLE_LINE? "\r" : "\n";
                System.out.print("Generation:" + generation + " Stagnant count:" + stagnantCount + " Avg. fitness:" + averageFitness + " Max fitness:" + maxFitness + " Index: " + maxFitnessIndex);
                while (maxFitness < 1 && stagnantCount <= stagnantTerminationCount && !stopped) {
                    if(maxFitness == prevMaxFitness) stagnantCount++;
                    else stagnantCount = 0;
                    prevMaxFitness = maxFitness;
                    selectParents();
                    generateNewPopulation();
                    calculateFitness();
                    generation++;
                    System.out.print(trailer + "Generation:" + generation + " Stagnant count:" + stagnantCount + " Avg. fitness:" + averageFitness + " Max fitness:" + maxFitness + " Index: " + maxFitnessIndex);
                }
                System.out.println("\nMax Fitness Index = " + maxFitnessIndex);

                //terminate threads
                for(GeneticThread gt:geneticThreads) gt.interrupt();

                if (stagnantCount > stagnantTerminationCount && !stopped)
                    onResultListener.onError("Couldn't find stable time table with given constraints");
                else {
                    System.out.println("Time taken: "+(System.currentTimeMillis()-time)/1000+" sec");
                    Scanner sc = populationStorage.getChromosomeReader(maxFitnessIndex);
                    ScheduleSolution.getInstance().parseChromo(sc, subjectCodeArray, teacherNameArray, practicalRoomCodeArray);
                    onResultListener.onResult();
                }
            } catch (IOException e) {
                System.out.println(e);
                e.printStackTrace();
                onResultListener.onError(e.getMessage());
            }
        }).start();
    }

    private void updateVariables() {
        //update teacherNameArray and subjectCodeArray
        this.subjectCodeArray = subjectDao.keySet().toArray(String[]::new);
        this.teacherNameArray = teacherDao.keySet().toArray(String[]::new);

        //start geneticThreads
        geneticThreads=new ArrayList<>();
        int populationPerThread=populationSize/threadCount;
        for(int i=0;i<threadCount;i++){
            GeneticThread gt=new GeneticThread(i*populationPerThread,populationPerThread);
            geneticThreads.add(gt);
            gt.start();
        }

        PreComputation preComputation = new PreComputation(subjectCodeArray, teacherNameArray);
        preComputation.compute();

        this.indexOfSubject = preComputation.getIndexOfSubject();

        this.teachersForSubjects = preComputation.getTeachersForSubjects();

        this.practicalRoomCodeArray = preComputation.getPracticalRoomCodes();

        this.indexOfRoom = preComputation.getIndexOfRoom();

        System.out.println(Arrays.toString(subjectCodeArray));
        System.out.println(Arrays.toString(teacherNameArray));
        System.out.println(Arrays.toString(practicalRoomCodeArray));

        for (int i = 0; i < teachersForSubjects.length; i++)
            if (teachersForSubjects[i].isEmpty() && !subjectDao.get(subjectCodeArray[i]).isFree()) {
                onResultListener.onError("Subject: " + subjectCodeArray[i] + " is not free and has no teacher");
                stop();
            }

        HashMap<Integer, Short> totalPeriodCounts = new HashMap<>();
        for (Subject subject : SubjectDao.getInstance().values()) {
            short newLectureCount = (short) (totalPeriodCounts.getOrDefault(subject.getSem(), (short) 0) + subject.getLectureCount());
            totalPeriodCounts.put(subject.getSem(), newLectureCount);
        }
        for (Map.Entry<Integer, Short> entry: totalPeriodCounts.entrySet()) {
            int numberOfPeriods = scheduleData.getPeriodCount() - scheduleData.getBreakLocations(entry.getKey()).length;
            if(entry.getValue() > numberOfPeriods * 5) {
                onResultListener.onError("Number of Lectures required in Year: "+(entry.getKey() + 1) + " exceeds available time slots");
            }
        }
    }

    private void generateInitialPopulation() throws IOException {
        populationStorage = new PopulationStorage(generation);
        for (GeneticThread gt:geneticThreads){
            gt.perform((index,populationPerThread)->{
                for (int i = index; i < (index+populationPerThread) && !stopped; i++) {
                    try {
                        generateRandomChromosome(i);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        waitForAllThreads();
        generation++;
    }

    //chromosome format: {day(0-4) period(0-periodCount-1), section(0-sectionCount), teacherIndex, subjectIndex}
    //New chromosome format: {sub1:{sec1{teachInd1,day period1, day period2}},{sec1{teachInd1,day period1, day period2}}}
    //Ex : {13,33,0,4}
    private void generateRandomChromosome(int index) throws IOException {
        PrintStream ps = populationStorage.getChromosomeWriter(index);
        Random random = new Random();

        ChromosomeAnalyzer ca = new ChromosomeAnalyzer(subjectCodeArray, teacherNameArray, teachersForSubjects);

        for (int i = 0; i < subjectCodeArray.length; i++) {
            byte sem = (byte) subjectDao.get(subjectCodeArray[i]).getSem();
            for (byte sec = 1; sec <= scheduleData.getSectionCount(sem); sec++) {
                SemesterSection semesterSection = new SemesterSection(sem, sec);
                int lectureCount = subjectDao.get(subjectCodeArray[i]).getLectureCount();
                boolean practical = subjectDao.get(subjectCodeArray[i]).isPractical();
                boolean free = subjectDao.get(subjectCodeArray[i]).isFree();
                if (practical) {
                    if (!free) {
                        //Select distinct random teachers
                        ArrayList<Short> teacherList = ca.suggestPracticalTeachers(semesterSection, (short) i);//new short[lectureCount];
                        short[] teachers = new short[teacherList.size()];
                        for (int ind = 0; ind < teachers.length; ind++) teachers[ind] = teacherList.get(ind);

                        ChromosomeAnalyzer.PracticalTimeRoom ptr = ca.suggestPracticalTimeRoom(semesterSection, teachers, subjectCodeArray[i]);

                        DayPeriod dayPeriod = ptr.time;
                        ps.println(dayPeriod.getCompact());

                        ps.println(indexOfRoom.get(ptr.roomCode));

                        ca.assignPractical(semesterSection, dayPeriod, teachers, subjectCodeArray[i], ptr.roomCode);
                        for (int k = 0; k < lectureCount; k++) {
                            ps.println(teachers[k]);
                        }
                    } else {
                        DayPeriod dayPeriod = ca.suggestFreePracticalDayPeriod(semesterSection, subjectCodeArray[i]);
                        ca.assignFreePractical(semesterSection, dayPeriod, subjectCodeArray[i]);
                        ps.println(dayPeriod.getCompact());
                    }
                } else {
                    if (!free) {
                        short teacher = ca.suggestTheoryTeacher(semesterSection, (short) i);
                        ps.println(teacher);

                        ArrayList<DayPeriod> dayPeriods = ca.suggestTheoryDayPeriod(semesterSection, teacher, subjectCodeArray[i]);
                        for (DayPeriod dayPeriod : dayPeriods) {
                            ca.assignTheory(semesterSection, dayPeriod, teacher, subjectCodeArray[i]);
                            ps.println(dayPeriod.getCompact());
                        }
                    } else {
                        ArrayList<DayPeriod> dayPeriods = ca.suggestFreeTheoryDayPeriod(semesterSection, subjectCodeArray[i]);
                        for (DayPeriod dayPeriod : dayPeriods) {
                            ca.assignFreeTheory(semesterSection, dayPeriod, subjectCodeArray[i]);
                            ps.println(dayPeriod.getCompact());
                        }
                    }
                }
            }
        }
        ps.close();
    }

    private void calculateFitness() throws IOException {
        float sum = 0f;
        maxFitness = 0;
        final Integer[] localMaxFitnessIndex=new Integer[threadCount];
        final Float[] localSum=new Float[threadCount];

        Arrays.fill(localSum,0f);

        for (GeneticThread gt:geneticThreads){
            gt.perform((index,populationPerThread)->{

                int threadIndex=index/populationPerThread;
                localMaxFitnessIndex[threadIndex]=index;

                for (int i = index; i < (index+populationPerThread) && !stopped; i++) {
                    try {
                        int[] violationCount = countConstraintViolation(i);
                        fitness[i] = 1f / (1f + violationCount[0]);
                        if (fitness[i] >= 1f) fitness[i] += 1f / (1f + violationCount[1]);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    localSum[threadIndex] += fitness[i];
                    if (fitness[i] > fitness[localMaxFitnessIndex[threadIndex]]) {
                        localMaxFitnessIndex[threadIndex] = i;
                    }
                }
            });
        }
        waitForAllThreads();
        for(int i=0;i<threadCount;i++){
            sum+=localSum[i];
            if(fitness[localMaxFitnessIndex[i]]>maxFitness){
                maxFitnessIndex=localMaxFitnessIndex[i];
                maxFitness=fitness[maxFitnessIndex];
            }
        }

        averageFitness = sum / populationSize;
    }

    private int[] countConstraintViolation(int index) throws IOException {
        int hCount = 0;
        int sCount = 0;

        HashSet<String> h4 = new HashSet<>();

        HashMap<String, Short> h5 = new HashMap<>();

        HashSet<String> h6 = new HashSet<>();

        HashSet<String> h7 = new HashSet<>();

        HashMap<String, List<short[]>> h89 = new HashMap<>();

        boolean[] h10 = new boolean[teacherNameArray.length];

        Scanner sc = populationStorage.getChromosomeReader(index);

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

                    //none other constraints required if subject is free
                    if (sub.isFree()) continue;

                    //evaluating h2
                    if (!teacherDao.get(teacher).getFreeTime().contains(new int[]{day, period}) && !teacherDao.get(teacher).getFreeTime().isEmpty())
                        hCount++;

                    //evaluating h4
                    if (subjectDao.get(subject).isPractical()) {
                        key = String.format("%d,%d,%s", day, period, roomCode);
                        if (h4.contains(key)) hCount++;
                        else h4.add(key);
                    }

                    //evaluating h5
                    else {
                        key = String.format("%d,%s", section, subject);
                        /*if (!h5.containsKey(key)) */
                        h5.put(key, teacherIndex);
                        //else if (h5.get(key) != teacherIndex) count++;
                    }

                    //evaluating h7
                    key = String.format("%d,%d,%d", teacherIndex, day, period);
                    if (h7.contains(key)) hCount++;
                    else h7.add(key);

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

        sc.close();

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

        return new int[]{hCount, sCount};
    }

    private void selectParents() {
        for (int i = 0; i < selectedIndices.length && !stopped; i++) {
            int max = i * tournamentSize;
            for (int j = 1; j < tournamentSize; j++) {
                if (fitness[i * tournamentSize + j] > fitness[max]) max = i * tournamentSize + j;
            }
            selectedIndices[i] = max;
        }
        if(!stopped)
            Arrays.sort(selectedIndices, (a, b) -> (fitness[b] - fitness[a]) < 0 ? -1 : (fitness[b] - fitness[a] > 0 ? 1 : 0));
    }

    private void generateNewPopulation() throws IOException {
        PopulationStorage prevPopulationStorage = populationStorage;
        populationStorage = new PopulationStorage(generation);
        final int noCrossLength=2;//Math.round(populationSize * (1 - crossoverRate));

        //copy the top individuals of previous generation as non-crossed individuals
        for (int ii = 0; ii < noCrossLength && !stopped; ii++) {
            PrintStream ps = populationStorage.getChromosomeWriter(ii);
            Scanner sc = prevPopulationStorage.getChromosomeReader(selectedIndices[ii]);
            while (sc.hasNextShort()) ps.println(sc.nextShort());
            ps.close();
            sc.close();
        }

        Random random = new Random();
        //add the crossed individuals from any two selected parents with mutation
        for (GeneticThread gt:geneticThreads){
            gt.perform((index,populationPerThread)->{
                for (int ind=noCrossLength+index/populationPerThread; ind < populationSize && !stopped; ind+=threadCount) {
                    //select two random indices to cross
                    int ind1 = selectedIndices[random.nextInt(selectedIndices.length)];
                    int ind2 = selectedIndices[random.nextInt(selectedIndices.length)];
                    Scanner sc1;
                    Scanner sc2;
                    PrintStream ps;
                    try {
                        sc1 = prevPopulationStorage.getChromosomeReader(ind1);
                        sc2 = prevPopulationStorage.getChromosomeReader(ind2);
                        ps = populationStorage.getChromosomeWriter(ind);
                    } catch (IOException e) {
                        System.out.println(e);
                        throw new RuntimeException(e);
                    }
                    for (int i = 0; i < subjectCodeArray.length && !stopped; i++) {
                        Subject sub = subjectDao.get(subjectCodeArray[i]);
                        byte secCount = scheduleData.getSectionCount(sub.getSem());
                        boolean practical = sub.isPractical();

                        for (byte sec = 1; sec <= secCount && !stopped; sec++) {
                            short teacher1;
                            short teacher2;
                            short val1;
                            short val2;
                            boolean mutate = Math.random() <= mutationRate;
                            int lectureCount = sub.getLectureCount();
                            if (practical) {
                                val1 = sc1.nextShort();
                                val2 = sc2.nextShort();
                                if (mutate) {
                                    byte period = Util.getPracticalStartingPeriodLocation(subjectCodeArray[i]);
                                    ps.println(DayPeriod.getCompact((byte) random.nextInt(5), period));
                                } else {
                                    ps.println(random.nextBoolean() ? val1 : val2);
                                }

                                if(!sub.isFree()) {
                                    short room1 = sc1.nextShort();
                                    short room2 = sc2.nextShort();
                                    if (mutate) {
                                        ArrayList<String> roomCodes = sub.getRoomCodes();
                                        String roomCode = roomCodes.get(random.nextInt(roomCodes.size()));
                                        ps.println(indexOfRoom.get(roomCode));
                                    } else {
                                        ps.println(random.nextBoolean() ? room1 : room2);
                                    }
                                }
                            } else if (!sub.isFree()) {
                                teacher1 = sc1.nextShort();
                                teacher2 = sc2.nextShort();
                                if (mutate) {
                                    short teacher = teachersForSubjects[i].get(random.nextInt(teachersForSubjects[i].size())).shortValue();
                                    ps.println(teacher);
                                } else {
                                    ps.println(random.nextBoolean() ? teacher1 : teacher2);
                                }
                            }

                            for (int j = 0; j < lectureCount && !stopped; j++) {
                                if (practical) {
                                    if (!sub.isFree()) {
                                        teacher1 = sc1.nextShort();
                                        teacher2 = sc2.nextShort();
                                        if (mutate) {
                                            short teacher = teachersForSubjects[i].get(random.nextInt(teachersForSubjects[i].size())).shortValue();
                                            ps.println(teacher);
                                        } else {
                                            ps.println(random.nextBoolean() ? teacher1 : teacher2);
                                        }
                                    }
                                } else {
                                    val1 = sc1.nextShort();
                                    val2 = sc2.nextShort();
                                    if (mutate) {
                                        byte period = getRandomExcluding(scheduleData.getPeriodCount(), scheduleData.getBreakLocations(sub.getSem()), random);
                                        ps.println(DayPeriod.getCompact((byte) random.nextInt(5), period));
                                    } else {
                                        ps.println(random.nextBoolean() ? val1 : val2);
                                    }
                                }
                            }
                        }
                    }
                    sc1.close();
                    sc2.close();
                    ps.close();
                }
            });
        }
        waitForAllThreads();
        /*for (; index < populationSize && !stopped; index++) {
            //select two random indices to cross
            int ind1 = selectedIndices[random.nextInt(selectedIndices.length)];
            int ind2 = selectedIndices[random.nextInt(selectedIndices.length)];
            Scanner sc1 = prevPopulationStorage.getChromosomeReader(ind1);
            Scanner sc2 = prevPopulationStorage.getChromosomeReader(ind2);
            PrintStream ps = populationStorage.getChromosomeWriter(index);
            for (int i = 0; i < subjectCodeArray.length && !stopped; i++) {
                Subject sub = subjectDao.get(subjectCodeArray[i]);
                byte secCount = scheduleData.getSectionCount(sub.getSem());
                boolean practical = sub.isPractical();

                for (byte sec = 1; sec <= secCount && !stopped; sec++) {
                    short teacher1;
                    short teacher2;
                    short val1;
                    short val2;
                    boolean mutate = Math.random() <= mutationRate;
                    int lectureCount = sub.getLectureCount();
                    if (practical) {
                        val1 = sc1.nextShort();
                        val2 = sc2.nextShort();
                        if (mutate) {
                            short period = getRandomExcludingTrailing(scheduleData.getPeriodCount(), scheduleData.getBreakLocations(sub.getSem()), (short) lectureCount, random);
                            ps.println((short) (random.nextInt(5) * 10 + period));
                        } else {
                            ps.println(random.nextBoolean() ? val1 : val2);
                        }
                    } else {
                        teacher1 = sc1.nextShort();
                        teacher2 = sc2.nextShort();
                        if (mutate) {
                            short teacher = teachersForSubjects[i].get(random.nextInt(teachersForSubjects[i].size())).shortValue();
                            ps.println(teacher);
                        } else {
                            ps.println(random.nextBoolean() ? teacher1 : teacher2);
                        }
                    }

                    for (int j = 0; j < lectureCount && !stopped; j++) {
                        if (practical) {
                            teacher1 = sc1.nextShort();
                            teacher2 = sc2.nextShort();
                            if (mutate) {
                                short teacher = teachersForSubjects[i].get(random.nextInt(teachersForSubjects[i].size())).shortValue();
                                ps.println(teacher);
                            } else {
                                ps.println(random.nextBoolean() ? teacher1 : teacher2);
                            }
                        } else {
                            val1 = sc1.nextShort();
                            val2 = sc2.nextShort();
                            if (mutate) {
                                short period = getRandomExcluding(scheduleData.getPeriodCount(), scheduleData.getBreakLocations(sub.getSem()), random);
                                ps.println((short) (random.nextInt(5) * 10 + period));
                            } else {
                                ps.println(random.nextBoolean() ? val1 : val2);
                            }
                        }
                    }
                }
            }
            sc1.close();
            sc2.close();
            ps.close();
        }*/
    }

    private byte getRandomExcluding(short upperBound, byte[] exclude, Random rand) {
        byte random = (byte) rand.nextInt(upperBound - exclude.length);
        for (byte ex : exclude)
            if (ex - 1 <= random) random++;
        return random;
    }

    public void waitForAllThreads(){
        for(int i=0;i<threadCount;i++){
            while(!geneticThreads.get(i).isCompleted());
        }
    }

    public void stop() {
        this.stopped = true;
    }
}
