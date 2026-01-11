package Repositories;

import Products.Produs;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;
import java.util.ArrayList;

public class ProductRepository {

    private final EntityManagerFactory emf;

    public ProductRepository() {
        // Inițializăm factory-ul o singură dată (sau îl primim prin constructor în aplicații mai mari)
        this.emf = Persistence.createEntityManagerFactory("RestaurantPU");
    }

    public List<Produs> findAllProducts() {
        EntityManager em = emf.createEntityManager();
        try {
            // Aceasta este logica SQL pură mutată din Controller
            return em.createQuery("SELECT p FROM Produs p", Produs.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public void save(Produs p) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            if (p.getId() == null) {
                em.persist(p);
            } else {
                em.merge(p); // Update
            }
            em.getTransaction().commit();
        } finally { em.close(); }
    }

    public void deleteById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Produs p = em.find(Produs.class, id);
            if (p != null) em.remove(p);
            em.getTransaction().commit();
        } finally { em.close(); }
    }

    // Metoda critică pentru Import JSON (resetare tabel)
    public void deleteAll() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            // Ștergem întâi dependențele (ComandaItem) dacă nu avem Cascade
            em.createQuery("DELETE FROM ComandaItem").executeUpdate();
            em.createQuery("DELETE FROM Comanda").executeUpdate();
            em.createQuery("DELETE FROM Produs").executeUpdate();
            em.getTransaction().commit();
        } finally { em.close(); }
    }

    // Putem adăuga metode de cleanup dacă e nevoie
    public void close() {
        if (emf.isOpen()) emf.close();
    }
}