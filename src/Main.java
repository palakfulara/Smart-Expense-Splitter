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
        double totalAmount = InputHelper.readDouble("  Total amount (Rs): ", 0.01);

        System.out.println("\n  How many people paid for this expense?");
        int payerCount = InputHelper.readInt("  Number of payers: ", 1, memberList.size());

        Map<Person, Double> payerAmounts = new LinkedHashMap<>();

        if (payerCount == 1) {
            System.out.println("\n  Who paid?");
            for (int i = 0; i < memberList.size(); i++)
                System.out.printf("    %d. %s%n", i + 1, memberList.get(i).getName());
            int idx = InputHelper.readInt("  Enter number: ", 1, memberList.size()) - 1;
            payerAmounts.put(memberList.get(idx), totalAmount);
        } else {
            System.out.println("\n  Select payers and how much each paid:");
            for (int i = 0; i < memberList.size(); i++)
                System.out.printf("    %d. %s%n", i + 1, memberList.get(i).getName());

            double remaining = totalAmount;
            for (int p = 1; p <= payerCount; p++) {
                int idx = InputHelper.readInt("  Payer " + p + " - enter number: ", 1, memberList.size()) - 1;
                Person payer = memberList.get(idx);
                if (payerAmounts.containsKey(payer)) {
                    System.out.println("  This person is already added. Choose someone else.");
                    p--;
                    continue;
                }
                double amt;
                if (p == payerCount) {
                    amt = roundTwo(remaining);
                    System.out.printf("  Amount paid by %s: Rs%.2f (remaining)%n", payer.getName(), amt);
                } else {
                    amt = InputHelper.readDouble("  Amount paid by " + payer.getName() + " (Rs): ", 0.01);
                    if (amt >= remaining) {
                        System.out.printf("  Amount must be less than Rs%.2f. Try again.%n", remaining);
                        p--;
                        continue;
                    }
                }
                payerAmounts.put(payer, amt);
                remaining = roundTwo(remaining - amt);
            }
        }

        System.out.println("\n  How to split?");
        System.out.println("    1. Equal among all members");
        System.out.println("    2. Equal among selected members");
        System.out.println("    3. By percentage");
        System.out.println("    4. Custom amounts");
        int splitChoice = InputHelper.readInt("  Choose split type: ", 1, 4);

        try {
            boolean multiPayer  = payerAmounts.size() > 1;
            Person singlePayer  = multiPayer ? null : payerAmounts.keySet().iterator().next();

            switch (splitChoice) {
                case 1 -> {
                    if (multiPayer) {
                        var list = expenseService.addEqualExpenseMultiplePayers(trip, description, payerAmounts, memberList);
                        System.out.printf("%n  Expense added: %s%n", list.get(0));
                    } else {
                        var e = expenseService.addEqualExpense(trip, description, totalAmount, singlePayer, memberList);
                        System.out.printf("%n  Expense added: %s%n", e);
                    }
                }
                case 2 -> {
                    List<Person> participants = selectParticipants(memberList);
                    if (multiPayer) {
                        var list = expenseService.addEqualExpenseMultiplePayers(trip, description, payerAmounts, participants);
                        System.out.printf("%n  Expense added: %s%n", list.get(0));
                    } else {
                        var e = expenseService.addEqualExpense(trip, description, totalAmount, singlePayer, participants);
                        System.out.printf("%n  Expense added: %s%n", e);
                    }
                }
                case 3 -> {
                    Map<Person, Double> percentages = collectPercentages(memberList);
                    if (multiPayer) {
                        var list = expenseService.addPercentageExpenseMultiplePayers(trip, description, payerAmounts, percentages);
                        System.out.printf("%n  Expense added: %s%n", list.get(0));
                    } else {
                        var e = expenseService.addPercentageExpense(trip, description, totalAmount, singlePayer, percentages);
                        System.out.printf("%n  Expense added: %s%n", e);
                    }
                }
                case 4 -> {
                    Map<Person, Double> customShares = collectCustomAmounts(memberList, totalAmount);
                    if (multiPayer) {
                        var list = expenseService.addCustomExpenseMultiplePayers(trip, description, payerAmounts, customShares);
                        System.out.printf("%n  Expense added: %s%n", list.get(0));
                    } else {
                        var e = expenseService.addCustomExpense(trip, description, totalAmount, singlePayer, customShares);
                        System.out.printf("%n  Expense added: %s%n", e);
                    }
                }
            }
        } catch (IllegalArgumentException ex) {
            System.out.println("\n  Error: " + ex.getMessage());
        }
    }

    private static List<Person> selectParticipants(List<Person> members) {
        System.out.println("\n  Select participants (numbers separated by commas e.g. 1,3,4):");
        for (int i = 0; i < members.size(); i++)
            System.out.printf("    %d. %s%n", i + 1, members.get(i).getName());
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
        for (Person p : members)
            percentages.put(p, InputHelper.readDouble("    " + p.getName() + " (%): ", 0));
        return percentages;
    }

    private static Map<Person, Double> collectCustomAmounts(List<Person> members, double total) {
        System.out.printf("\n  Enter exact amount for each member (must total Rs%.2f):%n", total);
        Map<Person, Double> shares = new LinkedHashMap<>();
        for (Person p : members)
            shares.put(p, InputHelper.readDouble("    " + p.getName() + " (Rs): ", 0));
        return shares;
    }

    private static double roundTwo(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}