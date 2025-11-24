package org.example;

import java.util.ArrayList;

public class Bautura extends Produs {

    public Bautura(String nume, double pret,Type tip, int Volum, ArrayList<String> ingredients, ArrayList<String> alergeni) {
        super(nume, pret, tip, Volum, ingredients, alergeni);
    }


    @Override
    public void displayInfo() {
        System.out.print("\n\n> " + getNume()+ " - " + getPret() + "RON" + " - " + getQuantity() + "ml\nIngrediente: ");
        printIngredientsAndAllergens();
    }

}
