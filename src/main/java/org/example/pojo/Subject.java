package org.example.pojo;

import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;

@JsonPropertyOrder({"sem", "lectureCount", "isPractical", "roomCodes"})
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public class Subject {
    @JsonAlias({"semester"})
    private final int sem;
    private final int lectureCount;
    @JsonAlias({"practical"})
    private final boolean isPractical;
    @JsonAlias({"room","roomCode"})
    private final ArrayList<String> roomCodes;

    public Subject(@JsonProperty("sem") int sem, @JsonProperty("lectureCount") int lectureCount, @JsonProperty("isPractical") boolean isPractical, @JsonProperty("roomCodes") ArrayList<String> roomCodes) {
        this.sem = sem;
        this.lectureCount = lectureCount;
        this.isPractical = isPractical;
        this.roomCodes = roomCodes;
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

    @JsonGetter("roomCodes")
    public ArrayList<String> getRoomCodes() {
        return roomCodes;
    }
}
