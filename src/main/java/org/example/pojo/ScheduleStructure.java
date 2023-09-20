package org.example.pojo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.example.dao.json.Byte1DArraySerializer;
import org.example.dao.json.Byte2DArraySerializer;

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
    @JsonIgnore
    private static ScheduleStructure instance;

    @JsonIgnore
    private ScheduleStructure() {
    }

    @JsonIgnore
    public static ScheduleStructure getInstance() {
        if (instance == null) {
            instance = new ScheduleStructure();
            instance.semesterCount = 4;
            instance.sectionsPerSemester = new byte[]{0, 0, 1, 0};
            instance.periodCount = 9;
            instance.breaksPerSemester = new byte[][]{{4, 5}, {5}, {5}, {5}};
        }
        return instance;
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
    public byte[][] gerBreaksPerSemester() {
        return breaksPerSemester;
    }

    public byte getPeriodCount() {
        return this.periodCount;
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
            for (byte br : breaks)
                if (br > periodCount) throw new RuntimeException("Invalid data format");
        }
        this.breaksPerSemester = breaksPerSemester;
    }

    public void setPeriodCount(byte periodCount) {
        this.periodCount = periodCount;
    }

    public void setSemesterCount(byte semesterCount) {
        this.semesterCount = semesterCount;
    }
}
