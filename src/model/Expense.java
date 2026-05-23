package model;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class Expense {
    private final String description;
    private final double totalAmount;
    private final Person paidBy;
    private final Map<Person, Double> allPayers;
    private final SplitType splitType;
    private final LocalDate date;
    private final Map<Person, Double> participantShares;

    public Expense(String description, double totalAmount, Person paidBy,
                   Map<Person, Double> allPayers, SplitType splitType,
                   Map<Person, Double> participantShares) {
        this.description       = description;
        this.totalAmount       = totalAmount;
        this.paidBy            = paidBy;
        this.allPayers         = allPayers;
        this.splitType         = splitType;
        this.date              = LocalDate.now();
        this.participantShares = new LinkedHashMap<>(participantShares);
    }

    public String              getDescription()       { return description; }
    public double              getTotalAmount()       { return totalAmount; }
    public Person              getPaidBy()            { return paidBy; }
    public Map<Person, Double> getAllPayers()         { return allPayers; }
    public SplitType           getSplitType()         { return splitType; }
    public LocalDate           getDate()              { return date; }
    public Map<Person, Double> getParticipantShares() { return participantShares; }

    public boolean hasMultiplePayers() { return allPayers != null && allPayers.size() > 1; }

    @Override
    public String toString() {
        String payerStr = hasMultiplePayers()
                ? allPayers.size() + " payers"
                : paidBy.getName();
        return String.format("[%s] %s - Rs%.2f (paid by %s, %s split)",
                date, description, totalAmount, payerStr, splitType);
    }
}