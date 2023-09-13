package org.example.algorithms;

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
    private final int populationSize=200;
    private final int tournamentSize=5;
    private final float crossoverRate=0.9f;
    private final float mutationRate=0.05f;
    private int chromoLength=0;
    private String[] subjectCodeArray=null;
    private String[] teacherNameArray=null;
    private HashMap<String,Short> indexOfSubject=null;
    ArrayList<Integer>[] teachersForSubjects=null;
    private PopulationStorage populationStorage;
    private final float[] fitness;
    private final Integer[] selectedIndices;
    private float averageFitness=0;
    private float maxFitness=0;
    private int maxFitnessIndex=0;
    private int generation = 0;
    private final OnResultListener onResultListener;
    SubjectDao subjectDao=SubjectDao.getInstance();
    TeacherDao teacherDao=TeacherDao.getInstance();
    ScheduleStructure scheduleData= ScheduleStructure.getInstance();
    boolean stopped=false;

    public Generator(OnResultListener onResultListener){
        fitness=new float[populationSize];
        selectedIndices=new Integer[populationSize/tournamentSize];
        System.out.println(selectedIndices.length+", "+populationSize*(1-crossoverRate));
        this.onResultListener=onResultListener;
    }

    public void generate() {
        stopped=false;
        new Thread(()->{
            try{
                updateVariables();
                populate();
                calculateFitness();
                System.out.println("Generation:"+generation+" Avg. fitness:"+averageFitness+" Max fitness:"+maxFitness+" Index: "+maxFitnessIndex);
                while(maxFitness!=1 && generation<=500){
                    selectParents();
                    generateNewPopulation();
                    calculateFitness();
                    generation++;
                    System.out.println("Generation:"+generation+" Avg. fitness:"+averageFitness+" Max fitness:"+maxFitness+" Index: "+maxFitnessIndex);
                }
                System.out.println("Max Fitness Index = "+maxFitnessIndex);
                if(generation>500 && !stopped)onResultListener.onError("Couldn't find stable time table with given constraints");
                else{
                    Scanner sc=populationStorage.getChromosomeReader(maxFitnessIndex);
                    ScheduleSolution.getInstance().parseChromo(sc,subjectCodeArray,teacherNameArray);
                    onResultListener.onResult();
                }
            }catch (IOException e){
                System.out.println(e);
                e.printStackTrace();
                onResultListener.onError(e.getMessage());
            }
        }).start();
    }

    private void updateVariables(){
        //update teacherNameArray and subjectCodeArray
        this.subjectCodeArray= subjectDao.keySet().toArray(String[]::new);
        this.teacherNameArray= teacherDao.keySet().toArray(String[]::new);

        //update indexOfSubject
        this.indexOfSubject=new HashMap<>();
        for(short i=0;i<subjectCodeArray.length;i++)
            indexOfSubject.put(subjectCodeArray[i],i);

        //update teachersForSubjects
        this.teachersForSubjects=new ArrayList[subjectCodeArray.length];

        for(int i=0;i<teachersForSubjects.length;i++)
            teachersForSubjects[i]=new ArrayList<>();

        for(int i=0;i<teacherNameArray.length;i++) {
            for (String code : teacherDao.get(teacherNameArray[i]).getSubjects())
                teachersForSubjects[indexOfSubject.get(code)].add(i);
        }

        for(int i=0;i<teachersForSubjects.length;i++)
            if(teachersForSubjects[i].isEmpty()){
                onResultListener.onError("Subject: "+subjectCodeArray[i]+" has no teacher");
                stop();
            }

        //update chromoLength
        chromoLength=0;
        for(String subject:subjectCodeArray){
            Subject s=subjectDao.get(subject);
            int secCount=scheduleData.getSectionCount(s.getSem());
            int lectureCount=s.getLectureCount();
            chromoLength+=(s.isPractical()?lectureCount*2:lectureCount+1)*secCount;
        }
    }

    private void populate() throws IOException {
        populationStorage=new PopulationStorage(generation);
        for(int i=0;i<populationSize && !stopped;i++){
            generateRandomChromosome(i);
        }
        generation++;
    }

    //chromosome format: {day(0-4) period(0-periodCount-1), section(0-sectionCount), teacherIndex, subjectIndex}
    //New chromosome format: {sub1:{sec1{teachInd1,day period1, day period2}},{sec1{teachInd1,day period1, day period2}}}
    //Ex : {13,33,0,4}
    private void generateRandomChromosome(int index) throws IOException{
        PrintStream ps=populationStorage.getChromosomeWriter(index);
        Random random=new Random();

        for(int i=0;i<subjectCodeArray.length;i++){
            byte sem=(byte)subjectDao.get(subjectCodeArray[i]).getSem();
            for(byte sec=1;sec<=scheduleData.getSectionCount(sem);sec++){
                int lectureCount=subjectDao.get(subjectCodeArray[i]).getLectureCount();
                if(!subjectDao.get(subjectCodeArray[i]).isPractical()){
                    short teacher=teachersForSubjects[i].get(random.nextInt(teachersForSubjects[i].size())).shortValue();
                    ps.println(teacher);
                }
                for(int k=0;k<lectureCount;k++){
                    if(subjectDao.get(subjectCodeArray[i]).isPractical()){
                        short teacher=teachersForSubjects[i].get(random.nextInt(teachersForSubjects[i].size())).shortValue();
                        ps.println(teacher);
                    }
                    short period=getRandomExcluding(scheduleData.getPeriodCount(),scheduleData.getBreakLocations(sem),random);
                    ps.println((short)(random.nextInt(5)*10+period));
                }
            }
        }
        /*for(int i=0;i<this.totalLectureCount && !stopped;i++){
            writeRandomGene(ps,random);
        }*/
        ps.close();
    }

    public void writeRandomGene(PrintStream ps,Random random){
        short i3=(short) random.nextInt(teacherNameArray.length);
        String[] subjects=TeacherDao.getInstance().get(teacherNameArray[i3]).getSubjects().toArray(String[]::new);
        short i4=indexOfSubject.get(subjects[random.nextInt(subjects.length)]);
        short sem=(short) subjectDao.get(subjectCodeArray[i4]).getSem();
        short i2=(short) random.nextInt(scheduleData.getSectionCount(sem));
        short period=getRandomExcluding(scheduleData.getPeriodCount(),scheduleData.getBreakLocations(sem),random);
        short i1=(short)(random.nextInt(5)*10+period);
        ps.println(i1);
        ps.println(i2);
        ps.println(i3);
        ps.println(i4);
    }

    private void calculateFitness()throws IOException{
        float sum=0f;
        maxFitness=0;
        for(int i=0;i<populationSize && !stopped;i++){
            fitness[i]=1f/(1f+countHardConstraintViolation(i));
            sum+=fitness[i];
            if(fitness[i]>maxFitness){
                maxFitness=fitness[i];
                maxFitnessIndex=i;
            }
        }
        averageFitness=sum/populationSize;
    }

    private int countHardConstraintViolation(int index) throws IOException {
        int count=0;

        HashMap<String,Integer> h3=new HashMap<>();

        HashSet<String> h4=new HashSet<>();

        HashMap<String,Short> h5=new HashMap<>();

        HashSet<String> h6=new HashSet<>();

        HashSet<String> h7=new HashSet<>();

        HashMap<String,List<short[]>> h89=new HashMap<>();

        boolean[] h10=new boolean[teacherNameArray.length];

        Scanner sc=populationStorage.getChromosomeReader(index);

        for(short subjectIndex=0;subjectIndex<subjectCodeArray.length;subjectIndex++){
            String subject=subjectCodeArray[subjectIndex];
            Subject sub=subjectDao.get(subject);
            short semester=(short)sub.getSem();
            byte secCount=scheduleData.getSectionCount(semester);

            for(short section=1;section<=secCount;section++){
                short teacherIndex=0;
                String teacher=null;
                if(!sub.isPractical()){
                    teacherIndex=sc.nextShort();
                    teacher=teacherNameArray[teacherIndex];
                }
                int lectureCount=sub.getLectureCount();

                for(int j=0;j<lectureCount;j++){
                    if(sub.isPractical()){
                        teacherIndex=sc.nextShort();
                        teacher=teacherNameArray[teacherIndex];
                    }
                    short value=sc.nextShort();
                    short day=(short)(value/10+1);
                    short period=(short)(value%10+1);

                    //evaluating h2
                    if(!teacherDao.get(teacher).getFreeTime().contains(new int[]{day,period}) && !teacherDao.get(teacher).getFreeTime().isEmpty())
                        count++;

                    //processing h3
                    String key=String.format("%s,%d",subject,section);
                    if(!h3.containsKey(key))h3.put(key,1);
                    else h3.put(key,h3.get(key)+1);

                    //evaluating h4
                    if(subjectDao.get(subject).isPractical()){
                        key=String.format("%d,%d,%s",day,period,subjectDao.get(subject).getRoomCode());
                        if(h4.contains(key))count++;
                        else h4.add(key);
                    }

                    //evaluating h5
                    else{
                        key=String.format("%d,%d",section,subjectIndex);
                        if(!h5.containsKey(key))h5.put(key, teacherIndex);
                        else if(h5.get(key)!=teacherIndex)count++;
                    }

                    //evaluating h6
                    key=String.format("%d,%d,%d,%d",day,period,semester,section);
                    if(h6.contains(key))count++;
                    else h6.add(key);

                    //evaluating h7
                    key=String.format("%d,%d,%d",teacherIndex,day,period);
                    if(h7.contains(key))count++;
                    else h7.add(key);

                    //processing h8 and h9
                    if(subjectDao.get(subject).isPractical()){
                        key=String.format("%d,%s",section,subject);
                        if(!h89.containsKey(key))
                            h89.put(key,new ArrayList<>());
                        h89.get(key).add(new short[]{day,period,teacherIndex});
                    }

                    //processing h10
                    h10[teacherIndex]=true;

                    //evaluating h11
                    if(!teacherDao.get(teacher).getSubjects().contains(subject))count++;
                }
            }
        }

        sc.close();

        //evaluating h3
        for(String subject:subjectCodeArray){
            for(int i = 1; i<= scheduleData.getSectionCount(subjectDao.get(subject).getSem()); i++){
                if(!h3.containsKey(String.format("%s,%d",subject,i)))
                    count+=subjectDao.get(subject).getLectureCount();
                else
                    count+=Math.abs(subjectDao.get(subject).getLectureCount()-h3.get(String.format("%s,%d",subject,i)));
            }
        }

        //evaluating h8 and h9
        for(String key:h89.keySet()){
            List<short[]> slots=h89.get(key);
            HashSet<Short> teachers=new HashSet<>();

            //finding count of slot with different days
            float sum=0;
            for(short[] slot:slots)sum+=slot[0];
            float mean=sum / slots.size();
            sum=0;
            for(short[] slot:slots)sum+=Math.abs(slot[0]-mean);
            count+=Math.round(sum);

            slots.sort(Comparator.comparingInt(a -> a[1]));
            for(byte i=1;i< slots.size();i++)
                if(slots.get(i)[1]-1!=slots.get(i-1)[1])count++;

            for(short[] slot:slots)teachers.add(slot[2]);
            for(Short teacherIndex:teachers){
                for(short[] slot:slots){
                    if(slot[2]==teacherIndex)continue;
                    if(h7.contains(String.format("%d,%d,%d",teacherIndex,slot[0],slot[1])))count++;
                }
            }
        }

        //evaluating h10
        for(boolean b:h10)
            if(!b) count++;

        return count;
    }

    private void selectParents(){
        for(int i=0;i<selectedIndices.length && !stopped;i++){
            int max=i*tournamentSize;
            for(int j=1;j<tournamentSize;j++) {
                if (fitness[i * tournamentSize + j] > fitness[max]) max = i * tournamentSize + j;
            }
            selectedIndices[i]=max;
        }
        Arrays.sort(selectedIndices,(a,b)->(fitness[b]-fitness[a])<0?-1:(fitness[b]-fitness[a]>0?1:0));
    }

    private void generateNewPopulation() throws IOException {
        PopulationStorage prevPopulationStorage=populationStorage;
        populationStorage=new PopulationStorage(generation);
        int index=0;

        //copy the top individuals of previous generation as non-crossed individuals
        for(;index<Math.round(populationSize*(1-crossoverRate)) && !stopped;index++){
            PrintStream ps=populationStorage.getChromosomeWriter(index);
            Scanner sc=prevPopulationStorage.getChromosomeReader(selectedIndices[index]);
            for(int i=0;i<chromoLength;i++) ps.println(sc.nextShort());
            ps.close();
            sc.close();
        }

        Random random=new Random();
        //add the crossed individuals from any two selected parents
        for (;index<populationSize && !stopped;index++){
            //select two random indices to cross
            int ind1=selectedIndices[random.nextInt(selectedIndices.length)];
            int ind2=selectedIndices[random.nextInt(selectedIndices.length)];
            Scanner sc1=prevPopulationStorage.getChromosomeReader(ind1);
            Scanner sc2=prevPopulationStorage.getChromosomeReader(ind2);
            PrintStream ps=populationStorage.getChromosomeWriter(index);
            for(int i=0;i<subjectCodeArray.length;i++){
                Subject sub=subjectDao.get(subjectCodeArray[i]);
                byte secCount = scheduleData.getSectionCount(sub.getSem());
                boolean practical=sub.isPractical();

                for (byte sec=1;sec<=secCount;sec++){
                    short teacher1;
                    short teacher2;
                    if(!practical){
                        teacher1=sc1.nextShort();
                        teacher2=sc2.nextShort();
                        if(Math.random()<=mutationRate){
                            short teacher=teachersForSubjects[i].get(random.nextInt(teachersForSubjects[i].size())).shortValue();
                            ps.println(teacher);
                        } else {
                            ps.println(random.nextBoolean()?teacher1:teacher2);
                        }
                    }
                    int lectureCount=sub.getLectureCount();
                    for(int j=0;j<lectureCount;j++){
                        if(practical){
                            teacher1=sc1.nextShort();
                            teacher2=sc2.nextShort();
                            if(Math.random()<=mutationRate){
                                short teacher=teachersForSubjects[i].get(random.nextInt(teachersForSubjects[i].size())).shortValue();
                                ps.println(teacher);
                            } else {
                                ps.println(random.nextBoolean()?teacher1:teacher2);
                            }
                        }
                        short val1=sc1.nextShort();
                        short val2=sc2.nextShort();
                        if(Math.random()<=mutationRate){
                            short period=getRandomExcluding(scheduleData.getPeriodCount(),scheduleData.getBreakLocations(sub.getSem()),random);
                            ps.println((short)(random.nextInt(5)*10+period));
                        } else {
                            ps.println(random.nextBoolean()?val1:val2);
                        }
                    }
                }
            }
            /*for(int i=0;i<totalLectureCount;i++){
                short s;
                if(Math.random()>mutationRate) {
                    boolean choice = random.nextBoolean();
                    for (byte b = 0; b < 4; b++) {
                        if (choice) {
                            s = sc1.nextShort();
                            sc2.nextShort();
                        } else {
                            s = sc2.nextShort();
                            sc1.nextShort();
                        }
                        ps.println(s);
                    }
                }
                else writeRandomGene(ps,random);
            }*/
            sc1.close();
            sc2.close();
            ps.close();
        }
    }

    private short getRandomExcluding(short upperBound,byte[] exclude, Random rand){
        short random=(short) rand.nextInt(upperBound-exclude.length);
        for (byte ex:exclude)
            if(ex-1<=random)random++;
        return random;
    }

    public void stop(){
        this.stopped=true;
    }
}
