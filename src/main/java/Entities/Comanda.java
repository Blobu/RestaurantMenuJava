package Entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import Products.Produs;

@Entity
@Table(name = "comenzi")
public class Comanda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime data;

    // Totalul va fi calculat si salvat la final
    private double total;

    private String status; // Ex: "IN_PROGRES", "FINALIZATA"

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User ospatar;

    @ManyToOne
    @JoinColumn(name = "masa_id")
    private Masa masa;

    // Aici inlocuim Map-ul din vechea clasa cu o Lista de Items pentru DB
    @OneToMany(mappedBy = "comanda", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ComandaItem> items = new ArrayList<>();

    public Comanda() {}

    // --- LOGICA MUTATA SI ADAPTATA ---

    // In loc de addProduct(String name...), primim direct obiectul Produs din Interfata Grafica
    public void addItem(Produs produs, int cantitate) {
        // Cautam daca produsul exista deja in lista pentru a mari cantitatea
        for (ComandaItem item : items) {
            if (item.getProdus().getId().equals(produs.getId())) {
                item.setCantitate(item.getCantitate() + cantitate);
                return;
            }
        }
        // Daca nu exista, cream un rand nou
        ComandaItem newItem = new ComandaItem(this, produs, cantitate);
        items.add(newItem);
    }

    // Calculul simplu (fara oferte - ofertele se aplica in Service/Controller)
    public double calculateSubtotal() {
        double sum = 0;
        for (ComandaItem item : items) {
            sum += item.getProdus().getPret() * item.getCantitate();
        }
        return sum;
    }

    // --- GETTERS & SETTERS STANDARD ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getData() { return data; }
    public void setData(LocalDateTime data) { this.data = data; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public User getOspatar() { return ospatar; }
    public void setOspatar(User ospatar) { this.ospatar = ospatar; }
    public Masa getMasa() { return masa; }
    public void setMasa(Masa masa) { this.masa = masa; }
    public List<ComandaItem> getItems() { return items; }
    public void setItems(List<ComandaItem> items) { this.items = items; }
}