package org.example.algorithms.io;

import org.example.algorithms.DayPeriod;
import org.example.dao.SubjectDao;
import org.example.pojo.ScheduleStructure;

import java.util.Scanner;

/**
 * A helper class to provide standard way of reading a chromosome
 */
public class ChromosomeReader {
    private final Scanner sc;
    private final String[] teachers;
    private final String[] subjects;
    private final String[] rooms;
    private final ReaderCallback readerCallback;

    public ChromosomeReader(Scanner sc, String[] teachers, String[] subjects, String[] rooms, ReaderCallback readerCallback) {
        this.sc = sc;
        this.teachers = teachers;
        this.subjects = subjects;
        this.rooms = rooms;
        this.readerCallback = readerCallback;
    }

    /**
     * Starts reading the chromosome and calls {@code process()} function of the
     * provided {@code ReaderCallback} interface
     */
    public void read() {
        SubjectDao subjectDao = SubjectDao.getInstance();

        for (String subject : subjects) {
            byte sem = (byte) subjectDao.get(subject).getSem();
            byte secCount = ScheduleStructure.getInstance().getSectionCount(sem);
            sem = (byte) (sem % 2 == 0 ? sem / 2 : (sem + 1) / 2);
            boolean practical = subjectDao.get(subject).isPractical();
            boolean free = subjectDao.get(subject).isFree();

            for (byte sec = 0; sec < secCount; sec++) {
                String teacher = null;
                short val = -1;
                short value;
                String roomCode;
                if (!practical) {
                    if (!free) teacher = teachers[sc.nextShort()];
                    roomCode = subjectDao.get(subject).getRoomCodes().get(0);
                }
                else {
                    val = sc.nextShort();
                    if (!free) roomCode = rooms[sc.nextShort()];
                    else roomCode = subjectDao.get(subject).getRoomCodes().get(0);
                }
                int lectureCount = subjectDao.get(subject).getLectureCount();

                for (int j = 0; j < lectureCount; j++) {
                    if (practical) {
                        value = (short) (val + j);
                        if (!free) teacher = teachers[sc.nextShort()];
                    } else {
                        value = sc.nextShort();
                    }
                    DayPeriod dayPeriod = new DayPeriod(value);
                    readerCallback.process((byte) (sem - 1), sec, dayPeriod.day, dayPeriod.period, subject, teacher, roomCode);
                }
            }
        }
    }

    public interface ReaderCallback {
        void process(byte sem, byte sec, byte day, byte period, String subject, String teacher, String room);
    }
}
