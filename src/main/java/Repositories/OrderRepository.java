package Repositories;

import Entities.*;
import Products.Produs;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderRepository {
    private final EntityManagerFactory emf = Persistence.createEntityManagerFactory("RestaurantPU");

    // Metoda primește ID-uri pentru a reîncărca entitățile în contextul de persistență curent
    public void createOrder(Long tableId, Long waiterId, List<ComandaItem> uiItems, double totalValue) throws Exception {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            // 1. Găsim entitățile părinte (Masa și Ospătarul)
            Masa masa = em.find(Masa.class, tableId);
            User ospatar = em.find(User.class, waiterId);

            // 2. Creăm Comanda
            Comanda comanda = new Comanda();
            comanda.setData(LocalDateTime.now());
            comanda.setStatus("IN_PROGRES");
            comanda.setMasa(masa);
            comanda.setOspatar(ospatar);
            comanda.setTotal(totalValue);

            // 3. Transformăm itemele din UI în iteme persistente
            List<ComandaItem> dbItems = new ArrayList<>();
            for (ComandaItem uiItem : uiItems) {
                // Reatașăm produsul la sesiune
                Produs p = em.find(Produs.class, uiItem.getProdus().getId());

                ComandaItem dbItem = new ComandaItem(comanda, p, uiItem.getCantitate());
                dbItems.add(dbItem);
            }
            comanda.setItems(dbItems);

            // 4. Salvăm totul (Cascade ar trebui să se ocupe de items, dar persistăm explicit comanda)
            em.persist(comanda);

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Comanda> findAllHistory() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT c FROM Comanda c ORDER BY c.data DESC", Comanda.class).getResultList();
        } finally {
            em.close();
        }
    }
}