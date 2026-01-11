package GUI;

import Entities.*;
import Products.Produs; // Asigură-te că acesta este singurul Produs importat
import Services.StaffService;
import javafx.collections.FXCollections; // NECESAR PENTRU FIX
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List; // NECESAR

public class StaffController {

    private final Stage stage;
    private final FXMain mainApp;
    private final User currentUser;

    private final StaffService staffService;

    // UI Components
    private ListView<Masa> tablesList;
    private TableView<Produs> menuTable;
    private ListView<ComandaItem> orderListView;
    private Label totalLabel;
    private TextArea discountDisplayArea;
    private Masa selectedTable;

    public StaffController(Stage stage, FXMain mainApp, User currentUser) {
        this.stage = stage;
        this.mainApp = mainApp;
        this.currentUser = currentUser;
        this.staffService = new StaffService();
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        // --- TOP BAR ---
        HBox topBar = new HBox(10);
        topBar.setStyle("-fx-background-color: #eee; -fx-padding: 10px;");
        Label lblUser = new Label("Logat ca: " + currentUser.getUsername() + " (Staff)");
        Button btnLogout = new Button("Logout");
        btnLogout.setOnAction(e -> {
            try { mainApp.start(stage); } catch (Exception ex) { ex.printStackTrace(); }
        });
        topBar.getChildren().addAll(lblUser, new Region(), btnLogout);
        HBox.setHgrow(topBar.getChildren().get(1), Priority.ALWAYS);
        root.setTop(topBar);

        // --- CENTER: MENU ---
        VBox centerPane = new VBox(10, new Label("Meniu (Dublu click pt adaugare):"));
        centerPane.setVisible(false);

        menuTable = new TableView<>();
        TableColumn<Produs, String> nameCol = new TableColumn<>("Produs");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("nume"));

        TableColumn<Produs, Double> priceCol = new TableColumn<>("Preț");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("pret"));

        menuTable.getColumns().addAll(nameCol, priceCol);

        // --- FIX APLICAT AICI ---
        // 1. Luăm lista simplă de la Service
        List<Produs> rawMenu = staffService.getMenu();
        // 2. O transformăm în ObservableList pentru JavaFX
        menuTable.setItems(FXCollections.observableArrayList(rawMenu));
        // ------------------------

        menuTable.setRowFactory(tv -> {
            TableRow<Produs> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty()) ) {
                    staffService.addItemToOrder(row.getItem());
                    refreshOrderDisplay();
                }
            });
            return row;
        });

        centerPane.getChildren().add(menuTable);
        root.setCenter(centerPane);

        // --- LEFT: TABLES ---
        VBox leftPane = new VBox(10, new Label("Selectează Masa:"));
        tablesList = new ListView<>();
        tablesList.setPrefWidth(150);

        // Aceeași logică și aici: Service returnează List, ListView vrea ObservableList
        List<Masa> rawTables = staffService.getTables();
        tablesList.setItems(FXCollections.observableArrayList(rawTables));

        tablesList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedTable = newVal;
            centerPane.setVisible(newVal != null);
        });
        leftPane.getChildren().add(tablesList);
        root.setLeft(leftPane);

        // --- RIGHT: ORDER SUMMARY ---
        VBox rightPane = new VBox(10, new Label("Comandă Curentă:"));
        rightPane.setPrefWidth(350);

        orderListView = new ListView<>();
        orderListView.setPrefHeight(300);
        orderListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(ComandaItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    double val = item.getProdus().getPret() * item.getCantitate();
                    setText(String.format("%s x %d = %.2f RON", item.getProdus().getNume(), item.getCantitate(), val));
                }
            }
        });

        // Context Menu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem incItem = new MenuItem("Adaugă (+1)");
        incItem.setOnAction(e -> modifyItemQty(1));
        MenuItem decItem = new MenuItem("Scade (-1)");
        decItem.setOnAction(e -> modifyItemQty(-1));
        MenuItem removeItem = new MenuItem("Șterge Produs");
        removeItem.setOnAction(e -> {
            ComandaItem item = orderListView.getSelectionModel().getSelectedItem();
            if(item != null) {
                staffService.removeItem(item);
                refreshOrderDisplay();
            }
        });

        contextMenu.getItems().addAll(incItem, decItem, new SeparatorMenuItem(), removeItem);
        orderListView.setContextMenu(contextMenu);

        discountDisplayArea = new TextArea();
        discountDisplayArea.setEditable(false);
        discountDisplayArea.setPrefHeight(100);

        totalLabel = new Label("Total: 0.00 RON");
        totalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button btnSend = new Button("Trimite Comanda");
        btnSend.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnSend.setMaxWidth(Double.MAX_VALUE);
        btnSend.setOnAction(e -> sendOrder());

        Button btnClear = new Button("Anulează");
        btnClear.setMaxWidth(Double.MAX_VALUE);
        btnClear.setOnAction(e -> {
            staffService.clearOrder();
            refreshOrderDisplay();
        });

        rightPane.getChildren().addAll(orderListView, new Label("Detalii Reduceri:"), discountDisplayArea, totalLabel, btnSend, btnClear);
        root.setRight(rightPane);

        Scene scene = new Scene(root, 1100, 750);
        stage.setScene(scene);
        stage.setTitle("POS - Staff View (MVC)");
    }

    private void modifyItemQty(int delta) {
        ComandaItem selectedItem = orderListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            staffService.updateQuantity(selectedItem, selectedItem.getCantitate() + delta);
            refreshOrderDisplay();
        }
    }

    private void refreshOrderDisplay() {
        // Din nou, fix-ul cu ObservableList pentru lista de comenzi
        List<ComandaItem> items = staffService.getCurrentItems();
        orderListView.setItems(FXCollections.observableArrayList(items));

        if (items.isEmpty()) {
            discountDisplayArea.setText("");
            totalLabel.setText("Total: 0.00 RON");
            return;
        }

        StringBuilder sb = new StringBuilder();
        double finalTotal = staffService.calculateTotalWithOffers(sb);

        discountDisplayArea.setText(sb.toString());
        totalLabel.setText(String.format("Total: %.2f RON", finalTotal));
    }

    private void sendOrder() {
        try {
            staffService.sendOrderToKitchen(selectedTable, currentUser);
            showAlert("Succes", "Comanda a fost trimisă!");
            refreshOrderDisplay();
        } catch (Exception e) {
            showAlert("Eroare", e.getMessage());
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}