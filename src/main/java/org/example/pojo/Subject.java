package org.example.pojo;

import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;
import java.util.Objects;

@JsonPropertyOrder({"sem", "lectureCount", "isPractical", "roomCodes", "isFree"})
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public class Subject {
    @JsonAlias({"semester"})
    private final int sem;
    private final int lectureCount;
    @JsonAlias({"practical"})
    private final boolean isPractical;
    @JsonAlias({"room","roomCode"})
    private final ArrayList<String> roomCodes;
    @JsonAlias({"free"})
    private final boolean isFree;

    public Subject(@JsonProperty("sem") int sem, @JsonProperty("lectureCount") int lectureCount, @JsonProperty("isPractical") boolean isPractical, @JsonProperty("roomCodes") ArrayList<String> roomCodes, @JsonProperty("isFree") boolean isFree) {
        this.sem = sem;
        this.lectureCount = lectureCount;
        this.isPractical = isPractical;
        this.roomCodes = roomCodes;
        this.isFree = isFree;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subject subject)) return false;
        return sem == subject.sem && lectureCount == subject.lectureCount && isPractical == subject.isPractical && Objects.equals(roomCodes, subject.roomCodes) && isFree == subject.isFree;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sem, lectureCount, isPractical, roomCodes, isFree);
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

    @JsonGetter("isFree")
    public boolean isFree() {
        return isFree;
    }
}
