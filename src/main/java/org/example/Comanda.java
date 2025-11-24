package org.example;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class Comanda {
    private final Map<ProdusInterface.Type, ArrayList<Produs>> Menu;
    private final Map<Produs, Integer> orderedProducts = new java.util.HashMap<>();

    public Comanda(Map<ProdusInterface.Type, ArrayList<Produs>> menu) {
        Menu = menu;
    }

    public void addProduct(String productName, int quantity) {

        for (ArrayList<Produs> produse : Menu.values()) {
            for (Produs produs : produse) {
                if (produs.getNume().equalsIgnoreCase(productName)) {
                    if(productName.toLowerCase().contains("pizza"))
                            orderedProducts.put(PizzaExtra(produs), quantity);
                    else
                            orderedProducts.put(produs, quantity);
                    if(produs.getExtraProducts()!=null) {
                        for (Produs extra : produs.getExtraProducts()) {
                            Produs temp = new Produs(extra);
                            temp.setQuantity(quantity);
                            temp.setPret(0);
                            temp.setPretDupaReducere(0);
                            orderedProducts.put(temp, quantity);
                            System.out.println(" Oferta extra adaugata: " + extra.getNume() + " x" + quantity);
                        }
                    }
                    return;
                }
            }
        }
        System.out.println("Produsul " + productName + " nu exista in meniu.");
    }

    public Produs PizzaExtra(Produs produs){
        Scanner scanner = new Scanner(System.in);
        boolean cheese, mushrooms, olives, pepperoni;
        Pizza pizzaWithExtras;

        System.out.println("introduceti marime Pizza: (Mica/Medie/Mare) ");
        String marime = scanner.nextLine().trim();
        if(!marime.equalsIgnoreCase("Mica") && !marime.equalsIgnoreCase("Medie") && !marime.equalsIgnoreCase("Mare")){
            System.out.println("Marime invalida. Se va selecta marimea Medie implicit.");
            marime = "Medie";
        }

        System.out.println(" Doriti sa adaugati extra topping-uri pentru pizza? (da/nu)");
        String raspuns = scanner.nextLine().trim().toLowerCase();
        if (raspuns.equals("da")) {
            System.out.println(" Doriti extra branza? (da/nu)");
            raspuns = scanner.nextLine().trim().toLowerCase();
            cheese = (raspuns.equals("da"));
            System.out.println(" Doriti ciuperci? (da/nu)");
            raspuns = scanner.nextLine().trim().toLowerCase();
            mushrooms = (raspuns.equals("da"));
            System.out.println(" Doriti masline? (da/nu)");
            raspuns = scanner.nextLine().trim().toLowerCase();
            olives = (raspuns.equals("da"));
            System.out.println(" Doriti pepperoni? (da/nu)");
            raspuns = scanner.nextLine().trim().toLowerCase();
            pepperoni = (raspuns.equals("da"));
            pizzaWithExtras = new Pizza.Builder(produs, marime)
                    .extraCheese(cheese)
                    .mushrooms(mushrooms)
                    .olives(olives)
                    .pepperoni(pepperoni)
                    .build();
        }
        else
            pizzaWithExtras = new Pizza.Builder(produs, marime).build();

        return pizzaWithExtras;

    }



    public double calculateTotal() {
        double total = 0.0;
        for (Map.Entry<Produs, Integer> entry : orderedProducts.entrySet()) {
            total += entry.getKey().getPretDupaReducere() * entry.getValue();
        }
        return total;
    }

    public void printOrderSummary() {
        System.out.println("−−− Comanda ta −−−\n");
        for(Map.Entry<Produs, Integer> entry : orderedProducts.entrySet()) {
            Produs produs = entry.getKey();
            int quantity = entry.getValue();
            System.out.printf("%s x%d - %.2f RON fiecare, Total: %.2f RON\n",
                    produs.getNume(), quantity, produs.getPretDupaReducere(),
                    produs.getPretDupaReducere() * quantity);
        }
        System.out.printf("Total de plata: %.2f RON\n", calculateTotal());
    }

}
