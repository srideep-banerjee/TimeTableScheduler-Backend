package org.example.algorithms.io;

import java.util.Scanner;

public class ChromosomeReader {
    public static enum Type {
        TEACHER_INDEX,
        DAY_PERIOD,
        ROOM_CODE;
    }

    private Scanner sc;
    private byte sem = 1;

    public ChromosomeReader(Scanner sc) {
        this.sc = sc;
    }


}
