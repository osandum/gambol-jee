package gambol.ejb;

import gambol.model.ClubEntity;
import gambol.model.TournamentEntity;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author osa
 */
@Named("gambol")
@Stateless
public class App {

    @Inject
    private Logger LOG;

    @PersistenceContext
    private EntityManager em;

    public void sayHello() {
        LOG.info("Yo, environment");
    }

    public List<ClubEntity> getClubs() {
        // OMG!
        CriteriaQuery<ClubEntity> cq = em.getCriteriaBuilder().createQuery(ClubEntity.class);
        CriteriaQuery<ClubEntity> all = cq.select(cq.from(ClubEntity.class)); //.orderBy(Ord);
        TypedQuery<ClubEntity> allQuery = em.createQuery(all);
        List<ClubEntity> clubs = allQuery.getResultList();
        
        return clubs;
    }

    public List<TournamentEntity> getAllTournaments() {
        // OMG!
        CriteriaQuery<TournamentEntity> cq = em.getCriteriaBuilder().createQuery(TournamentEntity.class);
        CriteriaQuery<TournamentEntity> all = cq.select(cq.from(TournamentEntity.class)); //.orderBy(Ord);
        TypedQuery<TournamentEntity> allQuery = em.createQuery(all);
        List<TournamentEntity> res = allQuery.getResultList();
        
        for (TournamentEntity t: res)
            t.getFixtures().size();
        
        return res;
    }

    public void loadInitialData() {
        InputStream is = getClass().getResourceAsStream("/initial-data.yml");
        Map<String, List<Object>> oo = (Map<String, List<Object>>) new Yaml().load(is);

        LOG.info(oo.toString());

        for (Object club : oo.get("clubs")) {
            em.persist(club);
            LOG.log(Level.INFO, "{0} persisted", club);
        }

    }

}
