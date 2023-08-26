package org.example.algorithms;

import org.example.dao.SubjectDao;
import org.example.dao.TeacherDao;
import org.example.pojo.Schedule;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public class Generator {
    private final int populationSize=100;
    private int totalLectureCount=0;
    private String[] subjectCodeArray=null;
    private String[] teacherNameArray=null;
    private PopulationStorage populationStorage;
    private double[] fitness=null;
    private double averageFitness=0;
    private int maxFitnessIndex=-1;
    private int generation = 0;
    final private Random random=new Random();
    SubjectDao subjectDao=SubjectDao.getInstance();
    TeacherDao teacherDao=TeacherDao.getInstance();

    public Generator(){
        fitness=new double[populationSize];
    }

    public void generate() throws IOException {
        updateVariables();
        populate();
        calculatePopulationFitness();
    }

    private void updateVariables(){
        //update teacherNameArray and subjectCodeArray
        this.subjectCodeArray= subjectDao.keySet().toArray(String[]::new);
        this.teacherNameArray= teacherDao.keySet().toArray(String[]::new);

        //update totalLectureCount and practicalSubjects
        totalLectureCount= Schedule.getInstance().getTotalLectureCount();
    }

    //chromosome format: {day(0-4) period(0-periodCount-1), section(0-sectionCount), teacherIndex, subjectIndex}
    //Ex : {13,33,0,4}
    private void populate() throws IOException {
        populationStorage=new PopulationStorage(generation);
        for(int i=0;i<populationSize;i++){
            generateRandomChromosome(i);
        }
        generation++;
    }

    private void generateRandomChromosome(int index) throws IOException{
        PrintStream ps=populationStorage.getChromosomeWriter(index);

        for(int i=0;i<this.totalLectureCount;i++){
            Schedule s=Schedule.getInstance();
            short i4=(short) random.nextInt(subjectCodeArray.length);
            short sem=(short) subjectDao.get(subjectCodeArray[i4]).getSem();
            short i2=(short) random.nextInt(s.getSectionCount(sem));
            short i1=(short)(random.nextInt(5)*10+random.nextInt(s.getPeriodCount(sem)));
            short i3=(short) random.nextInt(teacherNameArray.length);
            ps.println(i1);
            ps.println(i2);
            ps.println(i3);
            ps.println(i4);
        }
        ps.close();
    }

    public void calculatePopulationFitness()throws IOException{
        double sum=0d;
        for(int i=0;i<populationSize;i++){
            fitness[i]=calculateFitness(i);
            sum+=fitness[i];
            if(fitness[i]>fitness[maxFitnessIndex])maxFitnessIndex=i;
        }
        averageFitness=sum/populationSize;
    }

    public double calculateFitness(int index) throws IOException {
        return 1d/(1d+countHardConstraintViolation(index));
    }

    public int countHardConstraintViolation(int index) throws IOException {
        int count=0;

        HashMap<String,Integer> h3=new HashMap<>();

        HashSet<String> h4=new HashSet<>();

        HashMap<String,Short> h5=new HashMap<>();

        HashSet<String> h6=new HashSet<>();

        HashSet<String> h7=new HashSet<>();

        HashMap<String,List<short[]>> h89=new HashMap<>();

        boolean[] h10=new boolean[teacherNameArray.length];

        Scanner sc=populationStorage.getChromosomeReader(index);

        for(int i=0;i<this.totalLectureCount;i++){
            short i1=sc.nextShort();
            short i2=sc.nextShort();
            short i3=sc.nextShort();
            short i4=sc.nextShort();
            short day=(short)(i1/10);
            short period=(short)(i1%10);
            short semester=(short) subjectDao.get(subjectCodeArray[i4]).getSem();
            short section=i2;
            short teacherIndex= i3;
            short subjectIndex=i4;
            String teacher=teacherNameArray[i3];
            String subject=subjectCodeArray[i4];

            //evaluating h2
            if(teacherDao.get(teacher).getFreeTime().contains(Arrays.asList(day,period)))
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
            for(int i=1;i<=Schedule.getInstance().getSectionCount(subjectDao.get(subject).getSem());i++){
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

            float sum=0;
            for(short[] slot:slots)sum+=slot[0];
            float mean=sum / slots.size();
            sum=0;
            for(short[] slot:slots)sum+=Math.abs(slot[0]-mean);
            count+=Math.round(sum);

            slots.sort(Comparator.comparingInt(a -> a[1]));
            for(byte i=1;i< slots.size();i++)
                if(slots.get(i)[1]-1!=slots.get(i-1)[1])count++;
            short breakLocation=(short)Schedule.getInstance().getBreakLocation(subjectDao.get(subject).getSem());
            short start=slots.get(0)[1],end=slots.get(slots.size()-1)[1];
            if(start<=breakLocation && end>breakLocation)
                count+=Math.min(breakLocation-start+1,end-breakLocation);

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

}
