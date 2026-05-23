package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Trip {
    private final String name;
    private final Set<Person> members;
    private final List<Expense> expenses;

    public Trip(String name) {
        this.name     = name;
        this.members  = new LinkedHashSet<>();
        this.expenses = new ArrayList<>();
    }

    public void addMember(Person p)   { members.add(p); }
    public void addExpense(Expense e) { expenses.add(e); }

    public String        getName()     { return name; }
    public Set<Person>   getMembers()  { return Collections.unmodifiableSet(members); }
    public List<Expense> getExpenses() { return Collections.unmodifiableList(expenses); }

    public double getTotalExpenses() {
        return expenses.stream().mapToDouble(Expense::getTotalAmount).sum();
    }
}