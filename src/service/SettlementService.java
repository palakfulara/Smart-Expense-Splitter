package service;

import model.Person;

import java.util.*;

public class SettlementService {

    public record Transaction(Person from, Person to, double amount) {
        @Override
        public String toString() {
            return String.format("  %s  -->  %s  :  Rs%.2f", from.getName(), to.getName(), amount);
        }
    }

    public List<Transaction> minimizeTransactions(Map<Person, Double> balances) {
        PriorityQueue<double[]> creditors = new PriorityQueue<>((a, b) -> Double.compare(b[1], a[1]));
        PriorityQueue<double[]> debtors   = new PriorityQueue<>((a, b) -> Double.compare(a[1], b[1]));

        List<Person> people = new ArrayList<>(balances.keySet());

        for (int i = 0; i < people.size(); i++) {
            double amount = roundTwo(balances.get(people.get(i)));
            if (amount > 0.005)       creditors.offer(new double[]{i, amount});
            else if (amount < -0.005) debtors.offer(new double[]{i, amount});
        }

        List<Transaction> result = new ArrayList<>();

        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            double[] creditor = creditors.poll();
            double[] debtor   = debtors.poll();

            double transfer = roundTwo(Math.min(creditor[1], -debtor[1]));

            Person from = people.get((int) debtor[0]);
            Person to   = people.get((int) creditor[0]);
            result.add(new Transaction(from, to, transfer));

            double newCreditorBalance = roundTwo(creditor[1] - transfer);
            double newDebtorBalance   = roundTwo(debtor[1]   + transfer);

            if (newCreditorBalance > 0.005)  creditors.offer(new double[]{creditor[0], newCreditorBalance});
            if (newDebtorBalance   < -0.005) debtors.offer(new double[]{debtor[0],    newDebtorBalance});
        }

        return result;
    }

    private double roundTwo(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}