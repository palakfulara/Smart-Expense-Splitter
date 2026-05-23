package util;

import model.*;
import service.SettlementService.Transaction;

import java.util.List;
import java.util.Map;

public class DisplayHelper {

    private static final String DIVIDER      = "-".repeat(55);
    private static final String THIN_DIVIDER = ".".repeat(55);

    public static void printBanner() {
        System.out.println();
        System.out.println("=========================================================");
        System.out.println("           SMART EXPENSE SPLITTER                        ");
        System.out.println("           Split bills. No awkward maths.                ");
        System.out.println("=========================================================");
        System.out.println();
    }

    public static void printSection(String title) {
        System.out.println();
        System.out.println(DIVIDER);
        System.out.println("  " + title.toUpperCase());
        System.out.println(DIVIDER);
    }

    public static void printTripSummary(Trip trip) {
        printSection("Trip summary: " + trip.getName());
        System.out.printf("  Members    : %d%n", trip.getMembers().size());
        System.out.printf("  Expenses   : %d%n", trip.getExpenses().size());
        System.out.printf("  Total spent: Rs%.2f%n", trip.getTotalExpenses());
        System.out.println();
        System.out.println("  Members:");
        int i = 1;
        for (Person p : trip.getMembers()) {
            System.out.printf("    %d. %s%n", i++, p.getName());
        }
    }

    public static void printExpenseList(Trip trip) {
        printSection("All expenses");
        if (trip.getExpenses().isEmpty()) {
            System.out.println("  No expenses recorded yet.");
            return;
        }
        int i = 1;
        for (Expense e : trip.getExpenses()) {
            System.out.printf("  %2d. %s%n", i++, e);
            e.getParticipantShares().forEach((p, amt) ->
                    System.out.printf("       %-16s  Rs%.2f%n", p.getName(), amt));
            System.out.println("  " + THIN_DIVIDER);
        }
    }

    public static void printBalances(Map<Person, Double> balances) {
        printSection("Net balances");
        System.out.printf("  %-20s  %s%n", "Name", "Balance");
        System.out.println("  " + THIN_DIVIDER);
        balances.forEach((p, bal) -> {
            String indicator = bal > 0 ? "  gets back" : bal < 0 ? "  owes" : "  settled";
            System.out.printf("  %-20s  Rs%8.2f  %s%n", p.getName(), bal, indicator);
        });
    }

    public static void printSettlement(List<Transaction> txns) {
        printSection("Settlement plan (minimum transactions)");
        if (txns.isEmpty()) {
            System.out.println("  Everyone is settled! No transactions needed.");
            return;
        }
        System.out.printf("  %d transaction(s) needed:%n%n", txns.size());
        int i = 1;
        for (Transaction t : txns) {
            System.out.printf("  %d. %s%n", i++, t);
        }
    }

    public static void printMenu() {
        System.out.println();
        System.out.println("  What would you like to do?");
        System.out.println("  1. Add an expense");
        System.out.println("  2. View all expenses");
        System.out.println("  3. View balances");
        System.out.println("  4. View settlement plan");
        System.out.println("  5. Trip summary");
        System.out.println("  6. Exit");
        System.out.print("  > ");
    }
}