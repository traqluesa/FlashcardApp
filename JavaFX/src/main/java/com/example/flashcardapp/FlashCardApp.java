package com.example.flashcardapp;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.*;
import java.util.*;

public class FlashCardApp extends Application {
    private Stage stage;
    private String currentUser;
    private List<FlashCard> cards = new ArrayList<>();
    private int cardIndex = 0;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setTitle("FlashCard App");
        showLoginPage();
        stage.show();
    }
    private void showLoginPage() {
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        Label title = new Label("FlashCard App");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField username = new TextField();
        username.setPromptText("Username");
        username.setMaxWidth(200);

        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        password.setMaxWidth(200);

        Button loginBtn = new Button("Login");
        Button registerBtn = new Button("Register");

        loginBtn.setOnAction(e -> {
            if (login(username.getText(), password.getText())) {
                currentUser = username.getText();
                loadCards();
                showMainPage();
            } else {
                showMessage("Login failed!");
            }
        });

        registerBtn.setOnAction(e -> {
            if (register(username.getText(), password.getText())) {
                showMessage("Registration successful!");
            } else {
                showMessage("Registration failed!");
            }
        });

        root.getChildren().addAll(title, username, password, loginBtn, registerBtn);
        stage.setScene(new Scene(root, 400, 300));
    }
    private void showMainPage() {
        BorderPane root = new BorderPane();

        MenuBar menuBar = new MenuBar();
        Menu menu = new Menu("Menu");
        MenuItem logoutItem = new MenuItem("Logout");
        logoutItem.setOnAction(e -> showLoginPage());
        menu.getItems().add(logoutItem);
        menuBar.getMenus().add(menu);

        VBox leftBox = new VBox(10);
        leftBox.setPadding(new Insets(10));
        leftBox.setPrefWidth(280);

        Label addTitle = new Label("Add New Card");
        addTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TextField questionField = new TextField();
        questionField.setPromptText("Question");

        TextField answerField = new TextField();
        answerField.setPromptText("Answer");

        Button addBtn = new Button("Add Card");
        addBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addBtn.setOnAction(e -> {
            String q = questionField.getText();
            String a = answerField.getText();
            if (!q.isEmpty() && !a.isEmpty()) {
                cards.add(new FlashCard(q, a));
                saveCards();
                questionField.clear();
                answerField.clear();
                showMessage("Card added!");
                updateDisplay();
            }
        });

        Separator separator = new Separator();

        Label deleteTitle = new Label("Delete Card");
        deleteTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        ComboBox<String> cardSelector = new ComboBox<>();
        cardSelector.setPromptText("Select card to delete");
        cardSelector.setPrefWidth(250);

        Button refreshBtn = new Button("Refresh List");
        refreshBtn.setOnAction(e -> updateCardSelector(cardSelector));

        Button deleteBtn = new Button("Delete Selected Card");
        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> {
            int selectedIndex = cardSelector.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < cards.size()) {

                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Delete Card");
                confirmAlert.setHeaderText("Are you sure?");
                confirmAlert.setContentText("Do you want to delete this card?\n\nQ: " +
                        cards.get(selectedIndex).getQuestion() + "\nA: " +
                        cards.get(selectedIndex).getAnswer());

                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    cards.remove(selectedIndex);
                    saveCards();

                    if (cardIndex >= cards.size() && cards.size() > 0) {
                        cardIndex = cards.size() - 1;
                    } else if (cards.isEmpty()) {
                        cardIndex = 0;
                    }

                    updateDisplay();
                    updateCardSelector(cardSelector);
                    showMessage("Card deleted!");
                }
            } else {
                showMessage("Please select a card to delete!");
            }
        });

        leftBox.getChildren().addAll(addTitle, questionField, answerField, addBtn,
                separator, deleteTitle, cardSelector, refreshBtn, deleteBtn);

        VBox centerBox = new VBox(10);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(20));

        Label cardLabel = new Label("No cards yet!");
        cardLabel.setStyle("-fx-font-size: 16px; -fx-padding: 20; -fx-border-color: gray; -fx-background-color: #f9f9f9;");
        cardLabel.setPrefSize(350, 120);
        cardLabel.setAlignment(Pos.CENTER);
        cardLabel.setWrapText(true);

        Button showAnswerBtn = new Button("Show Answer");
        Button nextBtn = new Button("Next");
        Button prevBtn = new Button("Previous");

        showAnswerBtn.setOnAction(e -> {
            if (!cards.isEmpty()) {
                FlashCard card = cards.get(cardIndex);
                if (cardLabel.getText().startsWith("Q:")) {
                    cardLabel.setText("A: " + card.getAnswer());
                } else {
                    cardLabel.setText("Q: " + card.getQuestion());
                }
            }
        });

        nextBtn.setOnAction(e -> {
            if (!cards.isEmpty()) {
                cardIndex = (cardIndex + 1) % cards.size();
                updateDisplay();
            }
        });

        prevBtn.setOnAction(e -> {
            if (!cards.isEmpty()) {
                cardIndex = (cardIndex - 1 + cards.size()) % cards.size();
                updateDisplay();
            }
        });

        HBox buttons = new HBox(5);
        buttons.setAlignment(Pos.CENTER);
        buttons.getChildren().addAll(prevBtn, showAnswerBtn, nextBtn);

        Label cardCounter = new Label();
        cardCounter.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");

        centerBox.getChildren().addAll(cardLabel, buttons, cardCounter);

        root.setTop(menuBar);
        root.setLeft(leftBox);
        root.setCenter(centerBox);

        stage.setScene(new Scene(root, 700, 450));
        updateDisplay();
        updateCardSelector(cardSelector);
    }

    private void updateDisplay() {
        Scene scene = stage.getScene();
        if (scene != null && scene.getRoot() instanceof BorderPane) {
            BorderPane root = (BorderPane) scene.getRoot();
            VBox centerBox = (VBox) root.getCenter();
            Label cardLabel = (Label) centerBox.getChildren().get(0);
            Label cardCounter = (Label) centerBox.getChildren().get(2);

            if (cards.isEmpty()) {
                cardLabel.setText("No cards yet!");
                cardCounter.setText("0 cards");
            } else {
                cardLabel.setText("Q: " + cards.get(cardIndex).getQuestion());
                cardCounter.setText("Card " + (cardIndex + 1) + " of " + cards.size());
            }
        }
    }

    private void updateCardSelector(ComboBox<String> cardSelector) {
        cardSelector.getItems().clear();
        for (int i = 0; i < cards.size(); i++) {
            FlashCard card = cards.get(i);
            String preview = card.getQuestion();
            if (preview.length() > 40) {
                preview = preview.substring(0, 37) + "...";
            }
            cardSelector.getItems().add((i + 1) + ". " + preview);
        }
    }
    private boolean login(String username, String password) {
        try (Scanner scanner = new Scanner(new File("users.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(":");
                if (parts[0].equals(username) && parts[1].equals(password)) {
                    return true;
                }
            }
        } catch (FileNotFoundException e) {

        }
        return false;
    }

    private boolean register(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) return false;

        // Check if user exists
        if (login(username, "")) return false;

        try (PrintWriter writer = new PrintWriter(new FileWriter("users.txt", true))) {
            writer.println(username + ":" + password);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void loadCards() {
        cards.clear();
        try (Scanner scanner = new Scanner(new File(currentUser + "_cards.txt"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    cards.add(new FlashCard(parts[0], parts[1]));
                }
            }
        } catch (FileNotFoundException e) {

        }
    }

    private void saveCards() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(currentUser + "_cards.txt"))) {
            for (FlashCard card : cards) {
                writer.println(card.getQuestion() + "|" + card.getAnswer());
            }
        } catch (IOException e) {
            showMessage("Save failed!");
        }
    }

    private void showMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

class FlashCard {
    private String question;
    private String answer;

    public FlashCard(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public String getQuestion() { return question; }
    public String getAnswer() { return answer; }
}