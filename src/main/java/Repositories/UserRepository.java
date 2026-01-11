package Repositories;

import Entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;

public class UserRepository {
    private final EntityManagerFactory emf = Persistence.createEntityManagerFactory("RestaurantPU");

    public List<User> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT u FROM User u", User.class).getResultList();
        } finally {
            em.close();
        }
    }

    public void save(User user) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void deleteById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            User u = em.find(User.class, id);
            if (u != null) {
                em.remove(u); // Cascade delete ar trebui să fie configurat în Entity
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}