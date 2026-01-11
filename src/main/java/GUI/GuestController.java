package GUI;

import Products.Produs;
import Services.MenuService; // Importăm Service-ul
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GuestController {

    private final Stage stage;
    private final FXMain mainApp;

    // DEPENDINȚĂ: Folosim Service-ul, nu direct EntityManager
    private final MenuService menuService;

    private ListView<Produs> listView;

    public GuestController(Stage stage, FXMain mainApp) {
        this.stage = stage;
        this.mainApp = mainApp;
        // Controller-ul instanțiază Service-ul (sau îl primește prin constructor)
        this.menuService = new MenuService();
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        // --- UI Setup (Rămâne neschimbat, e responsabilitatea Controller-ului) ---
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(0, 0, 10, 0));

        TextField searchField = new TextField(); searchField.setPromptText("Caută produs...");
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Toate", "Mancare", "Bautura", "Pizza");
        typeCombo.setValue("Toate");
        TextField minPrice = new TextField(); minPrice.setPromptText("Min"); minPrice.setPrefWidth(50);
        TextField maxPrice = new TextField(); maxPrice.setPromptText("Max"); maxPrice.setPrefWidth(50);

        Button btnFilter = new Button("Aplică Filtre");
        Button btnBack = new Button("Înapoi");

        topBar.getChildren().addAll(btnBack, searchField, typeCombo, minPrice, maxPrice, btnFilter);
        root.setTop(topBar);

        // --- LISTA PRODUSE ---
        listView = new ListView<>();
        // Cerem datele inițiale de la Service
        listView.setItems(FXCollections.observableArrayList(menuService.getAllProducts()));

        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Produs item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.getNume() + " - " + String.format("%.2f", item.getPret()) + " RON");
            }
        });
        root.setCenter(listView);

        // --- DETALII ---
        TextArea detailsArea = new TextArea();
        detailsArea.setEditable(false);
        VBox rightPane = new VBox(10, new Label("Detalii:"), detailsArea);
        root.setRight(rightPane);

        // --- EVENIMENTE ---

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Formarea textului e ok în Controller,
                // dar ideal ar fi o metodă newVal.getDescription() în Model
                detailsArea.setText("NUME: " + newVal.getNume() + "\nPREȚ: " + newVal.getPret());
            }
        });

        // ACȚIUNEA DE FILTRARE (Mult simplificată)
        btnFilter.setOnAction(e -> {
            // Controller-ul doar colectează input-ul și îl pasă la Service
            var filteredList = menuService.getFilteredProducts(
                    searchField.getText(),
                    typeCombo.getValue(),
                    minPrice.getText(),
                    maxPrice.getText()
            );
            // Actualizăm UI-ul cu rezultatul primit
            listView.setItems(FXCollections.observableArrayList(filteredList));
        });

        btnBack.setOnAction(e -> {
            try { mainApp.start(stage); } catch (Exception ex) { ex.printStackTrace(); }
        });

        Scene scene = new Scene(root, 1000, 600);
        stage.setScene(scene);
        stage.setTitle("Meniu Guest - MVC Refactored");
    }
}