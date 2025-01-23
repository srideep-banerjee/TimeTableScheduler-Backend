package org.example.network.api;

import java.util.*;

public class StringSearcher<T> {
    private final Node<T> start = new Node<>();

    public void addEntry(String key, T value) {
        Node<T> node = start;
        node.addValue(value);
        for (int i = 0; i < key.length(); i++) {
            node = node.addChar(key.charAt(i));
            node.addValue(value);
        }
    }

    public List<T> getValues(String key) {
        Node<T> node = start;
        for (int i = 0; i < key.length(); i++) {
            Node<T> next = node.getNext(key.charAt(i));
            if (next == null) return Collections.emptyList();
            node = next;
        }
        return node.getValues();
    }
}

class Node<T> {
    private final Map<Character, Node<T>> nextEntries;
    private final List<T> values;

    public Node() {
        nextEntries = new HashMap<>();
        values = new ArrayList<>();
    }

    public void addValue(T value) {
        values.add(value);
    }

    public Node<T> addChar(char c) {
        nextEntries.putIfAbsent(c, new Node<>());
        return nextEntries.get(c);
    }

    public Node<T> getNext(char c) {
        return nextEntries.get(c);
    }

    public List<T> getValues() {
        return Collections.unmodifiableList(values);
    }
}