package Menu;
import Products.Produs;
import Products.ProdusInterface;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;


public record Menu(Map<ProdusInterface.Type, ArrayList<Produs>> menu) {

    public ArrayList<Produs> getProdusOfType(ProdusInterface.Type type) {
        return menu.get(type);
    }


    public Map<ProdusInterface.Type, ArrayList<Produs>> getMenu() {
        return menu;
    }

    public void addDiscounts(ArrayList<IOferte> oferte) {
        for (ArrayList<Produs> produse : menu.values()) {
            for (Produs produs : produse) {
                for (IOferte oferta : oferte) {
                    Double pretRedus = oferta.getPretDupaReducere(produs);
                    produs.setPretDupaReducere(pretRedus);
                }
            }
        }
    }

    public Optional<Produs> findProductByName(String name) {
        for (ArrayList<Produs> produse : menu.values()) {
            for (Produs produs : produse) {
                if (produs.getNume().equalsIgnoreCase(name)) {
                    return Optional.of(produs);
                }
            }
        }
        return Optional.empty();
    }
}
