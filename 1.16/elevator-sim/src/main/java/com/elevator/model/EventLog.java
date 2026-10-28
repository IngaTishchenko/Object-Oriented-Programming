package com.elevator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public final class EventLog {

    public record Entry(long time, String message) {
        public Entry {
            Objects.requireNonNull(message, "message must not be null");
        }

        public String toString() {
            return String.format("[%5d] %s", time, message);
        }
    }

    private final List<Entry> entries = new ArrayList<>();

    public void log(long time, String message) {
        entries.add(new Entry(time, message));
    }

    public List<Entry> getEntries() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public int size() {
        return entries.size();
    }

    public void clear() {
        entries.clear();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("EventLog (").append(entries.size()).append(" entries):\n");
        for (Entry e : entries) {
            sb.append("  ").append(e).append('\n');
        }
        return sb.toString();
    }
}