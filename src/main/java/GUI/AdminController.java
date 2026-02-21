package GUI;

import Entities.*;
import Products.Produs;
import Services.AdminService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task; // Importanț pentru Background Threads
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class AdminController {

    private final Stage stage;
    private final FXMain mainApp;
    private final AdminService adminService;

    // --- CONCURRENCY ---
    // Executorul care va rula task-urile pe thread-uri separate
    private final ExecutorService executorService;

    // --- UI COMPONENTS ---
    private TableView<User> staffTable;
    private TableView<Produs> menuTable;
    private TableView<Comanda> historyTable;

    // Componente pentru Loading (Feedback Vizual)
    private VBox loadingOverlay;
    private ProgressIndicator spinner;
    private Label loadingLabel;
    private StackPane rootStack; // Root-ul principal care suprapune spinner-ul peste UI

    public AdminController(Stage stage, FXMain mainApp) {
        this.stage = stage;
        this.mainApp = mainApp;
        this.adminService = new AdminService();

        // Inițializăm un pool de thread-uri (CachedThreadPool e bun pt task-uri scurte și variabile)
        this.executorService = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true); // Se închid automat când aplicația se închide
            return t;
        });
    }

    public void show() {
        // --- FIX CRITIC: Inițializăm variabilele necesare pentru async PRIMELE ---

        // 1. Inițializăm overlay-ul de încărcare
        createLoadingOverlay();

        // 2. Inițializăm rootStack-ul ACUM (gol momentan),
        // ca să nu fie null când refreshStaffTable() încearcă să facă rootStack.setDisable(true)
        rootStack = new StackPane();

        // 3. Construim UI-ul Principal (care va declanșa încărcările de date)
        BorderPane mainContent = new BorderPane();

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);


        // Când refreshStaffTable() va rula, rootStack există deja în memorie.
        tabPane.getTabs().addAll(
                new Tab("Personal (HR)", createStaffContent()),
                new Tab("Editare Meniu", createMenuContent()),
                new Tab("Strategii Preț", createOffersContent()),
                new Tab("Istoric Comenzi", createHistoryContent())
        );
        mainContent.setCenter(tabPane);

        // --- Bara de sus ---
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: #333;");
        Label lblTitle = new Label("PANOU ADMINISTRARE");
        lblTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnLogout = new Button("Deconectare");
        btnLogout.setOnAction(e -> {
            try { mainApp.start(stage); } catch (Exception ex) { ex.printStackTrace(); }
        });
        topBar.getChildren().addAll(lblTitle, spacer, btnLogout);
        mainContent.setTop(topBar);

        // 4. Asamblăm StackPane-ul final
        // Adăugăm mainContent primul (dedesubt) și loadingOverlay al doilea (deasupra)
        rootStack.getChildren().addAll(mainContent, loadingOverlay);

        Scene scene = new Scene(rootStack, 1000, 700);
        stage.setScene(scene);
        stage.setTitle("Restaurant Manager - Admin Mode (Async UI)");
    }

    // --- METODA MAGICĂ PENTRU CONCURRENCY ---
    /**
     * Această metodă execută orice acțiune pe un fir de execuție separat,
     * arată spinner-ul și apoi actualizează UI-ul.
     * * @param <T> Tipul de date returnat de operațiunea grea
     * @param backgroundAction Funcția care durează mult (SQL, IO, sleep)
     * @param onSuccessAction Ce facem cu rezultatul în UI (pe JavaFX Thread)
     */
    private <T> void executeAsync(Callable<T> backgroundAction, Consumer<T> onSuccessAction) {
        // 1. Activăm Loading-ul
        loadingOverlay.setVisible(true);
        rootStack.setDisable(true); // Dezactivăm click-urile pe spate

        // 2. Creăm Task-ul JavaFX
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                // SIMULARE ÎNTÂRZIERE (pentru demonstrație)

                //Thread.sleep(1000);

                // Executăm logica reală (DB, Service)
                return backgroundAction.call();
            }
        };

        // 3. Ce se întâmplă când reușește
        task.setOnSucceeded(e -> {
            loadingOverlay.setVisible(false);
            rootStack.setDisable(false);
            // Executăm callback-ul primit ca parametru cu rezultatul obținut
            onSuccessAction.accept(task.getValue());
        });

        // 4. Ce se întâmplă când eșuează
        task.setOnFailed(e -> {
            loadingOverlay.setVisible(false);
            rootStack.setDisable(false);
            Throwable ex = task.getException();
            showAlert("Eroare Critică", ex.getMessage());
            ex.printStackTrace();
        });

        // 5. Trimitem Task-ul la Executor
        executorService.submit(task);
    }

    // Varianta overload pentru acțiuni care nu returnează nimic (void)
    private void executeAsync(Runnable backgroundAction, Runnable onSuccessAction) {
        executeAsync(() -> {
            backgroundAction.run();
            return null;
        }, (result) -> onSuccessAction.run());
    }

    private void createLoadingOverlay() {
        spinner = new ProgressIndicator();
        spinner.setMaxSize(50, 50);
        loadingLabel = new Label("Se procesează datele...");
        loadingLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        loadingOverlay = new VBox(15, spinner, loadingLabel);
        loadingOverlay.setAlignment(Pos.CENTER);
        loadingOverlay.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7);"); // Alb semi-transparent
        loadingOverlay.setVisible(false); // Ascuns la start
    }

    // =============================================================
    // 1. TAB PERSONAL (Async)
    // =============================================================
    private VBox createStaffContent() {
        staffTable = new TableView<>();
        staffTable.getColumns().add(createCol("ID", "id"));
        staffTable.getColumns().add(createCol("Username", "username"));
        staffTable.getColumns().add(createCol("Rol", "role"));

        // Încărcare asincronă la pornire
        refreshStaffTable();

        TextField txtUser = new TextField(); txtUser.setPromptText("Username nou");
        PasswordField txtPass = new PasswordField(); txtPass.setPromptText("Parola");
        Button btnAdd = new Button("Angajează Ospătar");

        btnAdd.setOnAction(e -> {
            // Executăm angajarea ASINCRON
            executeAsync(
                    () -> {
                        try {
                            adminService.hireStaff(txtUser.getText(), txtPass.getText());
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }, // Background
                    () -> { // UI Update
                        refreshStaffTable();
                        txtUser.clear(); txtPass.clear();
                        showAlert("Succes", "Angajat adăugat!");
                    }
            );
        });

        Button btnDelete = new Button("Concediază");
        btnDelete.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
        btnDelete.setOnAction(e -> deleteStaff());

        HBox controls = new HBox(10, txtUser, txtPass, btnAdd, new Separator(), btnDelete);
        controls.setPadding(new Insets(10));

        return new VBox(10, staffTable, controls);
    }

    private void deleteStaff() {
        User selected = staffTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Sigur vrei să ștergi angajatul " + selected.getUsername() + "?",
                ButtonType.YES, ButtonType.NO);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                executeAsync(
                        () -> {
                            try {
                                adminService.fireStaff(selected);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        },
                        () -> {
                            refreshStaffTable();
                            showAlert("Succes", "Angajat șters.");
                        }
                );
            }
        });
    }

    private void refreshStaffTable() {
        // Loading label customizat pentru context
        loadingLabel.setText("Se încarcă lista de angajați...");

        executeAsync(
                () -> adminService.getAllStaff(), // Returnează List<User>
                (data) -> staffTable.setItems(FXCollections.observableArrayList(data)) // Primeste List<User>
        );
    }

    // =============================================================
    // 2. TAB MENIU (Async)
    // =============================================================
    private VBox createMenuContent() {
        menuTable = new TableView<>();
        menuTable.getColumns().add(createCol("Nume", "nume"));
        menuTable.getColumns().add(createCol("Preț", "pret"));

        refreshMenuTable();

        Button btnImport = new Button("Import JSON");
        btnImport.setOnAction(e -> handleImport());

        Button btnExport = new Button("Export JSON");
        btnExport.setOnAction(e -> handleExport());

        Button btnEdit = new Button("Modifică Preț");
        btnEdit.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white;");
        btnEdit.setOnAction(e -> editProductPrice());

        Button btnDeleteProd = new Button("Șterge Produs");
        btnDeleteProd.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
        btnDeleteProd.setOnAction(e -> {
            Produs p = menuTable.getSelectionModel().getSelectedItem();
            if(p != null) {
                executeAsync(
                        () -> adminService.deleteProduct(p),
                        () -> {
                            refreshMenuTable();
                            showAlert("Info", "Produs șters.");
                        }
                );
            }
        });

        HBox controls = new HBox(10, btnImport, btnExport, btnEdit, new Separator(), btnDeleteProd);
        controls.setPadding(new Insets(10));

        return new VBox(10, new Label("Gestiune Meniu"), menuTable, controls);
    }

    private void editProductPrice() {
        Produs selected = menuTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        TextInputDialog dialog = new TextInputDialog(String.valueOf(selected.getPret()));
        dialog.setTitle("Editare Preț");
        dialog.setContentText("Preț nou (RON):");

        dialog.showAndWait().ifPresent(priceStr -> {
            loadingLabel.setText("Actualizare preț în baza de date...");
            executeAsync(() -> {
                // Background logic
                double newPrice = Double.parseDouble(priceStr);
                adminService.updateProductPrice(selected, newPrice);
                return null;
            }, (res) -> {
                // UI update
                refreshMenuTable();
                showAlert("Succes", "Preț actualizat!");
            });
        });
    }

    private void handleImport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            loadingLabel.setText("Se importă datele (Poate dura)...");
            executeAsync(
                    () -> {
                        try {
                            adminService.importMenuFromJson(file);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    },
                    () -> {
                        refreshMenuTable();
                        showAlert("Succes", "Meniu importat!");
                    }
            );
        }
    }

    private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            // Trebuie să luăm datele din UI thread înainte de a porni task-ul
            List<Produs> itemsToExport = menuTable.getItems();

            loadingLabel.setText("Se exportă fișierul...");
            executeAsync(
                    () -> {
                        try {
                            adminService.exportMenuToJson(itemsToExport, file);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    },
                    () -> showAlert("Succes", "Meniu exportat!")
            );
        }
    }

    private void refreshMenuTable() {
        loadingLabel.setText("Se actualizează meniul...");
        executeAsync(
                () -> adminService.getMenu(),
                (data) -> menuTable.setItems(FXCollections.observableArrayList(data))
        );
    }

    // =============================================================
    // 3. TAB OFERTE
    // =============================================================
    private VBox createOffersContent() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(30));

        Label lblInfo = new Label("Selectează ofertele active (Instant):");

        CheckBox cb1 = new CheckBox("Happy Hour");
        cb1.setSelected(adminService.isHappyHour());
        // Aici nu folosim async pentru că e doar o schimbare de boolean în memorie (foarte rapid)
        cb1.setOnAction(e -> adminService.setHappyHour(cb1.isSelected()));

        CheckBox cb2 = new CheckBox("Party Pack");
        cb2.setSelected(adminService.isPartyPack());
        cb2.setOnAction(e -> adminService.setPartyPack(cb2.isSelected()));

        CheckBox cb3 = new CheckBox("Meal Deal");
        cb3.setSelected(adminService.isMealDeal());
        cb3.setOnAction(e -> adminService.setMealDeal(cb3.isSelected()));

        container.getChildren().addAll(lblInfo, cb1, cb2, cb3);
        return container;
    }

    // =============================================================
    // 4. TAB ISTORIC (Async)
    // =============================================================
    private VBox createHistoryContent() {
        historyTable = new TableView<>();

        TableColumn<Comanda, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Comanda, String> dateCol = new TableColumn<>("Data");
        dateCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getData().toString()));

        TableColumn<Comanda, String> waiterCol = new TableColumn<>("Ospătar");
        waiterCol.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getOspatar() != null ? cell.getValue().getOspatar().getUsername() : "N/A"
        ));

        TableColumn<Comanda, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));

        historyTable.getColumns().addAll(idCol, dateCol, waiterCol, totalCol);

        Button btnRefresh = new Button("Actualizează Istoric");
        btnRefresh.setOnAction(e -> refreshHistory());

        refreshHistory(); // Load initial
        return new VBox(10, btnRefresh, historyTable);
    }

    private void refreshHistory() {
        loadingLabel.setText("Se încarcă istoricul comenzilor...");
        executeAsync(
                () -> adminService.getHistory(),
                (data) -> historyTable.setItems(FXCollections.observableArrayList(data))
        );
    }

    // Helper UI
    private <T> TableColumn<T, String> createCol(String title, String property) {
        TableColumn<T, String> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        return col;
    }

    private void showAlert(String title, String content) {
        // Alert trebuie arătat mereu pe JavaFX Thread (deja asigurat de Task.setOnSucceeded)
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}