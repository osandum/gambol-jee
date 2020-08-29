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
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author osa
 */
@RunWith(Arquillian.class)
public class ClubEntityIT {

  @Deployment
  public static Archive<?> createDeployment() {
    return
      ShrinkWrap.create(WebArchive.class, "test.war")
            .addPackage(ClubEntity.class.getPackage())
            .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
  }

  @PersistenceContext
  EntityManager em;

  @Inject
  UserTransaction utx;

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
    System.out.println("Selecting (using Criteria)...");
    List<ClubEntity> games = em.createQuery(criteria).getResultList();

    // then
    System.out.println("Found " + games.size() + " games (using Criteria):");
  }
}
