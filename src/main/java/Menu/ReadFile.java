package Menu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import Products.Bautura;
import Products.Mancare;
import Products.Produs;
import Products.ProdusInterface;
import org.example.ComandaConsola;

import java.io.IOException;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadFile {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+)");


    public static String readNumeRestaurant(Path path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(path.toFile());
            JsonNode numeNode = root.get("nume_restaurant");
            if (numeNode != null && numeNode.isTextual()) {
                return numeNode.asText();
            } else {
                throw new IOException("Invalid JSON format: missing or invalid 'nume_restaurant' field");
            }
        } catch (IOException e) {
            throw new IOException("Error reading restaurant name from JSON file", e);
        }
    }

    public static Double readTVA(Path path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(path.toFile());
            JsonNode tvaNode = root.get("tva");
            if (tvaNode != null && tvaNode.isNumber()) {
                return tvaNode.asDouble();
            } else {
                throw new IOException("Invalid JSON format: missing or invalid 'tva' field");
            }
        }catch (IOException e) {
            throw new IOException("Error reading TVA from JSON file", e);
        }
    }

    /**
     * Read menu from a JSON file structured like `menu.json` and return a list of Produs instances.
     * It maps JSON categories to ProdusInterface.Type and decides whether an item is Mancare or Bautura.
     */
    public static Map<ProdusInterface.Type,ArrayList<Produs>> readMenuFromJson(Path path) throws IOException {
        Map<ProdusInterface.Type,ArrayList<Produs>> items = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        try{
        JsonNode root = mapper.readTree(path.toFile());


        JsonNode meniu = root.get("meniu_restaurant");
        if (meniu == null || !meniu.isObject()) {
            throw new IOException("Invalid JSON format: missing 'meniu_restaurant' object");
        }


        Iterator<String> categoryNames = meniu.fieldNames();
        while (categoryNames.hasNext()) {
            String rawCategory = categoryNames.next();
            JsonNode array = meniu.get(rawCategory);
            if (array == null || !array.isArray()) continue;

            ProdusInterface.Type mappedType = mapCategoryToType(rawCategory);

            for (JsonNode node : array) {
                String nume = node.has("nume") ? node.get("nume").asText() : null;
                double pret = node.has("pret") ? node.get("pret").asDouble() : 0.0;
                String gramaj = node.has("gramaj") ? node.get("gramaj").asText() : "";
                JsonNode arrayIngrediente = node.get("ingrediente");
                ArrayList<String> ingrediente = new ArrayList<>();
                JsonNode arrayAlergeni = node.get("alergeni");
                ArrayList<String> alergeni = new ArrayList<>();

                if (arrayIngrediente != null && arrayIngrediente.isArray()) {
                    for (JsonNode ingNode : arrayIngrediente) {
                        String ing = ingNode.asText().trim();
                        if (!ing.isEmpty()) ingrediente.add(ing);
                    }
                }

                if(arrayAlergeni != null && arrayAlergeni.isArray()) {
                    for (JsonNode alNode : arrayAlergeni) {
                        String al = alNode.asText().trim();
                        if (!al.isEmpty()) alergeni.add(al);
                    }
                }


                if (nume == null) continue;

                int quantity = parseQuantity(gramaj);

                // Decide whether it's a drink or food
                boolean isBautura = isCategoryBeverage(rawCategory) || gramaj.toLowerCase().contains("ml");

                if (isBautura) {
                    Bautura b = new Bautura(nume, pret, mappedType, quantity, ingrediente, alergeni);
                    items.put(mappedType, items.getOrDefault(mappedType, new ArrayList<>()));
                    items.get(mappedType).add(b);

                } else {
                    Mancare m = new Mancare(nume, pret, mappedType, quantity, ingrediente, alergeni);
                    items.put(mappedType, items.getOrDefault(mappedType, new ArrayList<>()));
                    items.get(mappedType).add(m);
                }
            }
        }

        return items;
        } catch (IOException e) {
            throw new IOException("Error reading menu from JSON file", e);
        }
    }

    private static boolean isCategoryBeverage(String category) {
        String norm = normalize(category);
        return norm.contains("bauturi") || norm.contains("bauturi racoritoare") || norm.contains("alcoolice") || norm.contains("bauturialcoolice") || norm.contains("bebida") || norm.contains("beverages");
    }

    private static ProdusInterface.Type mapCategoryToType(String category) {
        String norm = normalize(category);

        if (norm.contains("aperitiv") || norm.contains("aperitive")) return ProdusInterface.Type.Aperitive;
        if (norm.contains("fel") && norm.contains("principal")) return ProdusInterface.Type.FelPrincipal;
        if (norm.contains("desert")) return ProdusInterface.Type.Desert;
        if (norm.contains("pizza")) return ProdusInterface.Type.FelPrincipal; // map Pizza to FelPrincipal
        if (norm.contains("bauturi") && norm.contains("racoritoare")) return ProdusInterface.Type.BauturiRacoritoare;
        if (norm.contains("alcool") || norm.contains("bauturialcoolice")) return ProdusInterface.Type.BauturiAlcoolice;

        // fallback
        return ProdusInterface.Type.FelPrincipal;
    }

    private static String normalize(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.toLowerCase().trim();
    }

    private static int parseQuantity(String gramaj) {
        if (gramaj == null) return 0;
        Matcher m = NUMBER_PATTERN.matcher(gramaj);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) { }
        }
        // fallback 0
        return 0;
    }

    public static void printMenu(Menu menu) {

        System.out.println("−−− Meniu Restaurant −−−\n");
        for (Map.Entry<ProdusInterface.Type, ArrayList<Produs>> entry : menu.getMenu().entrySet()) {
            System.out.println("=== " + entry.getKey().name() + " ===");
            for (Produs produs : entry.getValue()) {
                produs.displayInfo();
            }
            System.out.println();
        }
    }

    public static int readInt(Integer nrMax) {
        Scanner scanner = new Scanner(System.in);
        int nivelAcces;
        while (true) {
            String input = scanner.nextLine().trim();
            try {
                nivelAcces = Integer.parseInt(input);
                if (nivelAcces >= 1 && nivelAcces <= nrMax) {
                    break;
                } else {
                    System.out.println("Nivel de acces invalid. Va rugam sa introduceti un numar intre 1 si " + nrMax + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("Input invalid. Va rugam sa introduceti un numar valid.");
            }
        }
        return nivelAcces;
    }

    public static void ReadComanda(ComandaConsola comandaConsola) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n\nIntroduceti produsele dorite (format: 'Nume Produs, Cantitate'), sau 'gata' pentru a finaliza:");
        while (true) {
            String line = scanner.nextLine().trim();
            if (line.equalsIgnoreCase("gata")) {
                break;
            }
            String[] parts = line.split(",");
            if (parts.length != 2) {
                System.out.println("Format invalid. Va rugam sa introduceti in formatul 'Nume Produs, Cantitate'.");
                continue;
            }
            String productName = parts[0].trim();
            int quantity;
            try {
                quantity = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException e) {
                System.out.println("Cantitate invalida. Va rugam sa introduceti un numar valid.");
                continue;
            }
            comandaConsola.addProduct(productName, quantity);
        }

    }



    public static ArrayList<String> ReadNonVegetarian(Path path){
        ArrayList<String> lines;
        try {
            lines = new ArrayList<>(java.nio.file.Files.readAllLines(path) );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lines;
    }

    public static ProdusInterface.Type ReadType(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Introduceti tipul produsului (Aperitive, FelPrincipal, Desert, BauturiRacoritoare, BauturiAlcoolice):");
        while (true) {
            String input = scanner.nextLine().trim().toLowerCase();
            switch (input) {
                case "aperitive":
                    return ProdusInterface.Type.Aperitive;
                case "felprincipal":
                    return ProdusInterface.Type.FelPrincipal;
                case "desert":
                    return ProdusInterface.Type.Desert;
                case "bauturiracoritoare":
                    return ProdusInterface.Type.BauturiRacoritoare;
                case "bauturialcoolice":
                    return ProdusInterface.Type.BauturiAlcoolice;
                default:
                    System.out.println("Tip invalid. Va rugam sa introduceti un tip valid.");
            }
        }
    }

    public static Produs ReadProdus(Menu menu){
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Introduceti numele produsului:");
            String nume = scanner.nextLine().trim();
            var produse = menu.findProductByName(nume);
            if (!produse.isEmpty()) {
                return produse.get();
            }
            System.out.println("Produsul nu a fost gasit in meniu.");
        }
    }


    public static void exportMenuToJson(Menu menu, Path path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, List<Map<String, Object>>> menuMap = new HashMap<>();

        for (HashMap.Entry<ProdusInterface.Type, ArrayList<Produs>> entry : menu.getMenu().entrySet()) {
            List<Map<String, Object>> itemList = new ArrayList<>();
            for (Produs produs : entry.getValue()) {
                HashMap<String, Object> itemMap = new HashMap<>();
                itemMap.put("nume", produs.getNume());
                itemMap.put("pret", produs.getPret());
                itemMap.put("pret_dupa_reducere", produs.getPretDupaReducere());
                itemMap.put("quantity", produs.getQuantity());
                itemMap.put("ingrediente", produs.getIngredients());
                itemMap.put("alergeni", produs.getAlergeni());
                itemList.add(itemMap);
            }
            menuMap.put(entry.getKey().name(), itemList);
        }

        Map<String, Object> root = new HashMap<>();
        root.put("meniu_restaurant", menuMap);
        mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), root);
    }

}
