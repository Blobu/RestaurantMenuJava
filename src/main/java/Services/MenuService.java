package Services;

import Products.*;
import Repositories.ProductRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MenuService {

    private final ProductRepository repo;
    private List<Produs> cachedMenu; // Păstrăm lista în memorie pentru performanță

    public MenuService() {
        this.repo = new ProductRepository();
        loadMenuData();
    }

    // Încarcă datele folosind Repository-ul
    public void loadMenuData() {
        this.cachedMenu = repo.findAllProducts();
    }

    public List<Produs> getAllProducts() {
        return cachedMenu;
    }

    // Logica de filtrare mutată din Controller
    public List<Produs> getFilteredProducts(String searchText, String type, String minStr, String maxStr) {
        return cachedMenu.stream()
                .filter(p -> filterByName(p, searchText))
                .filter(p -> filterByType(p, type))
                .filter(p -> filterByPrice(p, minStr, maxStr))
                .collect(Collectors.toList());
    }

    // --- Metode Helper Private (Logică pură de Business) ---

    private boolean filterByName(Produs p, String searchText) {
        if (searchText == null || searchText.isEmpty()) return true;
        return Optional.ofNullable(p.getNume())
                .map(n -> n.toLowerCase().contains(searchText.toLowerCase()))
                .orElse(false);
    }

    private boolean filterByType(Produs p, String type) {
        if (type.equals("Toate")) return true;
        if (type.equals("Mancare")) return p instanceof Mancare;
        if (type.equals("Bautura")) return p instanceof Bautura;
        if (type.equals("Pizza")) return p instanceof Pizza;
        return true;
    }

    private boolean filterByPrice(Produs p, String minStr, String maxStr) {
        try {
            double min = minStr.isEmpty() ? 0 : Double.parseDouble(minStr);
            double max = maxStr.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxStr);
            return p.getPret() >= min && p.getPret() <= max;
        } catch (NumberFormatException ex) {
            return true;
        }
    }

    // Regula de business mutată din UI
    private boolean isVegetarian(Produs p) {
        if (p.getIngredients() == null) return true;
        String allIng = p.getIngredients().toString().toLowerCase();
        return !allIng.contains("carne") && !allIng.contains("salam") &&
                !allIng.contains("sunca") && !allIng.contains("bacon") &&
                !allIng.contains("pui") && !allIng.contains("vita") &&
                !allIng.contains("peste");
    }
}