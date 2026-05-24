package util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.Person;
import model.Trip;

import java.util.ArrayList;
import java.util.List;

public class HomeScreen {

    private final Stage stage;
    private final DatabaseHelper db;
    private final List<String> memberNames = new ArrayList<>();
    private final ListView<String> memberListView = new ListView<>();

    public HomeScreen(Stage stage, DatabaseHelper db) {
        this.stage = stage;
        this.db    = db;
    }

    public void show() {
        // Title
        Label title = new Label("💰 Smart Expense Splitter");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        // Trip name
        Label tripLabel = new Label("Trip Name:");
        TextField tripField = new TextField();
        tripField.setPromptText("e.g. Goa Trip");
        tripField.setMaxWidth(300);

        // Member input
        Label memberLabel = new Label("Add Members:");
        TextField memberField = new TextField();
        memberField.setPromptText("Enter member name");
        memberField.setMaxWidth(220);

        Button addMemberBtn = new Button("Add");
        addMemberBtn.setOnAction(e -> {
            String name = memberField.getText().trim();
            if (!name.isEmpty() && !memberNames.contains(name)) {
                memberNames.add(name);
                memberListView.getItems().add(name);
                memberField.clear();
            }
        });

        HBox memberInput = new HBox(10, memberField, addMemberBtn);
        memberInput.setAlignment(Pos.CENTER);

        memberListView.setMaxWidth(300);
        memberListView.setPrefHeight(150);

        // Remove member button
        Button removeMemberBtn = new Button("Remove Selected");
        removeMemberBtn.setOnAction(e -> {
            String selected = memberListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                memberNames.remove(selected);
                memberListView.getItems().remove(selected);
            }
        });

        // Start Trip button
        Button startBtn = new Button("🚀 Start Trip");
        startBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        startBtn.setPrefWidth(200);
        startBtn.setOnAction(e -> {
            String tripName = tripField.getText().trim();
            if (tripName.isEmpty()) {
                showAlert("Please enter a trip name.");
                return;
            }
            if (memberNames.size() < 2) {
                showAlert("Please add at least 2 members.");
                return;
            }

            // Save to database
            int tripId = db.saveTrip(tripName);
            for (String name : memberNames) {
                db.saveMember(name, tripId);
            }

            // Build Trip object
            Trip trip = new Trip(tripName);
            for (String name : memberNames) {
                trip.addMember(new Person(name));
            }

            // Go to trip screen
            new TripScreen(stage, db, trip, tripId).show();
        });

        // Layout
        VBox layout = new VBox(15,
                title,
                new Separator(),
                tripLabel, tripField,
                memberLabel, memberInput,
                memberListView,
                removeMemberBtn,
                new Separator(),
                startBtn
        );
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #f5f5f5;");

        Scene scene = new Scene(layout, 420, 550);
        stage.setTitle("Smart Expense Splitter");
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Oops!");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}