package org.example;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {
    static void main() throws IOException {
    Path path = Path.of("C:\\Facultate\\AN_2_SEM_1\\MIP\\MeniuRestaurant\\src\\main\\java\\org\\example\\config.json");


        String numeRestaurant = ReadFile.readNumeRestaurant(path);
        Menu menu = new Menu(ReadFile.readMenuFromJson(path));
        Produs.setTVA(ReadFile.readTVA(path));
        //oferte lambda
        ArrayList<IOferte> oferte = new ArrayList<>(){
            {
                add(produs -> {

                    if(java.time.LocalTime.now().getHour()>=17 && java.time.LocalTime.now().getHour()<=19) {
                        if (produs.getType() == ProdusInterface.Type.BauturiAlcoolice) {
                            return produs.getPret() * 0.8; //20% reducere pentru bauturi alcoolice

                        }
                    }
                    return produs.getPret();
                });

                add(produs -> {
                    if(produs.getNume().toLowerCase().contains("pizza")){
                        produs.addextraProduct(menu.getProdusOfType(ProdusInterface.Type.BauturiRacoritoare).get(1));
                    }
                    return produs.getPret();
                });
            }
        };

        menu.addDiscounts(oferte);
        //---------------------------------------------------


        System.out.println("Alege nivelul de acces:\n1. Owner\n2. Ospatar\n3. Client\n");

        switch (ReadFile.readInt(3)) {
            case 1 -> {
                System.out.println("Bine ai venit, Owner!\n"+ "Alegeti o optiune:\n"+"1. Vizualizeaza meniul\n" +
                        "2. Care sunt toate preparatele vegetariene, sortate în ordine alfabetică?\n" +
                        "3. Care este prețul mediu al unui tip specific?\n" +
                        "4. Avem vreun preparat care costă mai mult de 100 RON?\n" +
                        "5. Exportați meniul într-un fișier Json.");
                switch (ReadFile.readInt(5)) {
                    case 1 -> ReadFile.printMenu(menu);
                    case 2 -> {
                        ArrayList<String> NonVeganProducts = ReadFile.ReadNonVegetarian(Path.of("C:\\Facultate\\AN_2_SEM_1\\MIP\\MeniuRestaurant\\src\\main\\java\\org\\example\\IngredienteNevegetariene.txt"));


                        for (ArrayList<Produs> Produse : menu.getMenu().values()) {
                            List<Produs> vegetarianProducts = Produse.stream()
                                    .filter(produs -> NonVeganProducts.stream().noneMatch(ingredient -> produs.getIngredients().contains(ingredient)))
                                    .sorted(Comparator.comparing(Produs::getNume))
                                    .toList();

                            System.out.println("\nProduse vegetariene in categoria " + (Produse.isEmpty() ? "N/A" : Produse.getFirst().getType()) + ":");
                            for (Produs produs : vegetarianProducts) {
                                System.out.println("- " + produs.getNume());
                            }
                        }
                    }
                    case 3 -> {
                        ProdusInterface.Type tip = ReadFile.ReadType();
                        double pretMediu;
                        pretMediu = menu.getProdusOfType(tip).stream()
                                .mapToDouble(Produs::getPret)
                                .average()
                                .orElse(0.0);
                        System.out.printf("Pretul mediu al preparatelor din categoria %s este: %.2f RON\n", tip, pretMediu);
                    }
                    case 4 -> {
                        boolean existaProdusScump = menu.getMenu().values().stream()
                                .flatMap(List::stream)
                                .anyMatch(produs -> produs.getPret() > 100.0);
                        if (existaProdusScump) {
                            System.out.println("Da, avem preparate care costa mai mult de 100 RON.");
                        } else {
                            System.out.println("Nu, nu avem preparate care costa mai mult de 100 RON.");
                        }
                    }
                    case 5 -> ReadFile.exportMenuToJson(menu, Path.of("C:\\Facultate\\AN_2_SEM_1\\MIP\\MeniuRestaurant\\src\\main\\java\\org\\example\\exported_menu.json"));
                    default -> System.out.println("Optiune invalida. Iesire din program.");
                }

            }
            case 2 -> {
                System.out.println("Bine ai venit, Ospatar! Poti prelua comenzile clientilor.\n");
                ReadFile.ReadProdus(menu).displayInfo();

            }

            case 3 -> {
                System.out.println("Bine ai venit, Client!\n");
                ReadFile.printMenu(menu);
                Comanda comanda = new Comanda(menu.getMenu());
                ReadFile.ReadComanda(comanda);
                comanda.printOrderSummary();
            }
            default ->System.out.println("Nivel de acces invalid. Iesire din program.");
        }

    }
}