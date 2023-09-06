package org.example.algorithms;

import org.example.dao.SubjectDao;
import org.example.dao.TeacherDao;
import org.example.interfaces.OnResultListener;
import org.example.pojo.ScheduleStructure;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public class Generator {
    private final int populationSize=100;
    private final int tournamentSize=5;
    private final float crossoverRate=0.9f;
    private final float mutationRate=0.01f;

    private int totalLectureCount=0;
    private String[] subjectCodeArray=null;
    private String[] teacherNameArray=null;
    private PopulationStorage populationStorage;
    private final float[] fitness;
    private final Integer[] selectedIndices;
    private float averageFitness=0;
    private float maxFitness=0;
    private int generation = 0;
    final private Random random=new Random();
    private final OnResultListener onResultListener;
    SubjectDao subjectDao=SubjectDao.getInstance();
    TeacherDao teacherDao=TeacherDao.getInstance();
    ScheduleStructure scheduleData= ScheduleStructure.getInstance();
    boolean stopped=false;

    public Generator(OnResultListener onResultListener){
        fitness=new float[populationSize];
        selectedIndices=new Integer[populationSize/tournamentSize];
        this.onResultListener=onResultListener;
    }

    public void generate() {
        stopped=false;
        new Thread(()->{
            try{
                updateVariables();
                populate();
                while(maxFitness!=1 && generation<=2000){
                    calculateFitness();
                    selectParents();
                    generateNewPopulation();
                    generation++;
                }
                if(generation>2000)onResultListener.onError("Couldn't find stable time table with given constraints");
            }catch (IOException e){
                onResultListener.onError(e.getMessage());
            }
        }).start();
    }

    private void updateVariables(){
        //update teacherNameArray and subjectCodeArray
        this.subjectCodeArray= subjectDao.keySet().toArray(String[]::new);
        this.teacherNameArray= teacherDao.keySet().toArray(String[]::new);

        //update totalLectureCount and practicalSubjects
        totalLectureCount= scheduleData.getTotalLectureCount();
    }

    private void populate() throws IOException {
        populationStorage=new PopulationStorage(generation);
        for(int i=0;i<populationSize && !stopped;i++){
            generateRandomChromosome(i);
        }
        generation++;
    }

    //chromosome format: {day(0-4) period(0-periodCount-1), section(0-sectionCount), teacherIndex, subjectIndex}
    //Ex : {13,33,0,4}
    private void generateRandomChromosome(int index) throws IOException{
        PrintStream ps=populationStorage.getChromosomeWriter(index);

        for(int i=0;i<this.totalLectureCount && !stopped;i++){
            short i4=(short) random.nextInt(subjectCodeArray.length);
            short sem=(short) subjectDao.get(subjectCodeArray[i4]).getSem();
            short i2=(short) random.nextInt(scheduleData.getSectionCount(sem));
            short period=getRandomExcluding(scheduleData.getPeriodCount(),scheduleData.getBreakLocations(sem));
            short i1=(short)(random.nextInt(5)*10+period);
            short i3=(short) random.nextInt(teacherNameArray.length);
            ps.println(i1);
            ps.println(i2);
            ps.println(i3);
            ps.println(i4);
        }
        ps.close();
    }

    private void calculateFitness()throws IOException{
        float sum=0f;
        maxFitness=0;
        for(int i=0;i<populationSize && !stopped;i++){
            fitness[i]=1f/(1f+countHardConstraintViolation(i));
            sum+=fitness[i];
            if(fitness[i]>maxFitness)maxFitness=fitness[i];
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

        for(int i=0;i<this.totalLectureCount && !stopped;i++){
            short i1=sc.nextShort();
            short i2=sc.nextShort();
            short i3=sc.nextShort();
            short i4=sc.nextShort();
            short day=(short)(i1/10+1);
            short period=(short)(i1%10+1);
            short semester=(short) subjectDao.get(subjectCodeArray[i4]).getSem();
            short section=i2;
            short teacherIndex= i3;
            short subjectIndex=i4;
            String teacher=teacherNameArray[i3];
            String subject=subjectCodeArray[i4];

            //evaluating h2
            if(teacherDao.get(teacher).getFreeTime().contains(Arrays.asList(day,period)) || teacherDao.get(teacher).getFreeTime().isEmpty())
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
                    h89.put(subject,new ArrayList<>());
                h89.get(key).add(new short[]{day,period,teacherIndex});
            }

            //processing h10
            h10[teacherIndex]=true;

            //evaluating h11
            if(teacherDao.get(teacher).getSubjects().contains(subjectDao.get(subject)))count++;
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
            String subject=key.substring(key.indexOf(',')+1);
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
            int max=random.nextInt(populationSize);
            for(int j=1;j<tournamentSize;j++){
                int next=random.nextInt(populationSize);
                if(fitness[next]>fitness[max])max=next;
            }
            selectedIndices[i]=max;
            fitness[max]=-fitness[max];
        }
        Arrays.sort(selectedIndices,(a,b)->(int)(-(fitness[b]-fitness[a])/Math.abs(fitness[a]-fitness[b])));
    }

    private void generateNewPopulation() throws IOException {
        PopulationStorage prevPopulationStorage=populationStorage;
        populationStorage=new PopulationStorage(generation);
        int index=0;

        //copy the top individuals of previous generation as non-crossed individuals
        for(;index<populationSize*(1-crossoverRate) && !stopped;index++){
            PrintStream ps=populationStorage.getChromosomeWriter(index);
            Scanner sc=prevPopulationStorage.getChromosomeReader(selectedIndices[index]);
            for(int i=0;i<totalLectureCount*4;i++) ps.println(sc.nextShort());
            ps.close();
            sc.close();
        }

        int mutationCount=(int)(populationSize*mutationRate);
        //add the crossed individuals from any two selected parents
        for (;index<populationSize-mutationCount && !stopped;index++){
            //select two random indices to cross
            int ind1=selectedIndices[random.nextInt(selectedIndices.length)];
            int ind2=selectedIndices[random.nextInt(selectedIndices.length)];
            Scanner sc1=prevPopulationStorage.getChromosomeReader(ind1);
            Scanner sc2=prevPopulationStorage.getChromosomeReader(ind2);
            PrintStream ps=populationStorage.getChromosomeWriter(index);
            for(int i=0;i<totalLectureCount;i++){
                short s=0;
                boolean ch=random.nextBoolean();
                for(byte b=0;b<4;b++){
                    if(ch){s=sc1.nextShort();sc2.nextShort();}
                    else {s=sc2.nextShort();sc1.nextShort();}
                    ps.println(s);
                }
            }
            sc1.close();
            sc2.close();
            ps.close();
        }

        //add mutated individuals to the population
        for(;index<populationSize;index++)
            generateRandomChromosome(index);
    }

    private short getRandomExcluding(short upperBound,byte[] exclude){
        short random=(short) this.random.nextInt(upperBound-exclude.length);
        for (byte ex:exclude)
            if(random<ex-1)random++;
        return random;
    }

    public void stop(){
        this.stopped=true;
    }
}
