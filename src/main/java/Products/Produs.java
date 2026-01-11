package Products;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List; // Important: Folosim List interfata pentru JPA

@Entity
@Table(name = "produse")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tip_produs", discriminatorType = DiscriminatorType.STRING)
public class Produs implements ProdusInterface {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nume;
    private double pret;
    private double pretDupaReducere;

    @Enumerated(EnumType.STRING) // Salvam tipul ca text (FEL_PRINCIPAL), nu ca numar (1)
    private Type type;

    private int quantity;

    @Transient // Spunem Hibernate sa ignore acest camp static
    private static double TVA = 0.09;

    // --- FIX PENTRU LISTE (Hibernate creeaza tabele separate pt string-uri) ---
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "produs_ingrediente", joinColumns = @JoinColumn(name = "produs_id"))
    @Column(name = "ingredient")
    private List<String> ingredients = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "produs_alergeni", joinColumns = @JoinColumn(name = "produs_id"))
    @Column(name = "alergen")
    private List<String> alergeni = new ArrayList<>();

    // --- FIX PENTRU EXTRA PRODUCTS ---
    // Momentan il punem @Transient pentru a opri eroarea.
    // O relatie Produs-in-Produs este complexa si blocheaza pornirea acum.
    @Transient
    private ArrayList<Produs> ExtraProducts;

    // --- CONSTRUCTOR GOL OBLIGATORIU ---
    public Produs() {
    }

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
        // Copiere sigura pentru liste
        this.ingredients = (produs.ingredients != null) ? new ArrayList<>(produs.ingredients) : new ArrayList<>();
        this.alergeni = (produs.alergeni != null) ? new ArrayList<>(produs.alergeni) : new ArrayList<>();

        if(produs.ExtraProducts != null) {
            this.ExtraProducts = new ArrayList<>(produs.ExtraProducts);
        }
    }

    // --- GETTERS & SETTERS ---

    @Override
    public Long getId() { return id; }
    @Override
    public void setId(Long id) { this.id = id; }

    @Override
    public Type getType() { return type; }
    @Override
    public void setType(Type t) { this.type = t; }
    @Override
    public void setType(String t) { this.type = Type.valueOf(t); }

    @Override
    public String getNume() { return nume; }
    @Override
    public void setNume(String nume) { this.nume = nume; }

    @Override
    public double getPret() { return pret; }

    // Logica TVA ramane, dar atentie la calcule duble la citire din DB
    @Override
    public void setPret(double pret) {
        this.pret = pret;
    }

    // Metoda ajutatoare daca vrei sa aplici TVA manual
    public void setPretCuTva(double pretFaraTva) {
        this.pret = pretFaraTva + (TVA * pretFaraTva);
    }

    @Override
    public int getQuantity() { return quantity; }
    @Override
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public static void setTVA(double tVA) { TVA = tVA; }

    @Override
    public void displayInfo() {
        System.out.print("\n\n> " + getNume()+ " - " + getPret() + "RON - " + getQuantity() + "Cantitate\nIngrediente:");
        printIngredientsAndAllergens();
    }

    public void printIngredientsAndAllergens() {
        if (this.getIngredients() != null) {
            for (String ingredient : this.getIngredients()) {
                System.out.print(ingredient + ", ");
            }
        }

        if (this.getIngredients() == null || this.getIngredients().isEmpty()) {
            System.out.print("Fara ingrediente speciale");
        } else {
            System.out.print("\nAlergeni: ");
            if (this.getAlergeni() != null) {
                for (String alergen : this.getAlergeni()) {
                    System.out.print(alergen + ", ");
                }
            }
        }
    }

    @Override
    public double getPretDupaReducere() { return pretDupaReducere; }
    @Override
    public void setPretDupaReducere(double pretDupaReducere) { this.pretDupaReducere = pretDupaReducere; }

    @Override
    public ArrayList<Produs> getExtraProducts() { return ExtraProducts; }
    @Override
    public void setExtraProducts(ArrayList<Produs> extraProducts) { ExtraProducts = extraProducts; }

    @Override
    public void addextraProduct(Produs p) {
        if(this.ExtraProducts == null) {
            this.ExtraProducts = new ArrayList<>();
        }
        this.ExtraProducts.add(p);
    }

    // Am schimbat semnatura sa accepte List (JPA returneaza List, nu ArrayList)
    @Override
    public void setIngredients(ArrayList<String> ingredients) {
        this.ingredients = ingredients;
    }

    // Getter compatibil cu interfata, dar returnam ArrayList prin cast sau wrap daca e nevoie
    @Override
    public ArrayList<String> getIngredients() {
        if (ingredients instanceof ArrayList) {
            return (ArrayList<String>) ingredients;
        }
        return new ArrayList<>(ingredients);
    }

    @Override
    public ArrayList<String> getAlergeni() {
        if (alergeni instanceof ArrayList) {
            return (ArrayList<String>) alergeni;
        }
        return new ArrayList<>(alergeni);
    }

    @Override
    public void setAlergeni(ArrayList<String> alergeni) {
        this.alergeni = alergeni;
    }
}