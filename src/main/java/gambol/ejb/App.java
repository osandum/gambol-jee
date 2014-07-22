package gambol.ejb;

import gambol.model.ClubEntity;
import gambol.model.FixtureEntity;
import gambol.model.FixtureSideEntity;
import gambol.model.SeasonEntity;
import gambol.model.TournamentEntity;
import gambol.model.TournamentTeamEntity;
import gambol.xml.Fixture;
import gambol.xml.FixtureSideRole;
import gambol.xml.Side;
import gambol.xml.Tournament;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
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

    public ClubEntity findClub(String slug) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<ClubEntity> query = builder.createQuery(ClubEntity.class);
        Root<ClubEntity> root = query.from(ClubEntity.class);
        query.select(root);
        query.where(builder.equal(root.get("slug"), slug));
        return em.createQuery(query).setMaxResults(1).getSingleResult();
    }

    public ClubEntity findOrCreateClub(String slug) {
        try {
            return findClub(slug);
        } catch (NoResultException ex) {
            ClubEntity res = new ClubEntity();
            res.setSlug(slug);
            res.setName(slug);
            em.persist(res);
            return res;
        }
    }

    public SeasonEntity findSeason(String slug) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<SeasonEntity> query = builder.createQuery(SeasonEntity.class);
        Root<SeasonEntity> root = query.from(SeasonEntity.class);
        query.select(root);
        query.where(builder.equal(root.get("id"), slug));
        return em.createQuery(query).setMaxResults(1).getSingleResult();
    }

    public SeasonEntity findOrCreateSeason(String slug) {
        try {
            return findSeason(slug);
        } catch (NoResultException ex) {
            SeasonEntity res = new SeasonEntity();
            res.setId(slug);
            res.setName(slug);
            em.persist(res);
            return res;
        }
    }

    public List<ClubEntity> getClubs() {
        // OMG!
        CriteriaQuery<ClubEntity> cq = em.getCriteriaBuilder().createQuery(ClubEntity.class);
        CriteriaQuery<ClubEntity> all = cq.select(cq.from(ClubEntity.class)); //.orderBy(Ord);
        TypedQuery<ClubEntity> allQuery = em.createQuery(all);
        List<ClubEntity> clubs = allQuery.getResultList();

        return clubs;
    }

    public TournamentEntity findTournamentBySourceRef(String sourceRef) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<TournamentEntity> query = builder.createQuery(TournamentEntity.class);
        Root<TournamentEntity> root = query.from(TournamentEntity.class);
        query.select(root);
        query.where(builder.equal(root.get("sourceRef"), sourceRef));
        return em.createQuery(query).getSingleResult();
    }

    
    
    public TournamentEntity putTournament(Tournament tt) {
        String sourceRef = tt.getSourceRef();
        String seasonId = tt.getSeason();
        if (seasonId == null)
            throw new IllegalArgumentException("Invalid season ID");
        String slug = tt.getSlug();
        String name = tt.getTitle();
        if (name == null)
            throw new IllegalArgumentException("Invalid tournament name");
        if (slug == null)
            slug = name.toLowerCase().replaceAll("[-_,. ]+", "-");

        TournamentEntity entity;
        try {
            entity = findTournamentBySourceRef(sourceRef);
        } catch (NoResultException ex) {
            SeasonEntity season = findOrCreateSeason(seasonId);
            entity = TournamentEntity.create(season, sourceRef);
        }
        entity.setSlug(slug);
        entity.setName(name);

        em.persist(entity);
        
        updateFixtures(entity, tt.getFixtures());
        
        return entity;
    }

    public List<TournamentEntity> getAllTournaments() {
        // OMG!
        CriteriaQuery<TournamentEntity> cq = em.getCriteriaBuilder().createQuery(TournamentEntity.class);
        CriteriaQuery<TournamentEntity> all = cq.select(cq.from(TournamentEntity.class)); //.orderBy(Ord);
        TypedQuery<TournamentEntity> allQuery = em.createQuery(all);
        List<TournamentEntity> res = allQuery.getResultList();

        for (TournamentEntity t : res) {
            t.getFixtures().size();
        }

        return res;
    }

    public void loadInitialData() {
        InputStream is = getClass().getResourceAsStream("/initial-data.yml");
        Map<String, List<Object>> oo = (Map<String, List<Object>>) new Yaml().load(is);

        LOG.info(oo.toString());

        for (Object o : oo.get("clubs")) {
            em.persist(o);
            LOG.log(Level.INFO, "{0} persisted", o);
        }

        for (Object o : oo.get("fixtures")) {
            FixtureEntity fixture = (FixtureEntity) o;
            em.persist(fixture.getHomeSide());
            em.persist(fixture.getAwaySide());
            em.persist(fixture);
            LOG.log(Level.INFO, "{0} persisted", fixture);
        }
    }

    public TournamentTeamEntity findTeam(TournamentEntity tournament, String clubRef, String teamName) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<TournamentTeamEntity> query = builder.createQuery(TournamentTeamEntity.class);
        Root<TournamentTeamEntity> root = query.from(TournamentTeamEntity.class);
        query.select(root);
        query.where(builder.equal(root.get("tournament"), tournament),
                builder.equal(root.get("name"), teamName),
                builder.equal(root.get("club").get("slug"), clubRef));
        return em.createQuery(query).setMaxResults(1).getSingleResult();

    }

    public TournamentTeamEntity findOrCreateTeam(TournamentEntity tournament, String clubRef, String teamName) {
        try {
            return findTeam(tournament, clubRef, teamName);
        } catch (NoResultException ex) {
            TournamentTeamEntity res = new TournamentTeamEntity();
            res.setTournament(tournament);
            ClubEntity club = findOrCreateClub(clubRef);
            res.setClub(club);
            res.setName(teamName);
            res.setSlug(asSlug(teamName, 16));
            em.persist(res);
            return res;
        }
    }

    private static String asSlug(String name, int maxLength) {
        String slug = name.toLowerCase().replaceAll("[-_,. ]+", "-");
        if (slug.length() > maxLength)
            slug = slug.substring(0, maxLength);
        return slug;
    }
    
    public void updateOrCreateClub(String slug, ClubEntity c) {
        ClubEntity entity = findOrCreateClub(slug);
        entity.setName(c.getName());
        entity.setAliasNames(c.getAliasNames());
        entity.setLatitude(c.getLatitude());
        entity.setLongitude(c.getLongitude());
        entity.setAddress(c.getAddress());
    }

    public void updateFixtures(TournamentEntity t, List<Fixture> fixtures) {
        Map<String, FixtureEntity> all = new HashMap<String, FixtureEntity>();
        for (FixtureEntity f : t.getFixtures()) {
            all.put(f.getSourceRef(), f);
        }

        List<FixtureEntity> nf = new LinkedList<FixtureEntity>();
        for (Fixture fo : fixtures) {
            FixtureEntity f = all.get(fo.getSourceRef());
            if (f == null) {
                // new fixture
                f = new FixtureEntity();
                f.setTournament(t);
                domain2entity(fo, f);
                em.persist(f);
                nf.add(f);
                LOG.info(fo.getSourceRef() + " not found: new fixture created");
            } else {
                // existing fixture, update:
                f.setTournament(t);
                domain2entity(fo, f);
                LOG.info(fo.getSourceRef() + ": fixture updated");
            }
            //:.. 
        }
    }

    private void domain2entity(Fixture f, FixtureEntity entity) {
        entity.setStartTime(f.getStartTime());
        entity.setEndTime(f.getEndTime());

        if (entity.getStartTime() != null && entity.getEndTime() == null) {
            Calendar d = Calendar.getInstance();
            d.setTime(entity.getStartTime());
            d.add(Calendar.MINUTE, 90);
            entity.setEndTime(d.getTime());
        }

        entity.setSourceRef(f.getSourceRef());
        for (Side s : f.getSides()) {
            FixtureSideEntity fe = new FixtureSideEntity();
            Side.Team team = s.getTeam();
            String clubRef = team.getClubRef();
            fe.setTeam(findOrCreateTeam(entity.getTournament(), clubRef, team.getValue()));
            fe.setScore(s.getScore());
            FixtureSideRole role = s.getRole();
            if (FixtureSideRole.HOME.equals(role)) {
                entity.setHomeSide(fe);
            } else if (FixtureSideRole.AWAY.equals(role)) {
                entity.setAwaySide(fe);
            }
        }
    }
}
