package org.example.algorithms;

import org.example.dao.SubjectDao;
import org.example.pojo.ScheduleStructure;
import org.example.pojo.Subject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Util {
    public static int[] shuffle(int upperBound) {
        return shuffle(0, upperBound);
    }

    public static int[] shuffle(int lowerBound, int upperBound) {
        if(upperBound == lowerBound) return new int[0];
        if (upperBound - lowerBound == 1) return new int[]{lowerBound};
        int[] arr = new int[upperBound-lowerBound];
        for(int i = 0; i < arr.length; i++) arr[i] = i + lowerBound;
        Random random = new Random();
        int ind1;
        int ind2;
        for (int i = 0; i < arr.length; i++) {
            //Select distinct indices
            ind1 = random.nextInt(arr.length);
            ind2 = random.nextInt(arr.length - 1);
            if (ind2 >= ind1) ind2++;

            //Swap items in those two indices
            arr[ind1] = arr[ind1] + arr[ind2];
            arr[ind2] = arr[ind1] - arr[ind2];
            arr[ind1] = arr[ind1] - arr[ind2];
        }
        return arr;
    }

    public static byte getPracticalStartingPeriodLocation(String subject) {
        ScheduleStructure scheduleStructure = ScheduleStructure.getInstance();
        byte res = 0;
        short upperBound = scheduleStructure.getPeriodCount();
        Subject sub = SubjectDao.getInstance().get(subject);
        short trailingLength = (short) sub.getLectureCount();
        byte[] exclude = scheduleStructure.getBreakLocations(sub.getSem());
        byte excludeIndex = 0;
        for (byte i = 0; i <= upperBound - trailingLength; i++) {
            if (excludeIndex < exclude.length && exclude[excludeIndex] - 1 < i + trailingLength) {
                i = (byte) (exclude[excludeIndex++] - 1);
                continue;
            }
            res = i;
        }
        return res;
    }

    public static ArrayList<Byte> getAllPracticalPeriodLocations(String subject) {
        ArrayList<Byte> res = new ArrayList<>();
        ScheduleStructure scheduleStructure = ScheduleStructure.getInstance();
        short upperBound = scheduleStructure.getPeriodCount();
        Subject sub = SubjectDao.getInstance().get(subject);
        short trailingLength = (short) sub.getLectureCount();
        byte[] exclude = scheduleStructure.getBreakLocations(sub.getSem());
        byte excludeIndex = 0;
        for (byte i = 0; i <= upperBound - trailingLength; i++) {
            if (excludeIndex < exclude.length && exclude[excludeIndex] - 1 < i + trailingLength) {
                i = (byte) (exclude[excludeIndex++] - 1);
                continue;
            }
            res.add(i);
        }
        Collections.reverse(res);
        return res;
    }

    public static byte getSectionCount(String subjectCode) {
        Subject subject = SubjectDao.getInstance().get(subjectCode);
        return ScheduleStructure.getInstance().getSectionCount(subject.getSem());
    }
}