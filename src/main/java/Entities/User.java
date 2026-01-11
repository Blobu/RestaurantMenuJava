package Entities;

import jakarta.persistence.*;
import java.util.List;

// ATENTIE MARE LA ACEST IMPORT:
import Entities.Comanda;
// Daca ai "import org.example.ComandaConsola;" sau "import Menu.Comanda;", STERGE-L!

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    // AICI ERA PROBLEMA:
    // Asigura-te ca tipul listei este 'Comanda' (cea din Entities), nu 'ComandaConsola'
    @OneToMany(mappedBy = "ospatar", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<Comanda> comenzi;

    public User() {}

    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public Role getRole() { return role; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // Getter pentru comenzi
    public List<Comanda> getComenzi() { return comenzi; }
    public void setComenzi(List<Comanda> comenzi) { this.comenzi = comenzi; }
}