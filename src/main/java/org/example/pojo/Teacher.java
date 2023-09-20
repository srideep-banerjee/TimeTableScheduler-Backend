package org.example.pojo;

import com.fasterxml.jackson.annotation.*;

import java.util.*;

@JsonPropertyOrder({"freeTime", "subjects"})
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public class Teacher {
    private final HashSet<int[]> freeTime;
    private final HashSet<String> subjects;

    public Teacher(@JsonProperty("freeTime") HashSet<int[]> freeTime, @JsonProperty("subjects") HashSet<String> subjects) {
        this.freeTime = freeTime;
        this.subjects = subjects;
    }

    @JsonGetter("freeTime")
    public HashSet<int[]> getFreeTime() {
        return freeTime;
    }

    @JsonGetter("subjects")
    public HashSet<String> getSubjects() {
        return subjects;
    }

}
