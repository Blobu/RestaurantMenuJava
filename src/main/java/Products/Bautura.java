package Products;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.util.ArrayList;

@Entity
@DiscriminatorValue("BAUTURA")
public class Bautura extends Produs {

    public Bautura() {
        super();
    }

    public Bautura(String nume, double pret,Type tip, int Volum, ArrayList<String> ingredients, ArrayList<String> alergeni) {
        super(nume, pret, tip, Volum, ingredients, alergeni);
    }


    @Override
    public void displayInfo() {
        System.out.print("\n\n> " + getNume()+ " - " + getPret() + "RON" + " - " + getQuantity() + "ml\nIngrediente: ");
        printIngredientsAndAllergens();
    }

}
