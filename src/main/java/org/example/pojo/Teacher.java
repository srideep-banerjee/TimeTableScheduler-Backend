package org.example.pojo;

import com.fasterxml.jackson.annotation.*;

import java.util.*;

@JsonPropertyOrder({"freeTime", "subjects"})
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public class Teacher {
    private final HashSet<List<Integer>> freeTime;
    private final HashSet<String> subjects;

    public Teacher(@JsonProperty("freeTime") HashSet<List<Integer>> freeTime, @JsonProperty("subjects") HashSet<String> subjects) {
        this.freeTime = freeTime;
        this.subjects = subjects;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Teacher teacher)) return false;
        return Objects.equals(freeTime, teacher.freeTime) && Objects.equals(subjects, teacher.subjects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(freeTime, subjects);
    }

    @JsonGetter("freeTime")
    public HashSet<List<Integer>> getFreeTime() {
        return freeTime;
    }

    @JsonGetter("subjects")
    public HashSet<String> getSubjects() {
        return subjects;
    }

}
