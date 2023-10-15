package org.example.algorithms;

import java.util.List;
import java.util.Random;

public class Util {
    public static <T> void shuffle(List<T> arr) {
        Random random = new Random();
        int ind1;
        int ind2;
        T item;
        for (int i = 0; i < arr.size(); i++) {
            //Select distinct indices
            ind1 = random.nextInt(arr.size());
            ind2 = random.nextInt(arr.size() - 1);
            if (ind2 >= ind1) ind2++;

            //Swap items in those two indices
            item = arr.get(ind1);
            arr.set(ind1, arr.get(ind2));
            arr.set(ind2, item);
        }
    }
}