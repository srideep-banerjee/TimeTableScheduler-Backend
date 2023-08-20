package org.example.algorithms;

import org.example.dao.SubjectDao;
import org.example.dao.TeacherDao;
import org.example.pojo.Subject;

import java.util.*;

public class Generator {
    private final int populationSize=100;
    private byte sectionCount = 3;
    private byte semesterCount = 4;
    private int totalLectureCount=0;
    private String[] subjectCodeArray=null;
    private String[] teacherNameArray=null;
    private String[] practicalSubjects=null;
    private short[][] population=null;
    final private Random random=new Random();
    SubjectDao subjectDao=SubjectDao.getInstance();
    TeacherDao teacherDao=TeacherDao.getInstance();

    public Generator(byte sectionCount, byte semesterCount) {
        this.sectionCount = sectionCount;
        this.semesterCount = semesterCount;
    }

    public Generator() {
    }

    public void generate() {
        updateVariables();
        populate();
    }

    private void updateVariables(){
        //update teacherNameArray and subjectCodeArray
        this.subjectCodeArray= subjectDao.keySet().toArray(String[]::new);
        this.teacherNameArray= teacherDao.keySet().toArray(String[]::new);

        //update totalLectureCount and practicalSubjects
        Subject subject;
        ArrayList<String> practicals=new ArrayList<>();
        for(String subjectCode: subjectCodeArray) {
            subject = subjectDao.get(subjectCode);
            totalLectureCount += subject.getLectureCount() * sectionCount;
            if(subject.isPractical()){
                practicals.add(subjectCode);
            }
        }
        this.practicalSubjects=practicals.toArray(String[]::new);
    }

    //chromosome format: {day(1-5) period(1-8), semester(1-semesterCount) section(1-sectionCount), teacherIndex, subjectIndex}
    //Ex : {13,33,0,4}
    private void populate(){
        this.population=new short[this.populationSize][this.totalLectureCount*4];
        for(int i=0;i<populationSize;i++)
            population[i]=generateRandomChromosome();
    }

    private short[] generateRandomChromosome(){
        short[] chromo=new short[this.totalLectureCount*4];
        for(int i=0;i<this.totalLectureCount;i++){
            chromo[i]=(short)(random.nextInt(5)*10+random.nextInt(8));
            chromo[i+1]=(short) (random.nextInt(semesterCount)*10+random.nextInt(sectionCount));
            chromo[i+2]=(short) random.nextInt(teacherNameArray.length);
            chromo[i+3]=(short) random.nextInt(subjectCodeArray.length);
        }
        return chromo;
    }

    public void calculateFitness(short[] chromo){

    }

    public int countHardConstraintViolation(short[] chromo){
        int count=0;

        HashMap<String,Integer>[] h3=new HashMap[this.sectionCount];
        for(int i=0;i<h3.length;i++)
            h3[i]=new HashMap<>();

        HashSet<String> h4=new HashSet<>();

        HashMap<String,Short> h5=new HashMap<>();

        HashSet<String> h6=new HashSet<>();

        HashSet<String>[] h7=new HashSet[teacherNameArray.length];

        for(int i=0;i<this.totalLectureCount;i++){
            short day=(short)(chromo[i]/10);
            short period=(short)(chromo[i]%10);
            short semester=(short)(chromo[i+1]/10);
            short section=(short)(chromo[i+1]%10);
            short teacherIndex= chromo[i+2];
            short subjectIndex=chromo[i+3];
            String teacher=teacherNameArray[chromo[i+2]];
            String subject=subjectCodeArray[chromo[i+3]];

            //evaluating h2
            if(teacherDao.get(teacher).getFreeTime().contains(Arrays.asList(day,period)))
                count++;

            //processing h3
            if(!h3[section].containsKey(subject))h3[section].put(subject,1);
            else h3[section].put(subject,h3[section].get(subject)+1);

            //evaluating h4
            if(subjectDao.get(subject).isPractical()){
                String key=String.format("%d,%d,%s",day,period,subjectDao.get(subject).getRoomCode());
                if(h4.contains(key))count++;
                else h4.add(key);
            }

            //evaluating h5
            else{
                String key=String.format("%d,%d",section,subjectIndex);
                if(!h5.containsKey(key))h5.put(key, teacherIndex);
                else if(h5.get(key)!=teacherIndex)count++;
            }

            //evaluating h6
            String key=String.format("%d,%d,%d,%d",day,period,semester,section);
            if(h6.contains(key))count++;
            else h6.add(key);

            //evaluating h7
            if(h7[teacherIndex]==null){
                h7[teacherIndex]=new HashSet<String>(0);
                h7[teacherIndex].add(String.format("%d,%d",day,period));
            }else if(!h7[teacherIndex].contains(String.format("%d,%d",day,period)))count++;
            else h7[teacherIndex].add(String.format("%d,%d",day,period));

            //processing h8
        }
        //evaluating h3
        for(int i=0;i<sectionCount;i++){
            for(String subject:subjectCodeArray){
                if(!h3[i].containsKey(subject))
                    count+=subjectDao.get(subject).getLectureCount();
                else
                    count+=Math.abs(subjectDao.get(subject).getLectureCount()-h3[i].get(subject));
            }
        }

        return count;
    }

}
