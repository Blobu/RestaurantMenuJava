package Products;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.util.ArrayList;

@Entity
@DiscriminatorValue("MANCARE")
public class Mancare extends Produs {

    public Mancare() {
        super();
    }

    public Mancare(String nume, double pret,Type tip, int gramaj, ArrayList<String> ingredients, ArrayList<String> alergeni) {
        super(nume, pret ,tip , gramaj, ingredients, alergeni);

    }

    @Override
    public void displayInfo() {
        System.out.print("\n\n> " + getNume()+ " - " + getPret() + "RON" + " - " + getQuantity() + "g\nIngrediente: ");
        printIngredientsAndAllergens();
    }


}
