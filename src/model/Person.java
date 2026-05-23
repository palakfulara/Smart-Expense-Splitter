package model;

import java.util.Objects;

public class Person {
    private final String id;
    private final String name;

    public Person(String name) {
        this.id = name.toLowerCase().replaceAll("\\s+", "_") + "_" + System.nanoTime() % 10000;
        this.name = name;
    }

    public String getId()   { return id; }
    public String getName() { return name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;
        return id.equals(((Person) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() { return name; }
}