package org.example.algorithms.io;

import org.example.algorithms.DayPeriod;
import org.example.algorithms.Util;
import org.example.dao.SubjectDao;
import org.example.pojo.ScheduleStructure;
import org.example.pojo.Subject;

import java.io.Closeable;
import java.io.IOException;
import java.util.Scanner;

/**
 * A helper class to provide standard way of reading a chromosome
 */
public class ChromosomeReader implements Closeable {
    private final Scanner sc;
    private final String[] teachers;
    private final String[] subjects;
    private final String[] rooms;

    private int subjectIndex = 0;
    private byte currentSec = 0;
    private int lectureIndex = 0;
    private String roomCode = null;
    private int teacherIndex = -1;
    private short dayPeriod = -1;


    /**
     * A helper class to provide standard way of reading a chromosome
     *
     * @param sc       A scanner reading the file containing the chromosome
     * @param subjects the array of subject codes to fetch subject code
     *                 from subject index in chromosome
     * @param teachers the array of teacher names to fetch teacher name
     *                 from teacher index in chromosome
     * @param rooms    the array of room codes to fetch room code
     *                 from room index in chromosome
     */
    public ChromosomeReader(Scanner sc, String[] teachers, String[] subjects, String[] rooms) {
        this.sc = sc;
        this.teachers = teachers;
        this.subjects = subjects;
        this.rooms = rooms;
    }

    /**
     * Returns whether there is more data in the chromosome that can be read
     *
     * @return {@code true} if there is more data to be read otherwise {@code false}
     */
    public boolean hasNext() {
        while (subjectIndex < subjects.length && Util.getSectionCount(subjects[subjectIndex]) == 0)
            subjectIndex++;
        return subjectIndex < subjects.length;
    }

    /**
     * Reads a single entry of the chromosome and calls {@code process()} function of the
     * provided {@code ReaderCallback}
     * @param readerCallback the callback to pass chromosome data back to the caller
     * @throws IOException If any error occurs while reading
     * @see #read(IndexedReaderCallback)
     */
    public void read(ReaderCallback readerCallback) throws IOException {
        read(
                (sem, sec, day, period, subjectIndex, teacherIndex, roomCode, lectureIndex) ->
                        readerCallback.process(sem, sec, day, period, subjects[subjectIndex], teacherIndex != -1 ? teachers[teacherIndex] : null, roomCode)
        );
    }

    /**
     * Same as {@link #read(ReaderCallback)} but provides index as callback
     * for writing purposes
     * @param readerCallback the callback to pass chromosome data back to the caller
     * @throws IOException If any error occurs while reading
     * @see #read(ReaderCallback)
     */
    public void read(IndexedReaderCallback readerCallback) throws IOException {
        if (!hasNext()) throw new IOException("End of chromosome reached");

        String subjectCode = subjects[subjectIndex];
        Subject subject = SubjectDao.getInstance().get(subjectCode);
        byte sem = (byte) subject.getSem();
        sem = (byte) (sem % 2 == 0 ? sem / 2 : (sem + 1) / 2);
        boolean practical = subject.isPractical();
        boolean free = subject.isFree();

        if (practical) {
            if (dayPeriod == -1) dayPeriod = sc.nextShort();
            if (roomCode == null) {
                if (!free) roomCode = rooms[sc.nextShort()];
                else roomCode = subject.getRoomCodes().get(0);
            }
            if (!free) teacherIndex = sc.nextShort();
        } else {
            if (teacherIndex == -1 && !free) teacherIndex = sc.nextShort();
            dayPeriod = sc.nextShort();
            roomCode = subject.getRoomCodes().get(0);
        }

        DayPeriod dayPeriodObj = new DayPeriod(practical ? (short) (dayPeriod + lectureIndex) : dayPeriod);
        readerCallback.process((byte) (sem - 1), currentSec, dayPeriodObj.day, dayPeriodObj.period, subjectIndex, teacherIndex, roomCode, lectureIndex);

        lectureIndex++;
        if (lectureIndex >= subject.getLectureCount()) {
            lectureIndex = 0;
            currentSec++;
            roomCode = null;
            teacherIndex = -1;
            dayPeriod = -1;
        }

        if (currentSec >= Util.getSectionCount(subjectCode)) {
            currentSec = 0;
            subjectIndex++;
        }
    }

    /**
     * Reads the entire chromosome and calls {@code process()} function of the
     * provided {@code ReaderCallback}<br>
     * Note: This function should be preferred over {@code read()} when
     * reading the entire chromosome as this function is more efficient due to caching of data
     *
     * @param readerCallback the callback to pass chromosome data back to the caller
     * @throws IOException If any error occurs while reading
     * @see #readAll(IndexedReaderCallback)
     */
    public void readAll(ReaderCallback readerCallback) throws IOException {
        readAll(
                (sem, sec, day, period, subjectIndex, teacherIndex, roomCode, lectureIndex) ->
                        readerCallback.process(sem, sec, day, period, subjects[subjectIndex], teacherIndex != -1 ? teachers[teacherIndex] : null, roomCode)
        );
    }

    /**
     * Same as {@link #read(ReaderCallback)} but provides index as callback
     * for writing purposes
     * @param readerCallback the callback to pass chromosome data back to the caller
     * @throws IOException If any error occurs while reading
     * @see #readAll(ReaderCallback)
     */
    public void readAll(IndexedReaderCallback readerCallback) throws IOException {
        if (!hasNext()) throw new IOException("End of chromosome reached");

        SubjectDao subjectDao = SubjectDao.getInstance();

        for (int subjectIndex = this.subjectIndex; subjectIndex < subjects.length; subjectIndex++) {
            String subject = subjects[subjectIndex];
            byte sem = (byte) subjectDao.get(subject).getSem();
            byte secCount = ScheduleStructure.getInstance().getSectionCount(sem);
            sem = (byte) (sem % 2 == 0 ? sem / 2 : (sem + 1) / 2);
            boolean practical = subjectDao.get(subject).isPractical();
            boolean free = subjectDao.get(subject).isFree();

            byte sec = subjectIndex == this.subjectIndex ? currentSec : 0;
            for (; sec < secCount; sec++) {
                int teacherIndex = -1;
                short practicalStartDayPeriod = -1;
                short dayPeriod;
                String roomCode;
                if (practical) {
                    practicalStartDayPeriod = sc.nextShort();
                    if (!free) roomCode = rooms[sc.nextShort()];
                    else roomCode = subjectDao.get(subject).getRoomCodes().get(0);
                } else {
                    if (!free) teacherIndex = sc.nextShort();
                    roomCode = subjectDao.get(subject).getRoomCodes().get(0);
                }
                int lectureCount = subjectDao.get(subject).getLectureCount();

                int lectureIndex = subjectIndex == this.subjectIndex && sec == currentSec ? this.lectureIndex : 0;
                for (; lectureIndex < lectureCount; lectureIndex++) {
                    if (practical) {
                        dayPeriod = (short) (practicalStartDayPeriod + lectureIndex);
                        if (!free) teacherIndex = sc.nextShort();
                    } else {
                        dayPeriod = sc.nextShort();
                    }
                    DayPeriod dayPeriodObj = new DayPeriod(dayPeriod);
                    readerCallback.process((byte) (sem - 1), sec, dayPeriodObj.day, dayPeriodObj.period, subjectIndex, teacherIndex, roomCode, lectureIndex);
                }
            }
        }
    }

    @Override
    public void close() {
        sc.close();
    }

    public interface ReaderCallback {
        void process(byte sem, byte sec, byte day, byte period, String subject, String teacher, String room);
    }

    public interface IndexedReaderCallback {
        void process(byte sem, byte sec, byte day, byte period, int subjectIndex, int teacherIndex, String roomCode, int lectureIndex);
    }
}
