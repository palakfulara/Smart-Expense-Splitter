Smart Expense Splitter
A console-based Java application to track and split trip expenses among friends — fairly and with minimal hassle.
---
Features
Create a named trip with any number of members (2–20)
Add expenses with 4 split modes:
Equal — divided evenly among all members
Selected equal — divided evenly among a chosen subset
Percentage — each person pays a defined % of the total
Custom — each person pays an exact amount
View all expenses with per-person breakdown
View net balances (who is owed, who owes)
Minimum-transaction settlement plan — the fewest transfers needed to fully settle all debts
---
Project Structure
```
SmartExpenseSplitter/
└── src/
    ├── Main.java                  ← entry point & menu loop
    ├── model/
    │   ├── Person.java            ← trip member
    │   ├── Expense.java           ← one expense with per-person shares
    │   ├── Trip.java              ← holds members + expenses
    │   └── SplitType.java         ← enum: EQUAL, PERCENTAGE, CUSTOM
    ├── service/
    │   ├── ExpenseService.java    ← split logic + balance computation
    │   └── SettlementService.java ← greedy minimum-transactions algorithm
    └── util/
        ├── InputHelper.java       ← safe console input (int, double, y/n)
        └── DisplayHelper.java     ← formatted output helpers
```
---
Compile & Run
```bash
# Compile
javac -d out -sourcepath src src/Main.java src/model/*.java src/service/*.java src/util/*.java

# Run from compiled classes
java -cp out Main

# OR run the prebuilt JAR
java -jar SmartExpenseSplitter.jar
```
Requires Java 17+ (uses records in SettlementService).
---
Sample Session
```
Enter trip name: Goa Trip
How many people are on this trip? 3
  Enter name of person 1: Alice
  Enter name of person 2: Bob
  Enter name of person 3: Charlie

  Trip "Goa Trip" created with 3 members. Let's go!

  > 1   (Add expense)
  Expense description: Hotel Room
  Total amount (Rs): 3000
  Who paid? 1 (Alice)
  How to split? 1 (Equal among all)

  > 4   (Settlement plan)
  2 transaction(s) needed:
    1. Bob  →  Alice  :  Rs 1000.00
    2. Charlie  →  Alice  :  Rs 1000.00
```
---
Key Algorithms
Balance computation (`ExpenseService`)
For each expense, the payer's balance increases by the full amount, and every participant's balance decreases by their share. Net result = total paid − total owed.
Minimum transactions (`SettlementService`)
Uses a greedy algorithm with two max-heaps (creditors and debtors). At each step, the largest debtor pays the largest creditor as much as possible. This minimises the number of transfers needed to zero out all balances.
