package Services;

import Entities.*;
import Products.Produs;
import Menu.OfferService;
import Repositories.OrderRepository;
import Repositories.ProductRepository;
import Repositories.TableRepository;
import java.util.List;

public class StaffService {

    private final TableRepository tableRepo;
    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;
    private final OfferService offerService;

    // Starea comenzii curente (Business State)
    private final Comanda currentOrderWrapper;

    public StaffService() {
        this.tableRepo = new TableRepository();
        this.productRepo = new ProductRepository(); // Presupunem că l-ai creat la pasul anterior
        this.orderRepo = new OrderRepository();
        this.offerService = new OfferService();

        this.currentOrderWrapper = new Comanda();
        this.currentOrderWrapper.setItems(new java.util.ArrayList<>());
    }

    // --- METODE DE CITIRE ---

    public List<Masa> getTables() {
        List<Masa> tables = tableRepo.findAll();
        if (tables.isEmpty()) {
            tableRepo.initializeTables(10);
            return tableRepo.findAll();
        }
        return tables;
    }

    public List<Produs> getMenu() {
        return productRepo.findAllProducts();
    }

    // --- LOGICĂ DE GESTIUNE A COMENZII CURENTE (Mutată din Controller) ---

    public List<ComandaItem> getCurrentItems() {
        return currentOrderWrapper.getItems();
    }

    public void addItemToOrder(Produs p) {
        boolean found = false;
        for (ComandaItem item : currentOrderWrapper.getItems()) {
            if (item.getProdus().getId().equals(p.getId())) {
                item.setCantitate(item.getCantitate() + 1);
                found = true;
                break;
            }
        }
        if (!found) {
            // Notă: Aici creăm un ComandaItem temporar, nelegat de baza de date încă
            ComandaItem newItem = new ComandaItem(null, p, 1);
            currentOrderWrapper.getItems().add(newItem);
        }
    }

    public void updateQuantity(ComandaItem item, int newQty) {
        if (newQty <= 0) {
            currentOrderWrapper.getItems().remove(item);
        } else {
            item.setCantitate(newQty);
        }
    }

    public void removeItem(ComandaItem item) {
        currentOrderWrapper.getItems().remove(item);
    }

    public void clearOrder() {
        currentOrderWrapper.getItems().clear();
    }

    // --- CALCULE ȘI SALVARE ---

    public double calculateTotalWithOffers(StringBuilder breakdown) {
        // Delegăm către OfferService-ul existent
        return offerService.calculateTotal(currentOrderWrapper.getItems(), breakdown);
    }

    public void sendOrderToKitchen(Masa selectedTable, User currentUser) throws Exception {
        if (selectedTable == null || currentOrderWrapper.getItems().isEmpty()) {
            throw new Exception("Selectează o masă și adaugă produse!");
        }

        // Recalculăm totalul final pentru a-l salva în DB
        double total = offerService.calculateTotal(currentOrderWrapper.getItems(), new StringBuilder());

        // Apelăm Repository-ul pentru persistență
        orderRepo.createOrder(
                selectedTable.getId(),
                currentUser.getId(),
                currentOrderWrapper.getItems(),
                total
        );

        // Resetăm comanda locală după succes
        clearOrder();
    }
}