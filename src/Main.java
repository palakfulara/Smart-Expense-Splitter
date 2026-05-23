import model.*;
import service.*;
import util.*;

import java.util.*;

public class Main {

    private static final ExpenseService    expenseService    = new ExpenseService();
    private static final SettlementService settlementService = new SettlementService();

    public static void main(String[] args) {

        DisplayHelper.printBanner();

        String tripName = InputHelper.readLine("Enter trip name: ");
        Trip trip = new Trip(tripName);

        System.out.println();
        int memberCount = InputHelper.readInt("How many people are on this trip? ", 2, 20);
        for (int i = 1; i <= memberCount; i++) {
            String name = InputHelper.readLine("  Enter name of person " + i + ": ");
            trip.addMember(new Person(name));
        }

        System.out.println("\n  Trip \"" + tripName + "\" created with " + memberCount + " members. Let's go!");

        boolean running = true;
        while (running) {
            DisplayHelper.printMenu();
            String choice = InputHelper.sc.nextLine().trim();

            switch (choice) {
                case "1" -> addExpense(trip);
                case "2" -> DisplayHelper.printExpenseList(trip);
                case "3" -> {
                    Map<Person, Double> balances = expenseService.computeBalances(trip);
                    DisplayHelper.printBalances(balances);
                }
                case "4" -> {
                    Map<Person, Double> balances = expenseService.computeBalances(trip);
                    var txns = settlementService.minimizeTransactions(balances);
                    DisplayHelper.printSettlement(txns);
                }
                case "5" -> DisplayHelper.printTripSummary(trip);
                case "6" -> {
                    System.out.println("\n  Goodbye! Safe travels!");
                    running = false;
                }
                default -> System.out.println("  Invalid option. Please enter 1-6.");
            }
        }
    }

    private static void addExpense(Trip trip) {
        System.out.println();
        List<Person> memberList = new ArrayList<>(trip.getMembers());

        String description = InputHelper.readLine("  Expense description: ");
        double amount      = InputHelper.readDouble("  Total amount (Rs): ", 0.01);

        System.out.println("\n  Who paid?");
        for (int i = 0; i < memberList.size(); i++) {
            System.out.printf("    %d. %s%n", i + 1, memberList.get(i).getName());
        }
        int payerIdx = InputHelper.readInt("  Enter number: ", 1, memberList.size()) - 1;
        Person paidBy = memberList.get(payerIdx);

        System.out.println("\n  How to split?");
        System.out.println("    1. Equal among all members");
        System.out.println("    2. Equal among selected members");
        System.out.println("    3. By percentage");
        System.out.println("    4. Custom amounts");
        int splitChoice = InputHelper.readInt("  Choose split type: ", 1, 4);

        try {
            switch (splitChoice) {
                case 1 -> {
                    Expense e = expenseService.addEqualExpense(trip, description, amount, paidBy, memberList);
                    System.out.printf("%n  Expense added: %s%n", e);
                }
                case 2 -> {
                    List<Person> participants = selectParticipants(memberList);
                    Expense e = expenseService.addEqualExpense(trip, description, amount, paidBy, participants);
                    System.out.printf("%n  Expense added: %s%n", e);
                }
                case 3 -> {
                    Map<Person, Double> percentages = collectPercentages(memberList);
                    Expense e = expenseService.addPercentageExpense(trip, description, amount, paidBy, percentages);
                    System.out.printf("%n  Expense added: %s%n", e);
                }
                case 4 -> {
                    Map<Person, Double> customShares = collectCustomAmounts(memberList, amount);
                    Expense e = expenseService.addCustomExpense(trip, description, amount, paidBy, customShares);
                    System.out.printf("%n  Expense added: %s%n", e);
                }
            }
        } catch (IllegalArgumentException ex) {
            System.out.println("\n  Error: " + ex.getMessage());
        }
    }

    private static List<Person> selectParticipants(List<Person> members) {
        System.out.println("\n  Select participants (numbers separated by commas e.g. 1,3,4):");
        for (int i = 0; i < members.size(); i++) {
            System.out.printf("    %d. %s%n", i + 1, members.get(i).getName());
        }
        while (true) {
            String input = InputHelper.readLine("  Your selection: ");
            try {
                List<Person> selected = new ArrayList<>();
                for (String part : input.split(",")) {
                    int idx = Integer.parseInt(part.trim()) - 1;
                    if (idx < 0 || idx >= members.size()) throw new NumberFormatException();
                    selected.add(members.get(idx));
                }
                if (!selected.isEmpty()) return selected;
            } catch (NumberFormatException e) {
                System.out.println("  Invalid selection. Try again.");
            }
        }
    }

    private static Map<Person, Double> collectPercentages(List<Person> members) {
        System.out.println("\n  Enter percentage for each member (must total 100):");
        Map<Person, Double> percentages = new LinkedHashMap<>();
        for (Person p : members) {
            double pct = InputHelper.readDouble("    " + p.getName() + " (%): ", 0);
            percentages.put(p, pct);
        }
        return percentages;
    }

    private static Map<Person, Double> collectCustomAmounts(List<Person> members, double total) {
        System.out.printf("\n  Enter exact amount for each member (must total Rs%.2f):%n", total);
        Map<Person, Double> shares = new LinkedHashMap<>();
        for (Person p : members) {
            double amt = InputHelper.readDouble("    " + p.getName() + " (Rs): ", 0);
            shares.put(p, amt);
        }
        return shares;
    }
}