package org.example;

import java.util.ArrayList;

public class Mancare extends Produs {
    public Mancare(String nume, double pret,Type tip, int gramaj, ArrayList<String> ingredients, ArrayList<String> alergeni) {
        super(nume, pret ,tip , gramaj, ingredients, alergeni);

    }

    @Override
    public void displayInfo() {
        System.out.print("\n\n> " + getNume()+ " - " + getPret() + "RON" + " - " + getQuantity() + "g\nIngrediente: ");
        printIngredientsAndAllergens();
    }


}
