package Menu;

import Entities.ComandaItem;
import Products.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;

public class OfferService {
    public static boolean HAPPY_HOUR = true;
    public static boolean PARTY_PACK = true;
    public static boolean MEAL_DEAL = true;

    public double calculateTotal(List<ComandaItem> items, StringBuilder receiptBuffer) {
        double subtotal = 0;
        double discount = 0;

        // 1. Calcul Subtotal Simplu
        for (ComandaItem item : items) {
            subtotal += item.getProdus().getPret() * item.getCantitate();
        }

        // 2. Happy Hour (A 2-a băutură la jumătate)
        if (HAPPY_HOUR) {
            List<ComandaItem> drinks = items.stream()
                    .filter(i -> i.getProdus() instanceof Bautura)
                    .collect(Collectors.toList());

            // Calcul simplificat: luam numarul total de bauturi
            int totalDrinks = drinks.stream().mapToInt(ComandaItem::getCantitate).sum();
            int eligible = totalDrinks / 2; // La fiecare a 2-a

            if (eligible > 0) {
                // Presupunem reducerea pe cea mai ieftina bautura (sau medie)
                // Pentru simplitate, luam prima bautura gasita
                double avgPrice = drinks.stream().mapToDouble(i -> i.getProdus().getPret()).average().orElse(0);
                double cut = (avgPrice / 2) * eligible;
                discount += cut;
                receiptBuffer.append("Happy Hour (-").append(String.format("%.2f", cut)).append(" RON)\n");
            }
        }

        // 3. Party Pack (4 Pizza -> 1 Gratis)
        if (PARTY_PACK) {
            List<ComandaItem> pizzas = items.stream()
                    .filter(i -> i.getProdus() instanceof Pizza || i.getProdus().getNume().toLowerCase().contains("pizza"))
                    .collect(Collectors.toList());

            int countPizzas = pizzas.stream().mapToInt(ComandaItem::getCantitate).sum();
            if (countPizzas >= 4) {
                double cheapest = pizzas.stream()
                        .mapToDouble(i -> i.getProdus().getPret())
                        .min().orElse(0);
                discount += cheapest;
                receiptBuffer.append("Party Pack (-").append(cheapest).append(" RON)\n");
            }
        }

        // 4. Meal Deal (Pizza + Desert -> Desert -25%)
        if (MEAL_DEAL) {
            boolean hasPizza = items.stream().anyMatch(i -> i.getProdus() instanceof Pizza);
            if (hasPizza) {
                List<ComandaItem> deserts = items.stream()
                        .filter(i -> i.getProdus().getType() == ProdusInterface.Type.Desert)
                        .sorted(Comparator.comparingDouble(i -> i.getProdus().getPret()))
                        .collect(Collectors.toList());

                if (!deserts.isEmpty()) {
                    double cut = deserts.get(0).getProdus().getPret() * 0.25;
                    discount += cut;
                    receiptBuffer.append("Meal Deal (-").append(String.format("%.2f", cut)).append(" RON)\n");
                }
            }
        }

        return subtotal - discount;
    }
}