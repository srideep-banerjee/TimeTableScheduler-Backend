package org.example.algorithms;

public class SemesterSection {
    public final byte semester;
    public final byte section;
    private final int hashCode;

    public SemesterSection(byte semester, byte section) {
        this.semester = semester;
        this. section = section;
        this.hashCode = toString().hashCode();
    }
    public SemesterSection(String str) {
        int ind = str.indexOf(',');
        this.semester = Byte.parseByte(str.substring(0,ind));
        this.section = Byte.parseByte(str.substring(ind+1));
        this.hashCode = toString().hashCode();
    }

    public String toString() {
        return semester+","+section;
    }

    public static String toString(byte semester, byte section) {
        return semester+","+section;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SemesterSection other = (SemesterSection) o;
        return other.semester == this.semester && other.section == this.section;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }
}
