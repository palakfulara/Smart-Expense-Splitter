package model;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class Expense {
    private final String description;
    private final double totalAmount;
    private final Person paidBy;
    private final SplitType splitType;
    private final LocalDate date;
    private final Map<Person, Double> participantShares;

    public Expense(String description, double totalAmount, Person paidBy,
                   SplitType splitType, Map<Person, Double> participantShares) {
        this.description       = description;
        this.totalAmount       = totalAmount;
        this.paidBy            = paidBy;
        this.splitType         = splitType;
        this.date              = LocalDate.now();
        this.participantShares = new LinkedHashMap<>(participantShares);
    }

    public String              getDescription()       { return description; }
    public double              getTotalAmount()       { return totalAmount; }
    public Person              getPaidBy()            { return paidBy; }
    public SplitType           getSplitType()         { return splitType; }
    public LocalDate           getDate()              { return date; }
    public Map<Person, Double> getParticipantShares() { return participantShares; }

    @Override
    public String toString() {
        return String.format("[%s] %s - Rs%.2f (paid by %s, %s split)",
                date, description, totalAmount, paidBy.getName(), splitType);
    }
}