package Repositories;

import Entities.Masa;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;

public class TableRepository {
    private final EntityManagerFactory emf = Persistence.createEntityManagerFactory("RestaurantPU");

    public List<Masa> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT m FROM Masa m", Masa.class).getResultList();
        } finally {
            em.close();
        }
    }

    public void initializeTables(int count) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            for (int i = 1; i <= count; i++) {
                em.persist(new Masa(i));
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}