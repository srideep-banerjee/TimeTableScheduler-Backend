package org.example;

import org.example.algorithms.ChromosomeAnalyzer;
import org.example.algorithms.DayPeriod;
import org.example.algorithms.PreComputation;
import org.example.algorithms.SemesterSection;

public class ChromosomeAnalyzerTest {
    public void test(){
        String[] subjectCodeArray = {"PCC-CS592", "ESC591", "PCC-CS593", "HSMC-501", "MC501", "PEC-ITB", "RPI", "ESC501", "PCC-CS503", "PCC-CS502", "PCC-CS501", "LET", "GD"};
        String[] teacherNameArray = {"DG", "SAR", "MRM", "J", "DS", "BR", "SC", "SG", "SBG", "MG", "SS", "AC", "PKC", "SKB", "LKM", "AP", "AS", "PC", "PD", "PKP", "SKHC", "RG", "RKM", "SKS", "TP"};
        String[] roomCodeArray = {"LAB-13,14", "LAB-3,4", "LH123", "LAB-7,8"};
        PreComputation pc= new PreComputation(subjectCodeArray, teacherNameArray);
        pc.compute();
        ChromosomeAnalyzer ca = new ChromosomeAnalyzer(subjectCodeArray, teacherNameArray, pc.getTeachersForSubjects());
        ca.assignPractical(new SemesterSection((byte) 5, (byte) 1), new DayPeriod((short) 15), new short[]{23,0,2}, "PCC-CS592","LAB-13,14");
        ca.assignPractical(new SemesterSection((byte) 5, (byte) 2), new DayPeriod((short) 42), new short[]{13,0,14}, "PCC-CS592","LAB-13,14");
        ca.assignPractical(new SemesterSection((byte) 5, (byte) 3), new DayPeriod((short) 33), new short[]{3,2,14}, "PCC-CS592","LAB-13,14");
        ca.assignPractical(new SemesterSection((byte) 5, (byte) 1), new DayPeriod((short) 33), new short[]{5,19,1}, "ESC591","LAB-3,4");
        ca.assignPractical(new SemesterSection((byte) 5, (byte) 2), new DayPeriod((short) 6), new short[]{5,18,10}, "ESC591","LAB-3,4");
        ca.assignPractical(new SemesterSection((byte) 5, (byte) 3), new DayPeriod((short) 42), new short[]{1,20,12}, "ESC591","LAB-3,4");
        ca.assignPractical(new SemesterSection((byte) 5, (byte) 1), new DayPeriod((short) 24), new short[]{17,20,14}, "PCC-CS593","LAB-13,14");
        var x = ca.suggestPracticalTimeRoom(new SemesterSection((byte) 5, (byte) 2), new short[]{17,13,8}, "PCC-CS593");
        System.out.println("day:"+x.time.day+" period:"+x.time.period);
        x = ca.suggestPracticalTimeRoom(new SemesterSection((byte) 5, (byte) 2), new short[]{17,13,8}, "PCC-CS593");
        System.out.println("day:"+x.time.day+" period:"+x.time.period);
        x = ca.suggestPracticalTimeRoom(new SemesterSection((byte) 5, (byte) 2), new short[]{17,13,8}, "PCC-CS593");
        System.out.println("day:"+x.time.day+" period:"+x.time.period);
    }
}
