package gambol.ejb;

import gambol.model.Club;
import gambol.model.Tournament;
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

    public List<Club> getClubs() {
        // OMG!
        CriteriaQuery<Club> cq = em.getCriteriaBuilder().createQuery(Club.class);
        CriteriaQuery<Club> all = cq.select(cq.from(Club.class)); //.orderBy(Ord);
        TypedQuery<Club> allQuery = em.createQuery(all);
        List<Club> clubs = allQuery.getResultList();
        
        return clubs;
    }

    public List<Tournament> getAllTournaments() {
        // OMG!
        CriteriaQuery<Tournament> cq = em.getCriteriaBuilder().createQuery(Tournament.class);
        CriteriaQuery<Tournament> all = cq.select(cq.from(Tournament.class)); //.orderBy(Ord);
        TypedQuery<Tournament> allQuery = em.createQuery(all);
        List<Tournament> res = allQuery.getResultList();
        
        for (Tournament t: res)
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
