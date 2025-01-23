package org.example.network.api;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class StringSearcherTest {

    @Test
    void emptySearcherEmptyInputReturnsEmpty() {
        StringSearcher<Integer> stringSearcher = new StringSearcher<>();
        assertEquals(Collections.EMPTY_LIST, stringSearcher.getValues(""));
    }

    @Test
    void emptySearcherNonEmptyInputReturnsEmpty() {
        StringSearcher<Integer> stringSearcher = new StringSearcher<>();
        assertEquals(Collections.EMPTY_LIST, stringSearcher.getValues("abcd"));
    }

    @Test
    void blankStringSearcherEmptyInputReturnsNonEmpty() {
        StringSearcher<Integer> stringSearcher = new StringSearcher<>();
        stringSearcher.addEntry("", 2);
        assertEquals(
                Collections.singletonList(2),
                stringSearcher.getValues("")
        );
    }

    @Test
    void nonEmptySearcherEmptyInputReturnsNonEmpty() {
        StringSearcher<Integer> stringSearcher = new StringSearcher<>();
        stringSearcher.addEntry("abcd", 3);
        assertEquals(
                Collections.singletonList(3),
                stringSearcher.getValues("")
        );
    }

    @Test
    void nonEmptySearcherMatchingInputReturnsNonEmpty() {
        StringSearcher<Integer> stringSearcher = new StringSearcher<>();
        stringSearcher.addEntry("abcd", 3);
        assertEquals(
                Collections.singletonList(3),
                stringSearcher.getValues("abcd")
        );
    }

    @Test
    void nonEmptySearcherLargerInputReturnsEmpty() {
        StringSearcher<Integer> stringSearcher = new StringSearcher<>();
        stringSearcher.addEntry("abcd", 0);
        assertEquals(
                Collections.EMPTY_LIST,
                stringSearcher.getValues("abcde")
        );
    }

    @Test
    void nonEmptySearcherSmallerInputReturnsNonEmpty() {
        StringSearcher<Integer> stringSearcher = new StringSearcher<>();
        stringSearcher.addEntry("abcd", 0);
        assertEquals(
                Collections.singletonList(0),
                stringSearcher.getValues("ab")
        );
    }

    @Test
    void multiSearcherSmallerInputReturnsAll() {
        StringSearcher<Integer> stringSearcher = new StringSearcher<>();
        stringSearcher.addEntry("abcd", 1);
        stringSearcher.addEntry("abef", 2);
        stringSearcher.addEntry("", 3);
        assertEquals(
                Arrays.asList(1, 2),
                stringSearcher.getValues("ab")
                );
        assertEquals(
                Arrays.asList(1, 2, 3),
                stringSearcher.getValues("")
                );
    }
}