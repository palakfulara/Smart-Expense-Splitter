package util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.Expense;
import model.Person;
import model.Trip;
import service.ExpenseService;
import service.SettlementService;

import java.util.*;

public class TripScreen {

    private final Stage stage;
    private final DatabaseHelper db;
    private final Trip trip;
    private final int tripId;
    private final ExpenseService expenseService = new ExpenseService();
    private final SettlementService settlementService = new SettlementService();
    private final ObservableList<ExpenseRow> expenseRows = FXCollections.observableArrayList();
    private Label totalLabel;

    public TripScreen(Stage stage, DatabaseHelper db, Trip trip, int tripId) {
        this.stage  = stage;
        this.db     = db;
        this.trip   = trip;
        this.tripId = tripId;
    }

    public void show() {
        Label title = new Label("Trip: " + trip.getName());
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        Label membersLabel = new Label("Members: " + getMemberNames());
        membersLabel.setFont(Font.font("Arial", 13));

        TableView<ExpenseRow> table = new TableView<>(expenseRows);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ExpenseRow, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<ExpenseRow, String> amtCol = new TableColumn<>("Amount (Rs)");
        amtCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<ExpenseRow, String> paidCol = new TableColumn<>("Paid By");
        paidCol.setCellValueFactory(new PropertyValueFactory<>("paidBy"));

        TableColumn<ExpenseRow, String> splitCol = new TableColumn<>("Split Type");
        splitCol.setCellValueFactory(new PropertyValueFactory<>("splitType"));

        table.getColumns().addAll(descCol, amtCol, paidCol, splitCol);
        table.setPrefHeight(250);

        Button addExpenseBtn = new Button("➕ Add Expense");
        addExpenseBtn.setPrefWidth(160);
        addExpenseBtn.setOnAction(e -> showAddExpenseDialog());

        Button balancesBtn = new Button("📊 View Balances");
        balancesBtn.setPrefWidth(160);
        balancesBtn.setOnAction(e -> showBalances());

        Button settleBtn = new Button("✅ Settlement Plan");
        settleBtn.setPrefWidth(160);
        settleBtn.setOnAction(e -> showSettlement());

        Button backBtn = new Button("🔙 Back");
        backBtn.setOnAction(e -> new HomeScreen(stage, db).show());

        HBox buttons = new HBox(15, addExpenseBtn, balancesBtn, settleBtn, backBtn);
        buttons.setAlignment(Pos.CENTER);

        totalLabel = new Label("Total Spent: Rs 0.00");
        totalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        updateTotal();

        VBox layout = new VBox(15,
                title, membersLabel,
                new Separator(),
                new Label("Expenses:"),
                table,
                totalLabel,
                new Separator(),
                buttons
        );
        layout.setPadding(new Insets(25));
        layout.setStyle("-fx-background-color: #f5f5f5;");

        Scene scene = new Scene(layout, 650, 560);
        stage.setScene(scene);
    }

    private void showAddExpenseDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Expense");
        dialog.setHeaderText("Enter expense details");

        TextField descField = new TextField();
        descField.setPromptText("e.g. Hotel Room");

        TextField amountField = new TextField();
        amountField.setPromptText("e.g. 3000");

        // Number of payers spinner
        Label payerCountLabel = new Label("Number of payers:");
        Spinner<Integer> payerCountSpinner = new Spinner<>(1, trip.getMembers().size(), 1);
        payerCountSpinner.setPrefWidth(80);

        // Payer input area - changes based on payer count
        VBox payerBox = new VBox(6);
        List<Person> memberList = new ArrayList<>(trip.getMembers());

        // Rebuild payer inputs when count changes
        Runnable rebuildPayers = () -> {
            payerBox.getChildren().clear();
            int count = payerCountSpinner.getValue();
            if (count == 1) {
                ComboBox<String> singlePayer = new ComboBox<>();
                singlePayer.setId("singlePayer");
                for (Person p : memberList) singlePayer.getItems().add(p.getName());
                singlePayer.setPromptText("Who paid?");
                singlePayer.setPrefWidth(200);
                payerBox.getChildren().add(singlePayer);
            } else {
                payerBox.getChildren().add(new Label("Select payer and amount each paid:"));
                for (int i = 1; i <= count; i++) {
                    HBox row = new HBox(8);
                    ComboBox<String> nameBox = new ComboBox<>();
                    nameBox.setId("payer_name_" + i);
                    for (Person p : memberList) nameBox.getItems().add(p.getName());
                    nameBox.setPromptText("Payer " + i);
                    nameBox.setPrefWidth(130);

                    TextField amtField = new TextField();
                    amtField.setId("payer_amt_" + i);
                    amtField.setPromptText("Amount (Rs)");
                    amtField.setPrefWidth(100);

                    row.getChildren().addAll(nameBox, amtField);
                    payerBox.getChildren().add(row);
                }
            }
        };

        rebuildPayers.run();
        payerCountSpinner.valueProperty().addListener((obs, oldVal, newVal) -> rebuildPayers.run());

        // Split type
        ComboBox<String> splitBox = new ComboBox<>();
        splitBox.getItems().addAll(
                "Equal among all",
                "Equal among selected",
                "By percentage",
                "Custom amounts"
        );
        splitBox.setValue("Equal among all");

        // Extra input area for split type
        VBox extraInputBox = new VBox(8);

        splitBox.setOnAction(e -> {
            extraInputBox.getChildren().clear();
            String selected = splitBox.getValue();
            if (selected.equals("Equal among selected")) {
                extraInputBox.getChildren().add(new Label("Select participants:"));
                for (Person p : memberList) {
                    CheckBox cb = new CheckBox(p.getName());
                    cb.setSelected(true);
                    cb.setUserData(p);
                    extraInputBox.getChildren().add(cb);
                }
            } else if (selected.equals("By percentage")) {
                extraInputBox.getChildren().add(new Label("Enter % for each (must total 100):"));
                for (Person p : memberList) {
                    TextField tf = new TextField();
                    tf.setPromptText(p.getName() + " (%)");
                    tf.setUserData(p);
                    extraInputBox.getChildren().add(tf);
                }
            } else if (selected.equals("Custom amounts")) {
                extraInputBox.getChildren().add(new Label("Enter exact amount for each:"));
                for (Person p : memberList) {
                    TextField tf = new TextField();
                    tf.setPromptText(p.getName() + " (Rs)");
                    tf.setUserData(p);
                    extraInputBox.getChildren().add(tf);
                }
            }
        });

        // Scrollable content
        VBox allContent = new VBox(10,
                new Label("Description:"), descField,
                new Label("Total Amount (Rs):"), amountField,
                new Separator(),
                payerCountLabel, payerCountSpinner,
                new Label("Payer details:"), payerBox,
                new Separator(),
                new Label("Split type:"), splitBox,
                extraInputBox
        );
        allContent.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(allContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(420);
        scrollPane.setStyle("-fx-background-color: transparent;");

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(380);

        dialog.showAndWait().ifPresent(result -> {
            if (result != ButtonType.OK) return;
            try {
                String desc      = descField.getText().trim();
                double totalAmt  = Double.parseDouble(amountField.getText().trim());
                String splitType = splitBox.getValue();
                int payerCount   = payerCountSpinner.getValue();

                if (desc.isEmpty()) { showAlert("Please enter a description."); return; }

                // Build payer amounts map
                Map<Person, Double> payerAmounts = new LinkedHashMap<>();

                if (payerCount == 1) {
                    // Single payer
                    ComboBox<String> singlePayer = (ComboBox<String>) payerBox.getChildren()
                            .stream().filter(n -> n instanceof ComboBox).findFirst().orElse(null);
                    if (singlePayer == null || singlePayer.getValue() == null) {
                        showAlert("Please select who paid."); return;
                    }
                    Person payer = memberList.stream()
                            .filter(p -> p.getName().equals(singlePayer.getValue()))
                            .findFirst().orElse(null);
                    payerAmounts.put(payer, totalAmt);
                } else {
                    // Multiple payers
                    double remaining = totalAmt;
                    List<HBox> rows = payerBox.getChildren().stream()
                            .filter(n -> n instanceof HBox)
                            .map(n -> (HBox) n)
                            .toList();
                    for (int i = 0; i < rows.size(); i++) {
                        HBox row = rows.get(i);
                        ComboBox<String> nameBox = (ComboBox<String>) row.getChildren().get(0);
                        TextField amtField = (TextField) row.getChildren().get(1);
                        if (nameBox.getValue() == null) { showAlert("Please select all payers."); return; }
                        Person payer = memberList.stream()
                                .filter(p -> p.getName().equals(nameBox.getValue()))
                                .findFirst().orElse(null);
                        double amt = (i == rows.size() - 1)
                                ? remaining
                                : Double.parseDouble(amtField.getText().trim());
                        payerAmounts.put(payer, amt);
                        remaining -= amt;
                    }
                }

                // Build split
                boolean multiPayer = payerAmounts.size() > 1;
                Person singlePayerPerson = multiPayer ? null : payerAmounts.keySet().iterator().next();
                List<Expense> expenses = new ArrayList<>();

                switch (splitType) {
                    case "Equal among all" -> {
                        if (multiPayer)
                            expenses.addAll(expenseService.addEqualExpenseMultiplePayers(trip, desc, payerAmounts, memberList));
                        else
                            expenses.add(expenseService.addEqualExpense(trip, desc, totalAmt, singlePayerPerson, memberList));
                    }
                    case "Equal among selected" -> {
                        List<Person> selected = new ArrayList<>();
                        for (var node : extraInputBox.getChildren())
                            if (node instanceof CheckBox cb && cb.isSelected())
                                selected.add((Person) cb.getUserData());
                        if (selected.isEmpty()) { showAlert("Select at least one person."); return; }
                        if (multiPayer)
                            expenses.addAll(expenseService.addEqualExpenseMultiplePayers(trip, desc, payerAmounts, selected));
                        else
                            expenses.add(expenseService.addEqualExpense(trip, desc, totalAmt, singlePayerPerson, selected));
                    }
                    case "By percentage" -> {
                        Map<Person, Double> percentages = new LinkedHashMap<>();
                        for (var node : extraInputBox.getChildren())
                            if (node instanceof TextField tf && tf.getUserData() instanceof Person p)
                                percentages.put(p, Double.parseDouble(tf.getText().trim()));
                        if (multiPayer)
                            expenses.addAll(expenseService.addPercentageExpenseMultiplePayers(trip, desc, payerAmounts, percentages));
                        else
                            expenses.add(expenseService.addPercentageExpense(trip, desc, totalAmt, singlePayerPerson, percentages));
                    }
                    case "Custom amounts" -> {
                        Map<Person, Double> custom = new LinkedHashMap<>();
                        for (var node : extraInputBox.getChildren())
                            if (node instanceof TextField tf && tf.getUserData() instanceof Person p)
                                custom.put(p, Double.parseDouble(tf.getText().trim()));
                        if (multiPayer)
                            expenses.addAll(expenseService.addCustomExpenseMultiplePayers(trip, desc, payerAmounts, custom));
                        else
                            expenses.add(expenseService.addCustomExpense(trip, desc, totalAmt, singlePayerPerson, custom));
                    }
                }

                // Save to DB and table
                String payerDisplay = multiPayer ? payerAmounts.size() + " payers" : singlePayerPerson.getName();
                db.saveExpense(desc, totalAmt, payerDisplay, splitType, tripId);
                expenseRows.add(new ExpenseRow(desc, String.format("%.2f", totalAmt), payerDisplay, splitType));
                updateTotal();

            } catch (NumberFormatException ex) {
                showAlert("Please enter valid numbers.");
            } catch (IllegalArgumentException ex) {
                showAlert(ex.getMessage());
            }
        });
    }

    private void showBalances() {
        Map<Person, Double> balances = expenseService.computeBalances(trip);
        StringBuilder sb = new StringBuilder();
        balances.forEach((p, bal) -> {
            String status = bal > 0 ? "gets back  Rs " + String.format("%.2f", bal)
                    : bal < 0 ? "owes  Rs " + String.format("%.2f", Math.abs(bal))
                    : "is settled ✔";
            sb.append(p.getName()).append("  →  ").append(status).append("\n");
        });
        showInfo("Net Balances", sb.toString());
    }

    private void showSettlement() {
        Map<Person, Double> balances = expenseService.computeBalances(trip);
        List<SettlementService.Transaction> txns = settlementService.minimizeTransactions(balances);
        if (txns.isEmpty()) {
            showInfo("Settlement", "Everyone is settled! No payments needed.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (SettlementService.Transaction t : txns) {
            sb.append(i++).append(". ")
                    .append(t.from().getName())
                    .append("  →  pay  Rs")
                    .append(String.format("%.2f", t.amount()))
                    .append("  to  ")
                    .append(t.to().getName())
                    .append("\n");
        }
        showInfo("Settlement Plan", sb.toString());
    }

    private void updateTotal() {
        if (totalLabel != null)
            totalLabel.setText("Total Spent: Rs " + String.format("%.2f", trip.getTotalExpenses()));
    }

    private String getMemberNames() {
        StringBuilder sb = new StringBuilder();
        for (Person p : trip.getMembers()) sb.append(p.getName()).append(", ");
        if (sb.length() > 2) sb.setLength(sb.length() - 2);
        return sb.toString();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Oops!");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public static class ExpenseRow {
        private final String description;
        private final String amount;
        private final String paidBy;
        private final String splitType;

        public ExpenseRow(String description, String amount, String paidBy, String splitType) {
            this.description = description;
            this.amount      = amount;
            this.paidBy      = paidBy;
            this.splitType   = splitType;
        }

        public String getDescription() { return description; }
        public String getAmount()      { return amount; }
        public String getPaidBy()      { return paidBy; }
        public String getSplitType()   { return splitType; }
    }
}