package Entities;

import jakarta.persistence.*;

@Entity
@Table(name = "mese")
public class Masa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int numarMasa;
    private boolean ocupata;

    public Masa() {}
    public Masa(int numarMasa) { this.numarMasa = numarMasa; this.ocupata = false; }

    // Getters/Setters
    public Long getId() { return id; }
    public int getNumarMasa() { return numarMasa; }
    public boolean isOcupata() { return ocupata; }
    public void setOcupata(boolean ocupata) { this.ocupata = ocupata; }

    @Override
    public String toString() { return "Masa " + numarMasa + (ocupata ? " (Ocupata)" : " (Libera)"); }
}