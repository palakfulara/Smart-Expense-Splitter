package service;

import model.*;

import java.util.*;

public class ExpenseService {

    public Expense addEqualExpense(Trip trip, String description,
                                   double amount, Person paidBy,
                                   List<Person> participants) {
        Map<Person, Double> shares = new LinkedHashMap<>();
        double share = roundTwo(amount / participants.size());
        for (Person p : participants) shares.put(p, share);
        adjustRemainder(shares, amount, participants.get(0));
        Expense expense = new Expense(description, amount, paidBy, null, SplitType.EQUAL, shares);
        trip.addExpense(expense);
        return expense;
    }

    public List<Expense> addEqualExpenseMultiplePayers(Trip trip, String description,
                                                       Map<Person, Double> payerAmounts,
                                                       List<Person> participants) {
        List<Expense> expenses = new ArrayList<>();
        for (Map.Entry<Person, Double> entry : payerAmounts.entrySet()) {
            Person payer  = entry.getKey();
            double amount = entry.getValue();
            Map<Person, Double> shares = new LinkedHashMap<>();
            double share = roundTwo(amount / participants.size());
            for (Person p : participants) shares.put(p, share);
            adjustRemainder(shares, amount, participants.get(0));
            Expense expense = new Expense(description, amount, payer, payerAmounts, SplitType.EQUAL, shares);
            trip.addExpense(expense);
            expenses.add(expense);
        }
        return expenses;
    }

    public Expense addPercentageExpense(Trip trip, String description,
                                        double amount, Person paidBy,
                                        Map<Person, Double> percentages) {
        double total = percentages.values().stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(total - 100.0) > 0.01)
            throw new IllegalArgumentException("Percentages must sum to 100. Got: " + total);
        Map<Person, Double> shares = new LinkedHashMap<>();
        percentages.forEach((p, pct) -> shares.put(p, roundTwo(amount * pct / 100.0)));
        adjustRemainder(shares, amount, percentages.keySet().iterator().next());
        Expense expense = new Expense(description, amount, paidBy, null, SplitType.PERCENTAGE, shares);
        trip.addExpense(expense);
        return expense;
    }

    public List<Expense> addPercentageExpenseMultiplePayers(Trip trip, String description,
                                                            Map<Person, Double> payerAmounts,
                                                            Map<Person, Double> percentages) {
        double total = percentages.values().stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(total - 100.0) > 0.01)
            throw new IllegalArgumentException("Percentages must sum to 100. Got: " + total);
        List<Expense> expenses = new ArrayList<>();
        for (Map.Entry<Person, Double> entry : payerAmounts.entrySet()) {
            Person payer  = entry.getKey();
            double amount = entry.getValue();
            Map<Person, Double> shares = new LinkedHashMap<>();
            percentages.forEach((p, pct) -> shares.put(p, roundTwo(amount * pct / 100.0)));
            adjustRemainder(shares, amount, percentages.keySet().iterator().next());
            Expense expense = new Expense(description, amount, payer, payerAmounts, SplitType.PERCENTAGE, shares);
            trip.addExpense(expense);
            expenses.add(expense);
        }
        return expenses;
    }

    public Expense addCustomExpense(Trip trip, String description,
                                    double amount, Person paidBy,
                                    Map<Person, Double> customShares) {
        double total = customShares.values().stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(total - amount) > 0.01)
            throw new IllegalArgumentException(
                    String.format("Custom shares (%.2f) do not sum to total (%.2f).", total, amount));
        Expense expense = new Expense(description, amount, paidBy, null, SplitType.CUSTOM, customShares);
        trip.addExpense(expense);
        return expense;
    }

    public List<Expense> addCustomExpenseMultiplePayers(Trip trip, String description,
                                                        Map<Person, Double> payerAmounts,
                                                        Map<Person, Double> customShares) {
        double totalPaid  = payerAmounts.values().stream().mapToDouble(Double::doubleValue).sum();
        double totalShare = customShares.values().stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(totalShare - totalPaid) > 0.01)
            throw new IllegalArgumentException(
                    String.format("Custom shares (%.2f) do not sum to total paid (%.2f).", totalShare, totalPaid));
        List<Expense> expenses = new ArrayList<>();
        for (Map.Entry<Person, Double> entry : payerAmounts.entrySet()) {
            Person payer  = entry.getKey();
            double amount = entry.getValue();
            double ratio  = amount / totalPaid;
            Map<Person, Double> shares = new LinkedHashMap<>();
            customShares.forEach((p, s) -> shares.put(p, roundTwo(s * ratio)));
            adjustRemainder(shares, amount, customShares.keySet().iterator().next());
            Expense expense = new Expense(description, amount, payer, payerAmounts, SplitType.CUSTOM, shares);
            trip.addExpense(expense);
            expenses.add(expense);
        }
        return expenses;
    }

    public Map<Person, Double> computeBalances(Trip trip) {
        Map<Person, Double> balances = new LinkedHashMap<>();
        for (Person m : trip.getMembers()) balances.put(m, 0.0);
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
        if (Math.abs(diff) > 0.0) shares.merge(first, diff, Double::sum);
    }
}