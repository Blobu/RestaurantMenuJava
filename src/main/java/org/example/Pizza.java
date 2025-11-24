package org.example;

public class Pizza extends Mancare{
    // Required parameters
    private  String size;

    //Optional paramters
    private boolean extraCheese;
    private boolean mushrooms;
    private boolean olives;
    private boolean pepperoni;

    private Pizza(String nume, double pret, Type tip, int gramaj, java.util.ArrayList<String> ingredients,
                 java.util.ArrayList<String> alergeni, String size, boolean extraCheese, boolean mushrooms,
                 boolean olives, boolean pepperoni){
        if(size.equalsIgnoreCase("Mica")) {
            pret -= 10;
        }
        else if(size.equalsIgnoreCase("Medie"))
        {
            pret +=0;
        }
        else if(size.equalsIgnoreCase("Mare"))
        {
            pret +=10;
        }
        else
        {
            throw new IllegalArgumentException("Ileagel size for pizza");
        }

        super(nume, pret, tip, gramaj, ingredients, alergeni);
        this.size = size;
        this.extraCheese = extraCheese;
        this.mushrooms = mushrooms;
        this.olives = olives;
        this.pepperoni = pepperoni;
    }



    public static class Builder {

        // Required parameters
        private final String nume;
        private final double pret;
        private final Type tip;
        private final int gramaj;
        private final java.util.ArrayList<String> ingredients;
        private final java.util.ArrayList<String> alergeni;
        private final String size;

        //Optional paramters
        private boolean extraCheese;
        private boolean mushrooms;
        private boolean olives;
        private boolean pepperoni;

        public Builder(String nume, double pret, Type tip, int gramaj, java.util.ArrayList<String> ingredients, java.util.ArrayList<String> alergeni,String size) {
            this.size = size;
            this.nume = nume;
            this.pret = pret;
            this.tip = tip;
            this.gramaj = gramaj;
            this.ingredients = ingredients;
            this.alergeni = alergeni;
        }

        public Builder(Produs produs, String size) {
            this.size = size;
            this.nume = produs.getNume();
            this.pret = produs.getPret();
            this.tip = produs.getType();
            this.gramaj = produs.getQuantity();
            this.ingredients = produs.getIngredients();
            this.alergeni = produs.getAlergeni();

        }

        public Builder extraCheese(boolean value) {
            this.extraCheese = value;
            return this;
        }
        public Builder mushrooms(boolean value) {
            this.mushrooms = value;
            return this;
        }
        public Builder olives(boolean value) {
            this.olives = value;
            return this;
        }
        public Builder pepperoni(boolean value) {
            this.pepperoni = value;
            return this;
        }

        public Pizza build() {
            return new Pizza(nume, pret, tip, gramaj, ingredients, alergeni, size, extraCheese, mushrooms, olives, pepperoni);
        }
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public boolean isExtraCheese() {
        return extraCheese;
    }

    public void setExtraCheese(boolean extraCheese) {
        this.extraCheese = extraCheese;
    }

    public boolean isMushrooms() {
        return mushrooms;
    }

    public void setMushrooms(boolean mushrooms) {
        this.mushrooms = mushrooms;
    }

    public boolean isOlives() {
        return olives;
    }

    public void setOlives(boolean olives) {
        this.olives = olives;
    }

    public boolean isPepperoni() {
        return pepperoni;
    }

    public void setPepperoni(boolean pepperoni) {
        this.pepperoni = pepperoni;
    }
}
