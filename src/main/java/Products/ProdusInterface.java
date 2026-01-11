package Products;

import java.util.ArrayList;

public interface ProdusInterface {
    enum Type {
        Aperitive,
        FelPrincipal,
        Desert,
        BauturiRacoritoare,
        BauturiAlcoolice
    }

    Long getId();

    void setId(Long id);

    Type getType();

    void setType(Type t);

    void setType(String t);

    String getNume();

    void setNume(String nume);

    double getPret();

    void setPret(double pret);

    int getQuantity();

    void setQuantity(int quantity);

    void displayInfo();

    double getPretDupaReducere();

    void setPretDupaReducere(double pretDupaReducere);

    ArrayList<Produs> getExtraProducts();

    void setExtraProducts(ArrayList<Produs> extraProducts);

    void addextraProduct(Produs p) ;

    void setIngredients(ArrayList<String> ingredients);

    ArrayList<String> getIngredients();

    void setAlergeni(ArrayList<String> alergeni);

    ArrayList<String> getAlergeni();
}
