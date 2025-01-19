package org.example.pojo;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Objects;

@JsonPropertyOrder({"name", "rollNo", "sem", "sec", "semSec", "email", "attendance", "phoneNumber", "address"})
public class Student {
    private final String name;
    private final String rollNo;
    private final int sem;
    private final int sec;
    private final String email;
    private final int attendance;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String phoneNumber;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String address;

    public Student(@JsonProperty("name") String name, @JsonProperty("rollNo") String rollNo, @JsonProperty("sem") int sem, @JsonProperty("sec") int sec, @JsonProperty("email") String email, @JsonProperty("attendance") int attendance, @JsonProperty("phoneNumber") String phoneNumber, @JsonProperty("address") String address) {
        this.name = name;
        this.rollNo = rollNo;
        this.sem = sem;
        this.sec = sec;
        this.email = email;
        this.attendance = attendance;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student student)) return false;
        return attendance == student.attendance && Objects.equals(name, student.name) && Objects.equals(rollNo, student.rollNo) && Objects.equals(sem, student.sem) && Objects.equals(sec, student.sec) && Objects.equals(email, student.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, rollNo, sem, sec, email, attendance);
    }

    @JsonGetter("name")
    public String getName() {
        return name;
    }

    @JsonGetter("rollNo")
    public String getRollNo() {
        return rollNo;
    }

    @JsonGetter("sem")
    public int getSem() {
        return sem;
    }

    @JsonGetter("sec")
    public int getSec() {
        return sec;
    }

    @JsonGetter("email")
    public String getEmail() {
        return email;
    }

    @JsonGetter("attendance")
    public int getAttendance() {
        return attendance;
    }

    @JsonGetter("phoneNumber")
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @JsonGetter("address")
    public String getAddress() {
        return address;
    }
}
