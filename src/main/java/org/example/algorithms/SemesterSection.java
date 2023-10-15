package org.example.algorithms;

public class SemesterSection {
    public final byte semester;
    public final byte section;

    public SemesterSection(byte semester, byte section) {
        this.semester = semester;
        this. section = section;
    }
    public SemesterSection(String str) {
        int ind = str.indexOf(',');
        this.semester = Byte.parseByte(str.substring(0,ind));
        this.section = Byte.parseByte(str.substring(ind+1));
    }
    public String toString() {
        return semester+","+section;
    }
    public static String toString(byte semester, byte section) {
        return semester+","+section;
    }
}
