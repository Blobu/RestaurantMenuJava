package GUI;

import Menu.ReadFile;
import Entities.User;
import Entities.Role;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Pair;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class FXMain extends Application {

    // Factory-ul pentru JPA
    private EntityManagerFactory emf;

    // --- MODIFICARE: Definim stage-ul aici ca să fie vizibil peste tot ---
    private Stage primaryStage;

    @Override
    public void init() {
        try {
            emf = Persistence.createEntityManagerFactory("RestaurantPU");
        } catch (Exception e) {
            System.err.println("Eroare la initializarea bazei de date: " + e.getMessage());
        }
    }

    @Override
    public void start(Stage stage) {
        // --- MODIFICARE: Salvăm referința primită în variabila clasei ---
        this.primaryStage = stage;

        String appTitle = "Meniu Restaurant";
        String restaurantName = "Restaurant Generic";

        // Incercam sa citim numele din JSON (Optional)
        try {
            Path jsonPath = Paths.get("src/main/resources/Menu/restaurant.json");
            if (!jsonPath.toFile().exists()) {
                jsonPath = Paths.get("src/main/java/External/config.json");
            }
            if (jsonPath.toFile().exists()) {
                restaurantName = ReadFile.readNumeRestaurant(jsonPath);
                appTitle = "Meniu Restaurant " + restaurantName;
            }
        } catch (Exception e) {
            System.err.println("Nu s-a putut citi numele restaurantului: " + e.getMessage());
        }

        primaryStage.setTitle(appTitle);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        // 1. TITLU RESTAURANT
        Label titleLabel = new Label(restaurantName);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setStyle("-fx-text-fill: #333333; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 1);");
        titleLabel.setPadding(new Insets(20, 0, 40, 0));

        VBox topContainer = new VBox(titleLabel);
        topContainer.setAlignment(Pos.CENTER);
        root.setTop(topContainer);

        // 2. BUTOANE NIVEL ACCES
        VBox centerMenu = new VBox(20);
        centerMenu.setAlignment(Pos.CENTER);

        Button guestButton = new Button("Guest (Client)");
        Button staffButton = new Button("Staff (Ospătar)");
        Button adminButton = new Button("Admin (Manager)");

        // Configurare stil butoane
        Font buttonFont = Font.font("System", FontWeight.BOLD, 18);
        double buttonWidth = 250;
        double buttonHeight = 60;

        for (Button btn : new Button[]{adminButton, staffButton, guestButton}) {
            btn.setFont(buttonFont);
            btn.setPrefSize(buttonWidth, buttonHeight);
        }

        // Stiluri specifice
        guestButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        staffButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        adminButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

        // --- ACTIUNI BUTOANE ---

        // GUEST
        guestButton.setOnAction(e -> {
            GuestController guestController = new GuestController(primaryStage, this);
            guestController.show();
        });

        // STAFF
        staffButton.setOnAction(e -> handleLogin("Login Ospătar", Role.STAFF));

        // ADMIN
        adminButton.setOnAction(e -> handleLogin("Login Administrator", Role.ADMIN));

        centerMenu.getChildren().addAll(guestButton, staffButton, adminButton);
        root.setCenter(centerMenu);

        // Footer simplu
        Label footer = new Label("Sistem de Gestiune Restaurant v2.0 - JPA/Hibernate Edition");
        footer.setStyle("-fx-text-fill: gray;");
        BorderPane.setAlignment(footer, Pos.CENTER);
        root.setBottom(footer);

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Metoda generica pentru login (Staff sau Admin)
     */
    private void handleLogin(String title, Role requiredRole) {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText("Acces restricționat: " + requiredRole);

        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 50, 10, 10));

        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Parola");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Parola:"), 0, 1);
        grid.add(password, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Focus pe username field
        Platform.runLater(username::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(username.getText(), password.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(usernamePassword -> {
            String user = usernamePassword.getKey();
            String pass = usernamePassword.getValue();
            authenticateAndOpen(user, pass, requiredRole);
        });
    }

    private void authenticateAndOpen(String user, String pass, Role requiredRole) {
        EntityManager em = emf.createEntityManager();
        try {
            User foundUser = em.createQuery("SELECT u FROM User u WHERE u.username = :u AND u.password = :p", User.class)
                    .setParameter("u", user)
                    .setParameter("p", pass)
                    .getSingleResult();

            // Verificăm dacă rolul din baza de date corespunde cu cel cerut de buton
            if (foundUser.getRole() == requiredRole) {
                if (requiredRole == Role.ADMIN) {
                    AdminController admin = new AdminController(primaryStage, this);
                    admin.show();
                } else if (requiredRole == Role.STAFF) {
                    // Transmit toți parametrii necesari către StaffController, inclusiv user-ul curent
                    StaffController staff = new StaffController(primaryStage, this, foundUser);
                    staff.show();
                }
            } else {
                showError("Contul tău nu are acces de tip: " + requiredRole);
            }

        } catch (jakarta.persistence.NoResultException ex) {
            showError("Username sau parolă incorectă!");
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Eroare de sistem: " + ex.getMessage());
        } finally {
            em.close();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }

    @Override
    public void stop() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}