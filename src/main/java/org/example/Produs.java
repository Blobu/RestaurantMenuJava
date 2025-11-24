package org.example;

import java.util.ArrayList;

public class Produs implements ProdusInterface {
    private String nume;
    private double pret;
    private double pretDupaReducere;
    private Type type;
    private int quantity;
    private static double TVA = 0.09;
    private ArrayList<String> ingredients;
    private ArrayList<String> alergeni;
    private ArrayList<Produs> ExtraProducts;

    public Produs(String nume, double pret, Type type, int quantity, ArrayList<String> ingredients, ArrayList<String> alergeni) {
        this.nume = nume;
        this.pret = pret;
        this.pretDupaReducere = pret;
        this.type = type;
        this.quantity = quantity;
        this.ingredients = ingredients;
        this.alergeni = alergeni;
    }

    public Produs(Produs produs) {
        this.nume = produs.nume;
        this.pret = produs.pret;
        this.pretDupaReducere = produs.pretDupaReducere;
        this.type = produs.type;
        this.quantity = produs.quantity;
        this.ingredients = new ArrayList<>(produs.ingredients);
        this.alergeni = new ArrayList<>(produs.alergeni);
        if(produs.ExtraProducts != null) {
            this.ExtraProducts = new ArrayList<>(produs.ExtraProducts);
        }
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void setType(Type t) {
        this.type = t;
    }

    @Override
    public void setType(String t) {
        this.type = Type.valueOf(t);
    }

    @Override
    public String getNume() {
        return nume;
    }

    @Override
    public void setNume(String nume) {
        this.nume = nume;
    }

    @Override
    public double getPret() {
        return pret;
    }

    @Override
    public void setPret(double pret) {
        this.pret = pret+(TVA*pret);
    }

    @Override
    public int getQuantity() {
        return quantity;
    }

    @Override
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }


    public static void setTVA(double tVA) {
        TVA = tVA;
    }

    @Override
    public void displayInfo() {
        System.out.print("\n\n> " + getNume()+ " - " + getPret() + "RON - " + getQuantity() + "Cantitate\nIngrediente:");
        printIngredientsAndAllergens();
    }

    public void printIngredientsAndAllergens() {
        for (String ingredient : this.getIngredients()) {
            System.out.print(ingredient + ", ");
        }
        if (this.getIngredients().isEmpty()) {
            System.out.print("Fara ingrediente speciale");
        } else {
            System.out.print("\nAlergeni: ");
            for (String alergen : this.getAlergeni()) {
                System.out.print(alergen + ", ");
            }
        }
    }


    @Override
    public double getPretDupaReducere() {
        return pretDupaReducere;
    }

    @Override
    public void setPretDupaReducere(double pretDupaReducere) {
        this.pretDupaReducere = pretDupaReducere;
    }

    @Override
    public ArrayList<Produs> getExtraProducts() {
        return ExtraProducts;
    }

    @Override
    public void setExtraProducts(ArrayList<Produs> extraProducts) {
        ExtraProducts = extraProducts;
    }

    @Override
    public void addextraProduct(Produs p) {
        if(this.ExtraProducts == null) {
            this.ExtraProducts = new ArrayList<>();
        }
        this.ExtraProducts.add(p);
    }

    @Override
    public void setIngredients(ArrayList<String> ingredients) {
        this.ingredients = ingredients;
    }
    @Override
    public ArrayList<String> getIngredients() {
        return ingredients;
    }

    @Override
    public ArrayList<String> getAlergeni() {
        return alergeni;
    }

    @Override
    public void setAlergeni(ArrayList<String> alergeni) {
        this.alergeni = alergeni;
    }
}
