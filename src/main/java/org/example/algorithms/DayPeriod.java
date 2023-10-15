package org.example.algorithms;

import org.example.pojo.ScheduleStructure;

public class DayPeriod {
    public final byte day;
    public final byte period;

    public DayPeriod(byte day, byte period) {
        this.day = day;
        this.period = period;
    }

    public DayPeriod(short compact) {
        this.day = (byte) (compact / ScheduleStructure.getInstance().getPeriodCount());
        this.period = (byte) (compact % ScheduleStructure.getInstance().getPeriodCount());
    }

    public short getCompact() {
        return (short) (day * ScheduleStructure.getInstance().getPeriodCount() + period);
    }

    public static short getCompact(byte day, byte period) {
        return (short) (day * ScheduleStructure.getInstance().getPeriodCount() + period);
    }
}