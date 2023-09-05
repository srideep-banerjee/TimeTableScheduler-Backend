package org.example.pojo;

import com.fasterxml.jackson.annotation.*;

@JsonPropertyOrder({"sem", "lectureCount","isPractical","roomCode"})
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public class Subject {
    @JsonAlias({"semester"})
    private final int sem;
    private final int lectureCount;
    @JsonAlias({"practical"})
    private final boolean isPractical;
    @JsonAlias({"room"})
    private final String roomCode;

    public Subject(@JsonProperty("sem")int sem, @JsonProperty("lectureCount")int lectureCount, @JsonProperty("isPractical")boolean isPractical, @JsonProperty("roomCode")String roomCode) {
        this.sem = sem;
        this.lectureCount = lectureCount;
        this.isPractical = isPractical;
        this.roomCode = roomCode;
    }

    @JsonGetter("sem")
    public int getSem() {
        return sem;
    }

    @JsonGetter("lectureCount")
    public int getLectureCount() {
        return lectureCount;
    }

    @JsonGetter("isPractical")
    public boolean isPractical() {
        return isPractical;
    }

    @JsonGetter("roomCode")
    public String getRoomCode() {
        return roomCode;
    }
}
