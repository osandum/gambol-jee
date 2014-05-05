package gambol.ejb;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author osa
 */
@Stateless
public class App {

    @Inject
    private Logger LOG;

    @PersistenceContext
    private EntityManager em;

    public void sayHello() {
        LOG.info("Yo, environment");
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
