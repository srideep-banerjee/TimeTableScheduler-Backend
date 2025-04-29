package org.example.pojo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.example.dao.json.Byte1DArraySerializer;
import org.example.dao.json.Byte2DArraySerializer;

import java.util.Arrays;

/**
 * A singleton class to store the current schedule structure
 */
@JsonPropertyOrder({"semesterCount", "sectionsPerSemester", "periodCount", "breaksPerSemester"})
public class ScheduleStructure {
    @JsonProperty("sectionsPerSemester")
    private byte[] sectionsPerSemester;
    @JsonProperty("periodCount")
    private byte periodCount;
    @JsonProperty("breaksPerSemester")
    private byte[][] breaksPerSemester;//if breakPerSemester[0]=3, semester 1 has break between 3rd and 4th periods
    @JsonProperty("semesterCount")
    private byte semesterCount;
    @JsonProperty("dayCount")
    private byte dayCount = 5;
    @JsonIgnore
    private static ScheduleStructure instance;

    @JsonIgnore
    private ScheduleStructure() {
    }

    @JsonIgnore
    public static ScheduleStructure getInstance() {
        if (instance == null) {
            instance = getRevertedClone();
        }
        return instance;
    }

    /**
     * Used to easily get a new schedule structure with values reset to defaults</br>
     * @return A new {@code ScheduleStructure} that is independent of the current instance
     */
    @JsonIgnore
    public static ScheduleStructure getRevertedClone() {
        ScheduleStructure scheduleStructure = new ScheduleStructure();
        scheduleStructure.semesterCount = 4;
        scheduleStructure.sectionsPerSemester = new byte[] {0, 0, 1, 0};
        scheduleStructure.periodCount = 9;
        scheduleStructure.breaksPerSemester = new byte[][]{{4, 5}, {5}, {5}, {5}};
        scheduleStructure.dayCount = 5;
        return scheduleStructure;
    }

    @JsonIgnore
    public byte getSectionCount(int semester) {
        semester = semester % 2 == 0 ? semester / 2 : (semester + 1) / 2;
        return this.sectionsPerSemester[semester - 1];
    }

    @JsonSerialize(using = Byte1DArraySerializer.class)
    @JsonGetter("sectionsPerSemester")
    public byte[] getSectionsPerSemester() {
        return sectionsPerSemester;
    }

    @JsonIgnore
    public byte[] getBreakLocations(int semester) {
        semester = semester % 2 == 0 ? semester / 2 : (semester + 1) / 2;
        return this.breaksPerSemester[semester - 1];
    }

    @JsonSerialize(using = Byte2DArraySerializer.class)
    @JsonGetter("breaksPerSemester")
    public byte[][] getBreaksPerSemester() {
        return breaksPerSemester;
    }

    public byte getPeriodCount() {
        return this.periodCount;
    }

    public byte getDayCount() {
        return dayCount;
    }

    public byte getSemesterCount() {
        return this.semesterCount;
    }


    public void setSectionsPerSemester(byte[] sectionsPerSemester) {
        if (sectionsPerSemester.length != semesterCount)
            throw new RuntimeException("Invalid data format");
        this.sectionsPerSemester = sectionsPerSemester;
    }

    public void setBreaksPerSemester(byte[][] breaksPerSemester) {
        if (breaksPerSemester.length != semesterCount) return;
        for (byte[] breaks : breaksPerSemester) {
            Arrays.sort(breaks);
            for (byte br : breaks)
                if (br > periodCount) throw new RuntimeException("Invalid data format");
        }
        this.breaksPerSemester = breaksPerSemester;
    }

    public void setPeriodCount(byte periodCount) {
        this.periodCount = periodCount;
    }

    public void setDayCount(byte dayCount) {
        this.dayCount = dayCount;
    }

    public void setSemesterCount(byte semesterCount) {
        this.semesterCount = semesterCount;
    }
}
