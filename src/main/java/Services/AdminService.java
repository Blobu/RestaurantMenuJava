package Services;

import Entities.*;
import Menu.Menu;
import Menu.OfferService;
import Menu.ReadFile;
import Products.Produs;
import Products.ProdusInterface;
import Repositories.OrderRepository;
import Repositories.ProductRepository;
import Repositories.UserRepository;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminService {

    private final UserRepository userRepo = new UserRepository();
    private final ProductRepository productRepo = new ProductRepository();
    private final OrderRepository orderRepo = new OrderRepository();

    // --- LOGICĂ PERSONAL (HR) ---
    public List<User> getAllStaff() {
        return userRepo.findAll();
    }

    public void hireStaff(String username, String password) throws Exception {
        if (username.isEmpty() || password.isEmpty()) throw new Exception("Date incomplete!");
        // Implicit setăm rolul STAFF, Adminul se face doar din DB direct
        User u = new User(username, password, Role.STAFF);
        userRepo.save(u);
    }

    public void fireStaff(User user) throws Exception {
        if (user == null) throw new Exception("Niciun angajat selectat.");
        if (user.getRole() == Role.ADMIN) throw new Exception("Nu poți șterge un Administrator!");

        // Repository se ocupă de ștergerea fizică și cascade
        userRepo.deleteById(user.getId());
    }

    // --- LOGICĂ MENIU ---
    public List<Produs> getMenu() {
        return productRepo.findAllProducts();
    }

    public void updateProductPrice(Produs product, double newPrice) throws Exception {
        if (newPrice < 0) throw new Exception("Prețul nu poate fi negativ.");

        product.setPret(newPrice);
        product.setPretDupaReducere(newPrice);
        productRepo.save(product);
    }

    public void deleteProduct(Produs product) {
        if (product != null) {
            productRepo.deleteById(product.getId());
        }
    }

    // --- LOGICĂ IMPORT/EXPORT JSON ---
    public void exportMenuToJson(List<Produs> currentMenu, File file) throws Exception {
        Map<ProdusInterface.Type, ArrayList<Produs>> exportMap = new HashMap<>();
        for (Produs p : currentMenu) {
            exportMap.computeIfAbsent(p.getType(), k -> new ArrayList<>()).add(p);
        }
        Menu menuObj = new Menu(exportMap);
        ReadFile.exportMenuToJson(menuObj, file.toPath());
    }

    public void importMenuFromJson(File file) throws Exception {
        // 1. Ștergem tot (Reset)
        productRepo.deleteAll();

        // 2. Citim fișierul
        Map<ProdusInterface.Type, ArrayList<Produs>> importedMap = ReadFile.readMenuFromJson(file.toPath());

        // 3. Salvăm noile produse
        if (importedMap != null) {
            for (ArrayList<Produs> productList : importedMap.values()) {
                for (Produs p : productList) {
                    p.setId(null); // Resetăm ID pentru a crea intrări noi
                    productRepo.save(p);
                }
            }
        }
    }

    // --- LOGICĂ OFERTE (Strategy Wrappers) ---
    public void setHappyHour(boolean active) { OfferService.HAPPY_HOUR = active; }
    public boolean isHappyHour() { return OfferService.HAPPY_HOUR; }

    public void setPartyPack(boolean active) { OfferService.PARTY_PACK = active; }
    public boolean isPartyPack() { return OfferService.PARTY_PACK; }

    public void setMealDeal(boolean active) { OfferService.MEAL_DEAL = active; }
    public boolean isMealDeal() { return OfferService.MEAL_DEAL; }

    // --- LOGICĂ ISTORIC ---
    public List<Comanda> getHistory() {
        return orderRepo.findAllHistory();
    }
}