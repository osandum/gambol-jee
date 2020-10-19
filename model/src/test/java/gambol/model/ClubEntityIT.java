package gambol.model;

import java.util.List;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.UserTransaction;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author osa
 */
@RunWith(Arquillian.class)
public class ClubEntityIT {
  private static final Logger LOG = LoggerFactory.getLogger(ClubEntityIT.class);

  @Deployment
  public static Archive<?> createDeployment() {
    return
      ShrinkWrap.create(WebArchive.class, "gambol-jpa-test.war")
            .addPackage(ClubEntity.class.getPackage())
            .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
  }

  @PersistenceContext
  EntityManager em;

  @Inject
  UserTransaction utx;

  @Before
  public void loadClubs() throws Exception {
    utx.begin();
    ClubEntity c = new ClubEntity();
    c.setName("Test HC");
    c.setSlug("test-hc");
    em.persist(c);
    LOG.info("# persisted {}", c);

    c = new ClubEntity();
    c.setName("Kjøbenhavn Skøjteløber Forening");
    c.setSlug("ksf");
    em.persist(c);
    LOG.info("# persisted {}", c);

    utx.commit();
  }

  @Test
  public void shouldFindAllGamesUsingCriteriaApi() throws Exception {
    // given
    CriteriaBuilder builder = em.getCriteriaBuilder();
    CriteriaQuery<ClubEntity> criteria = builder.createQuery(ClubEntity.class);

    Root<ClubEntity> game = criteria.from(ClubEntity.class);
    criteria.select(game);
    // TIP: If you don't want to use the JPA 2 Metamodel,
    // you can change the get() method call to get("id")
    criteria.orderBy(builder.asc(game.get(ClubEntity_.id)));
    // No WHERE clause, which implies select all

    // when
    LOG.info("Selecting (using Criteria)...");
    List<ClubEntity> clubs = em.createQuery(criteria).getResultList();

    // then
    LOG.info("Found {} clubs: {}", clubs.size(), clubs);
  }
}
