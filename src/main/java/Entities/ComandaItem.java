package Entities;

import jakarta.persistence.*;
import Products.Produs;

@Entity
@Table(name = "comanda_items")
public class ComandaItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "comanda_id")
    private Comanda comanda;

    @ManyToOne
    @JoinColumn(name = "produs_id")
    private Produs produs;

    private int cantitate;

    public ComandaItem() {}
    public ComandaItem(Comanda comanda, Produs produs, int cantitate) {
        this.comanda = comanda;
        this.produs = produs;
        this.cantitate = cantitate;
    }

    // Getters
    public Produs getProdus() { return produs; }
    public int getCantitate() { return cantitate; }
    public void setCantitate(int cantitate) { this.cantitate = cantitate; }
}