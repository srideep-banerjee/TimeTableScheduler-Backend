package org.example.pojo;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Objects;

@JsonPropertyOrder({"name", "rollNo", "semSec", "email", "attendance"})
public class Student {
    private final String name;
    private final String rollNo;
    @JsonAlias("sem")
    private final String semSec;
    private final String email;
    private final int attendance;

    public Student(@JsonProperty("name") String name, @JsonProperty("rollNo") String rollNo, @JsonProperty("semSec") String semSec, @JsonProperty("email") String email, @JsonProperty("attendance") int attendance) {
        this.name = name;
        this.rollNo = rollNo;
        this.semSec = semSec;
        this.email = email;
        this.attendance = attendance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student student)) return false;
        return attendance == student.attendance && Objects.equals(name, student.name) && Objects.equals(rollNo, student.rollNo) && Objects.equals(semSec, student.semSec) && Objects.equals(email, student.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, rollNo, semSec, email, attendance);
    }

    @JsonGetter("name")
    public String getName() {
        return this.name;
    }

    @JsonGetter("rollNo")
    public String getRollNo() {
        return rollNo;
    }

    @JsonGetter("semSec")
    public String getSemSec() {
        return this.semSec;
    }

    @JsonGetter("email")
    public String getEmail() {
        return email;
    }

    @JsonGetter("attendance")
    public int getAttendance() {
        return attendance;
    }
}
