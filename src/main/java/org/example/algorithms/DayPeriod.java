package org.example.algorithms;

import org.example.pojo.ScheduleStructure;

import java.util.Objects;

public class DayPeriod {
    public final byte day;
    public final byte period;
    private final int hashCode;

    public DayPeriod(byte day, byte period) {
        this.day = day;
        this.period = period;
        this.hashCode = Objects.hash(day, period);
    }

    public DayPeriod(short compact) {
        this.day = (byte) (compact / ScheduleStructure.getInstance().getPeriodCount());
        this.period = (byte) (compact % ScheduleStructure.getInstance().getPeriodCount());
        this.hashCode = Objects.hash(day, period);
    }

    public short getCompact() {
        return (short) (day * ScheduleStructure.getInstance().getPeriodCount() + period);
    }

    public static short getCompact(byte day, byte period) {
        return (short) (day * ScheduleStructure.getInstance().getPeriodCount() + period);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DayPeriod other = (DayPeriod) o;
        return other.day == this.day && other.period == this.period;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }
}