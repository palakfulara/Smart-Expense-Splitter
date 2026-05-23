package service;

import model.*;

import java.util.*;

public class ExpenseService {

    public Expense addEqualExpense(Trip trip, String description,
                                   double amount, Person paidBy,
                                   List<Person> participants) {
        Map<Person, Double> shares = new LinkedHashMap<>();
        double share = roundTwo(amount / participants.size());
        for (Person p : participants) {
            shares.put(p, share);
        }
        adjustRemainder(shares, amount, participants.get(0));
        Expense expense = new Expense(description, amount, paidBy, SplitType.EQUAL, shares);
        trip.addExpense(expense);
        return expense;
    }

    public Expense addPercentageExpense(Trip trip, String description,
                                        double amount, Person paidBy,
                                        Map<Person, Double> percentages) {
        double total = percentages.values().stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(total - 100.0) > 0.01) {
            throw new IllegalArgumentException("Percentages must sum to 100. Got: " + total);
        }
        Map<Person, Double> shares = new LinkedHashMap<>();
        percentages.forEach((p, pct) -> shares.put(p, roundTwo(amount * pct / 100.0)));
        adjustRemainder(shares, amount, percentages.keySet().iterator().next());
        Expense expense = new Expense(description, amount, paidBy, SplitType.PERCENTAGE, shares);
        trip.addExpense(expense);
        return expense;
    }

    public Expense addCustomExpense(Trip trip, String description,
                                    double amount, Person paidBy,
                                    Map<Person, Double> customShares) {
        double total = customShares.values().stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(total - amount) > 0.01) {
            throw new IllegalArgumentException(
                    String.format("Custom shares (%.2f) do not sum to total amount (%.2f).", total, amount));
        }
        Expense expense = new Expense(description, amount, paidBy, SplitType.CUSTOM, customShares);
        trip.addExpense(expense);
        return expense;
    }

    public Map<Person, Double> computeBalances(Trip trip) {
        Map<Person, Double> balances = new LinkedHashMap<>();
        for (Person m : trip.getMembers()) {
            balances.put(m, 0.0);
        }
        for (Expense expense : trip.getExpenses()) {
            Person payer = expense.getPaidBy();
            balances.merge(payer, expense.getTotalAmount(), Double::sum);
            expense.getParticipantShares().forEach((person, share) ->
                    balances.merge(person, -share, Double::sum));
        }
        balances.replaceAll((p, v) -> roundTwo(v));
        return balances;
    }

    private double roundTwo(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private void adjustRemainder(Map<Person, Double> shares, double totalAmount, Person first) {
        double computed = shares.values().stream().mapToDouble(Double::doubleValue).sum();
        double diff     = roundTwo(totalAmount - computed);
        if (Math.abs(diff) > 0.0) {
            shares.merge(first, diff, Double::sum);
        }
    }
}
