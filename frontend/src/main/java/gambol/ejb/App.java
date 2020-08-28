package gambol.ejb;

import gambol.model.ClubEntity;
import gambol.model.ClubEntity_;
import gambol.model.FixtureEntity;
import gambol.model.FixtureEntity_;
import gambol.model.FixtureEventEntity;
import gambol.model.FixturePlayerEntity;
import gambol.model.FixturePlayerEntity_;
import gambol.model.FixtureSideEntity;
import gambol.model.FixtureSideEntity_;
import gambol.model.GoalEventEntity;
import gambol.model.GoalEventEntity_;
import gambol.model.PenaltyEventEntity;
import gambol.model.PersonEntity;
import gambol.model.PersonEntity_;
import gambol.model.SeasonEntity;
import gambol.model.SeasonEntity_;
import gambol.model.SeriesEntity;
import gambol.model.SeriesEntity_;
import gambol.model.TournamentEntity;
import gambol.model.TournamentEntity_;
import gambol.model.TeamEntity;
import gambol.model.TeamEntity_;
import gambol.model.GameTime;
import gambol.model.ModelUtil;
import gambol.model.PenaltyEventEntity_;
import gambol.xml.Event;
import gambol.xml.Fixture;
import gambol.xml.FixtureEvents;
import gambol.xml.FixtureSideRole;
import gambol.xml.GameSituation;
import gambol.xml.Gamesheet;
import gambol.xml.GamesheetStatus;
import gambol.xml.GoalEvent;
import gambol.xml.PenaltyEvent;
import gambol.xml.Player;
import gambol.xml.PlayerRef;
import gambol.xml.Roster;
import gambol.xml.ScheduleStatus;
import gambol.xml.Side;
import gambol.xml.TeamDef;
import gambol.xml.Tournament;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author osa
 */
@Named("gambol")
@Stateless
public class App {

    private final static Logger LOG = LoggerFactory.getLogger(App.class);

    @PersistenceContext
    private EntityManager em;

    public void sayHello() {
        LOG.info("Yo, environment");
    }

    public PersonEntity getPersonById(long personId) {
        return em.find(PersonEntity.class, personId);
    }

    public PersonEntity findPerson(String slug) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<PersonEntity> query = builder.createQuery(PersonEntity.class);
        Root<PersonEntity> root = query.from(PersonEntity.class);
        query.select(root);
        query.where(builder.equal(root.get(PersonEntity_.slug), slug));
        return em.createQuery(query).setMaxResults(1).getSingleResult();
    }

    public ClubEntity findClub(String slug) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<ClubEntity> query = builder.createQuery(ClubEntity.class);
        Root<ClubEntity> root = query.from(ClubEntity.class);
        query.select(root);
        query.where(builder.equal(root.get(ClubEntity_.slug), slug));
        return em.createQuery(query).setMaxResults(1).getSingleResult();
    }

    public ClubEntity findOrCreateClub(String slug) {
        if (StringUtils.isEmpty(slug))
            throw new IllegalArgumentException("no club ref");

        try {
            return findClub(slug);
        }
        catch (NoResultException ex) {
            ClubEntity res = new ClubEntity();
            res.setSlug(slug);
            res.setName(slug);
            em.persist(res);
            LOG.info("# new club: '{}' created", slug);
            return res;
        }
    }

    public SeasonEntity findSeason(String slug) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<SeasonEntity> query = builder.createQuery(SeasonEntity.class);
        Root<SeasonEntity> root = query.from(SeasonEntity.class);
        query.select(root);
        query.where(builder.equal(root.get(SeasonEntity_.id), slug));
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

    public List<SeasonEntity> getSeasons() {
        // OMG!
        CriteriaQuery<SeasonEntity> cq = em.getCriteriaBuilder().createQuery(SeasonEntity.class);
        CriteriaQuery<SeasonEntity> all = cq.select(cq.from(SeasonEntity.class)); //.orderBy(Ord);
        return em.createQuery(all).getResultList();
    }

    public List<SeriesEntity> getSeries() {
        CriteriaQuery<SeriesEntity> cq = em.getCriteriaBuilder().createQuery(SeriesEntity.class);
        CriteriaQuery<SeriesEntity> all = cq.select(cq.from(SeriesEntity.class)); //.orderBy(Ord);
        return em.createQuery(all).getResultList();
    }

    private SeriesEntity findSeries(String slug) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<SeriesEntity> query = builder.createQuery(SeriesEntity.class);
        Root<SeriesEntity> root = query.from(SeriesEntity.class);
        query.select(root);
        query.where(builder.equal(root.get(SeriesEntity_.slug), slug));
        return em.createQuery(query).setMaxResults(1).getSingleResult();
    }

    private SeriesEntity findOrCreateSeries(String slug) {
        try {
            return findSeries(slug);
        } catch (NoResultException ex) {
            SeriesEntity res = new SeriesEntity();
            res.setSlug(slug);
            res.setName(slug);
            res.setFixtureDuration(60);
            em.persist(res);
            LOG.info("{} created", res);
            return res;
        }
    }


    public List<ClubEntity> getClubs() {
        CriteriaBuilder builder = em.getCriteriaBuilder();

        // OMG!
        CriteriaQuery<ClubEntity> q = builder.createQuery(ClubEntity.class);
        Root<ClubEntity> club = q.from(ClubEntity.class);
        q.orderBy(builder.asc(club.get(ClubEntity_.country)),
                   builder.asc(club.get(ClubEntity_.name)));

        List<ClubEntity> clubs = em.createQuery(q).getResultList();

        return clubs;
    }


    public TournamentEntity putTournamentSrc(Tournament tt) {
        String sourceRef = tt.getSourceRef();
        String seasonId = tt.getSeason();
        if (seasonId == null)
            throw new IllegalArgumentException("Invalid season ID");
        SeasonEntity season = findOrCreateSeason(seasonId);

        String slug = tt.getSlug();
        String name = tt.getTitle();
        if (name == null)
            throw new IllegalArgumentException("Invalid tournament name");
        if (slug == null)
            slug = name.toLowerCase().replaceAll("[-_,. ]+", "-");
        LOG.info("updating tournament {} \"{}\"", slug, name);

        String seriesSlug = slug.replaceAll("-[^-]*$", "");
        SeriesEntity series = findOrCreateSeries(seriesSlug);

        TournamentEntity entity;
        try {
            entity = findTournamentBySourceRef(sourceRef);
            entity.setSeason(season);
            entity.setSeries(series);
        } catch (NoResultException ex) {
            entity = TournamentEntity.create(season, series, sourceRef);
        }
        entity.setSlug(slug);
        entity.setName(name);

        em.persist(entity);

        updateFixtures(entity, tt.getFixtures());

        String arenaClubRef = tt.getArena();
        if (arenaClubRef != null) {
            ClubEntity arena = findOrCreateClub(arenaClubRef);
            LOG.info(name + " is held at " + arena);
            entity.setArena(arena);
        }

        return entity;
    }

    public TournamentEntity getTournament(String seasonId, String slug) {
        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<TournamentEntity> q = builder.createQuery(TournamentEntity.class);
        Root<TournamentEntity> tournaments = q.from(TournamentEntity.class);
        q.select(tournaments);
        q.where(
                builder.equal(tournaments.get(TournamentEntity_.slug), slug),
                builder.equal(tournaments.get(TournamentEntity_.season).get(SeasonEntity_.id), seasonId));

        return em.createQuery(q).getSingleResult();
    }

    public List<TournamentEntity> getAllTournaments() {
        // OMG!
        CriteriaQuery<TournamentEntity> cq = em.getCriteriaBuilder().createQuery(TournamentEntity.class);
        CriteriaQuery<TournamentEntity> all = cq.select(cq.from(TournamentEntity.class)); //.orderBy(Ord);
        TypedQuery<TournamentEntity> allQuery = em.createQuery(all);
        List<TournamentEntity> res = allQuery.getResultList();

        for (TournamentEntity t : res)
            t.getFixtures().size();

        return res;
    }

    public void loadInitialData() {
        if (!getSeasons().isEmpty())
            return;

        InputStream is = getClass().getResourceAsStream("/initial-data.yml");
        Map<String, List<Object>> oo = (Map<String, List<Object>>) new Yaml().load(is);

        LOG.info(oo.toString());

        for (Object o : oo.get("clubs")) {
            em.persist(o);
            LOG.info("{} persisted", o);
        }

        for (Object o : oo.get("fixtures")) {
            FixtureEntity fixture = (FixtureEntity) o;
            em.persist(fixture.getHomeSide());
            em.persist(fixture.getAwaySide());
            em.persist(fixture);
            LOG.info("{} persisted", fixture);
        }
    }

    public TeamEntity findTeam(TournamentEntity tournament, String clubRef, String teamName) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<TeamEntity> query = builder.createQuery(TeamEntity.class);
        Root<TeamEntity> root = query.from(TeamEntity.class);
        query.select(root);
        query.where(builder.equal(root.get("tournament"), tournament),
                builder.equal(root.get("name"), teamName),
                builder.equal(root.get("club").get("slug"), clubRef));
        return em.createQuery(query).setMaxResults(1).getSingleResult();

    }

    public TeamEntity findOrCreateTeam(TournamentEntity tournament, String clubRef, String teamName) {
        try {
            return findTeam(tournament, clubRef, teamName);
        }
        catch (NoResultException ex) {
            TeamEntity res = new TeamEntity();
            res.setTournament(tournament);
            ClubEntity club = findOrCreateClub(clubRef);
            res.setClub(club);
            res.setName(teamName);
            res.setSlug(club.getSlug());
            em.persist(res);
            LOG.info("# new team: '{}' created for {} {}", teamName, tournament.getName(), tournament.getSeason().getName());
            return res;
        }
    }

    public void updateOrCreateSeries(String slug, SeriesEntity s) {
        SeriesEntity entity = findOrCreateSeries(slug);
        entity.setName(s.getName());
        entity.setFixtureDuration(s.getFixtureDuration());
    }

    public void updateOrCreateClub(String slug, ClubEntity c) {
        ClubEntity entity = findOrCreateClub(slug);
        entity.setName(c.getName());
        entity.setAliasNames(c.getAliasNames());
        entity.setLatitude(c.getLatitude());
        entity.setLongitude(c.getLongitude());
        entity.setAddress(c.getAddress());
        entity.setCountryIso2(c.getCountryIso2());
    }

    public void updateFixtures(TournamentEntity t, List<Fixture> fixtures) {
        // Uh-oh, this thing leaves orphaned FixtureSideEntity rows behind.

        // 1) read all fixtures currently in the database, mapped by sourceRef
        Map<String, FixtureEntity> all = new HashMap<>();
        for (FixtureEntity f : t.getFixtures())
            all.put(f.getSourceRef(), f);

        // 2) traverse the input list of fixtures. Lookup (and remove) in map from
        //    step above, and insert or update fixture in database accordingly
        for (Fixture fo : fixtures) {
            FixtureEntity f = all.remove(fo.getSourceRef());
            if (f == null) {
                // new fixture
                f = new FixtureEntity();
                f.setTournament(t);
                f.setSheet(GamesheetStatus.MISSING);
                domain2entity(fo, f);
                em.persist(f);
                LOG.info(fo.getSourceRef() + " not found: new fixture created: " + f);
            }
            else {
                // existing fixture, update:
                f.setTournament(t);
                domain2entity(fo, f);
                LOG.info(fo.getSourceRef() + ": fixture updated: " + f);
            }
        }

        // 3) delete fixtures, if any, still remaining in map from step 1).
        for (FixtureEntity f : all.values()) {
            // this all *shuould* be achievable with cascade = ALL and
            // orphanRemoval = true in some combination, but I didn't manage
            // to get it working, so here we go handheld:
            for (FixtureEventEntity fe : f.getEvents())
                em.remove(fe);
            for (FixturePlayerEntity fp : f.getHomeSide().getPlayers())
                em.remove(fp);
            for (FixturePlayerEntity fp : f.getAwaySide().getPlayers())
                em.remove(fp);
            LOG.info("{}: gone", f.getSourceRef());

            em.remove(f);
        }
    }


    private void domain2entity(Fixture f, FixtureEntity entity) {
        entity.setStatus(f.getSchedule());
        entity.setStartTime(f.getStartTime());
        entity.setEndTime(f.getEndTime());

        if (entity.getStartTime() != null && entity.getEndTime() == null) {
            Calendar d = Calendar.getInstance();
            d.setTime(entity.getStartTime());
            int durationMinutes = entity.getTournament().getSeries().getFixtureDuration();
            d.add(Calendar.MINUTE, durationMinutes);
            entity.setEndTime(d.getTime());
        }

        FixtureSideEntity homeSide = entity.getHomeSide();
        FixtureSideEntity awaySide = entity.getAwaySide();

        for (Side s : f.getSides()) {
            FixtureSideRole role = s.getRole();
            if (FixtureSideRole.HOME.equals(role)) {
                homeSide = domain2entity(s, homeSide, entity.getTournament());
                entity.setHomeSide(homeSide);
            } else if (FixtureSideRole.AWAY.equals(role)) {
                awaySide = domain2entity(s, awaySide, entity.getTournament());
                entity.setAwaySide(awaySide);
            }
        }

        entity.setMatchNumber(f.getMatchNumber());

        entity.setSourceRef(f.getSourceRef());
    }

    private FixtureSideEntity domain2entity(Side s, FixtureSideEntity fe, TournamentEntity tournament) {
        if (fe == null)
            fe = new FixtureSideEntity();
        Side.Team team = s.getTeam();
        String clubRef = team.getClubRef();
        fe.setTeam(findOrCreateTeam(tournament, clubRef, team.getValue()));
        fe.setScore(s.getScore());
        return fe;
    }

    public TournamentEntity findTournamentBySourceRef(String sourceRef) {
        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<TournamentEntity> q = builder.createQuery(TournamentEntity.class);
        Root<TournamentEntity> tournaments = q.from(TournamentEntity.class);
        q.select(tournaments);
        q.where(builder.equal(tournaments.get(TournamentEntity_.sourceRef), sourceRef));

        return em.createQuery(q).getSingleResult();
    }

    public FixtureEntity findFixtureBySourceRef(String sourceRef) {
        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<FixtureEntity> select = builder.createQuery(FixtureEntity.class);
        Root<FixtureEntity> fixtures = select.from(FixtureEntity.class);
        select.where(builder.like(fixtures.get(FixtureEntity_.sourceRef), sourceRef + "%"));

        TypedQuery<FixtureEntity> q = em.createQuery(select);

        try {
            return q.getSingleResult();
        }
        catch (NoResultException ex) {
            throw new IllegalArgumentException(sourceRef + ": fixture not found");
        }
    }


    public FixtureEntity getFixture(String seasonId, String slug, String matchNumber) {
        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<FixtureEntity> q = builder.createQuery(FixtureEntity.class);
        Root<FixtureEntity> fixture = q.from(FixtureEntity.class);
        Path<TournamentEntity> tournament = fixture.get(FixtureEntity_.tournament);
        Path<SeasonEntity> season = tournament.get(TournamentEntity_.season);
        q.where(builder.equal(fixture.get(FixtureEntity_.matchNumber), matchNumber),
                builder.equal(tournament.get(TournamentEntity_.slug), slug),
                builder.equal(season.get(SeasonEntity_.id), seasonId));

        return em.createQuery(q).getSingleResult();
    }

    public FixtureEntity getFixtureById(long fixtureId) {
        return em.find(FixtureEntity.class, fixtureId);
    }

    public List<GoalEventEntity> getGoalsByPlayer(long personId) {
        long t1 = System.currentTimeMillis();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<GoalEventEntity> q = builder.createQuery(GoalEventEntity.class);
        Root<GoalEventEntity> goal = q.from(GoalEventEntity.class);
        Join<FixturePlayerEntity, PersonEntity> scorer = goal.join(GoalEventEntity_.player).join(FixturePlayerEntity_.person);
        //  Join<FixturePlayerEntity, PersonEntity> assist = goal.join(GoalEventEntity_.assists).join(FixturePlayerEntity_.person);
        Join<GoalEventEntity, FixtureEntity> fixture = goal.join(GoalEventEntity_.fixture);

        q.where(builder.equal(scorer.get(PersonEntity_.id), personId));
        q.orderBy(builder.asc(fixture.get(FixtureEntity_.startTime)),
                  builder.asc(goal.get(GoalEventEntity_.gameTimeSecond)));

        List<GoalEventEntity> res = em.createQuery(q).getResultList();
        long t2 = System.currentTimeMillis();

        LOG.info(personId + ": " + res.size() + " goal(s) retrieved ("+(t2-t1)+"ms)");

        return res;
    }

    public List<PenaltyEventEntity> getPenaltiesByPlayer(long personId) {
        long t1 = System.currentTimeMillis();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<PenaltyEventEntity> q = builder.createQuery(PenaltyEventEntity.class);
        Root<PenaltyEventEntity> penalties = q.from(PenaltyEventEntity.class);
        Join<FixturePlayerEntity, PersonEntity> scorer = penalties.join(PenaltyEventEntity_.player).join(FixturePlayerEntity_.person);
        //  Join<FixturePlayerEntity, PersonEntity> assist = goal.join(GoalEventEntity_.assists).join(FixturePlayerEntity_.person);
        Join<PenaltyEventEntity, FixtureEntity> fixture = penalties.join(PenaltyEventEntity_.fixture);

        q.where(builder.equal(scorer.get(PersonEntity_.id), personId));
        q.orderBy(builder.asc(fixture.get(FixtureEntity_.startTime)),
                  builder.asc(penalties.get(GoalEventEntity_.gameTimeSecond)));

        List<PenaltyEventEntity> res = em.createQuery(q).getResultList();
        long t2 = System.currentTimeMillis();

        LOG.info(personId + ": " + res.size() + " penalties(s) retrieved ("+(t2-t1)+"ms)");

        return res;
    }

    public List<FixturePlayerEntity> getFixturesByPlayer(long personId) {
        long t1 = System.currentTimeMillis();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<FixturePlayerEntity> q = builder.createQuery(FixturePlayerEntity.class);
        Root<FixturePlayerEntity> played = q.from(FixturePlayerEntity.class);
        Join<FixturePlayerEntity, PersonEntity> player = played.join(FixturePlayerEntity_.person);
        Join<FixturePlayerEntity, FixtureEntity> fixture = played.join(FixturePlayerEntity_.fixture);

        q.where(builder.equal(player.get(PersonEntity_.id), personId));
        q.orderBy(builder.asc(fixture.get(FixtureEntity_.startTime)));


        List<FixturePlayerEntity> res = em.createQuery(q).getResultList();

        long t2 = System.currentTimeMillis();

        LOG.info(personId + ": " + res.size() + " fixture(s) retrieved ("+(t2-t1)+"ms)");

        return res;
    }

    public Map<PersonEntity, Map<ClubEntity, Set<SeasonEntity>>> getPlayers(PlayersQueryParam param) {
        long t1 = System.currentTimeMillis();

        CriteriaBuilder b = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> tq = b.createTupleQuery();

        Root<FixturePlayerEntity> fp = tq.from(FixturePlayerEntity.class);

        Path<PersonEntity> _person = fp.get(FixturePlayerEntity_.person);
        Path<FixtureSideEntity> _side = fp.get(FixturePlayerEntity_.side);
        Path<TeamEntity> _team = _side.get(FixtureSideEntity_.team);
        Path<ClubEntity> _club = _team.get(TeamEntity_.club);
        Path<TournamentEntity> _tournament = _team.get(TeamEntity_.tournament);
        Path<SeasonEntity> _season = _tournament.get(TournamentEntity_.season);

        tq.select(b.tuple(_person, _club, _season)).distinct(true);

        Expression<String> _firstName = b.upper(b.function("unaccent", String.class, _person.get(PersonEntity_.firstNames)));
        Expression<String> _lastName = b.upper(b.function("unaccent", String.class, _person.get(PersonEntity_.lastName)));
        Predicate a = b.conjunction();
        List<Expression<Boolean>> wheres = a.getExpressions();

        int s = 0;
        if (param.getName() != null) {
            for (String n : param.getName().split("\\h+"))
            {
                String p = ModelUtil.unaccent(n).toUpperCase().trim();
                s += p.length();
                wheres
                    .add(b.or(b.like(_firstName, "%" + p + "%"), b.like(_lastName, "%" + p + "%")));
            }
        }

        if (s < 3)
            throw new IllegalArgumentException("query at least three characters");

        tq.where(a);
      //tq.orderBy(b.asc(_lastName), b.asc(_firstName));

        List<Tuple> res = em.createQuery(tq).setMaxResults(1001).getResultList();

        Comparator<PersonEntity> byLastName = (PersonEntity p1, PersonEntity p2) -> {
            return p1.getLastName().compareToIgnoreCase(p2.getLastName());
        };
        Comparator<PersonEntity> byFirstName = (PersonEntity p1, PersonEntity p2) -> {
            return p1.getFirstNames().compareToIgnoreCase(p2.getFirstNames());
        };
        Map<PersonEntity,Map<ClubEntity,Set<SeasonEntity>>> rr =
                new TreeMap<>(byLastName.thenComparing(byFirstName));
        res.stream().forEach((entity) -> {
            PersonEntity person = (PersonEntity)entity.get(0);
            ClubEntity club = (ClubEntity)entity.get(1);
            SeasonEntity season = (SeasonEntity)entity.get(2);

            Map<ClubEntity, Set<SeasonEntity>> pp = rr.get(person);
            if (pp == null) {
                pp = new HashMap<>();
                rr.put(person, pp);
            }
            Set<SeasonEntity> cc = pp.get(club);
            if (cc == null) {
                cc = new TreeSet<>();
                pp.put(club, cc);
            }
            cc.add(season);
        });


        long t2 = System.currentTimeMillis();

        LOG.info(param + ": " + res.size() + " player(s) retrieved ("+(t2-t1)+"ms)");

        return rr;
    }

    public List<FixturePlayerEntity> getPlayers__(PlayersQueryParam param) {
        long t1 = System.currentTimeMillis();

        CriteriaBuilder b = em.getCriteriaBuilder();

        CriteriaQuery<FixturePlayerEntity> q = b.createQuery(FixturePlayerEntity.class);
        Root<FixturePlayerEntity> players = q.from(FixturePlayerEntity.class);

        Path<PersonEntity> person = players.get(FixturePlayerEntity_.person);
        Path<FixtureEntity> fixture = players.get(FixturePlayerEntity_.fixture);

        Expression<String> _firstName = b.upper(b.function("unaccent", String.class, person.get(PersonEntity_.firstNames)));
        Expression<String> _lastName = b.upper(b.function("unaccent", String.class, person.get(PersonEntity_.lastName)));
        Predicate a = b.conjunction();
        List<Expression<Boolean>> wheres = a.getExpressions();

        int s = 0;
        if (param.getName() != null) {
            for (String n : param.getName().split("\\h+"))
            {
                String p = n.toUpperCase().trim();
                s += p.length();
                wheres
                    .add(b.or(b.like(_firstName, "%" + p + "%"), b.like(_lastName, "%" + p + "%")));
            }
        }

        if (s < 3)
            throw new IllegalArgumentException("query at least three characters");

        q.where(a);
        q.orderBy(b.asc(_lastName), b.asc(_firstName));
        List<FixturePlayerEntity> res =
                em.createQuery(q).setMaxResults(101).getResultList();

        long t2 = System.currentTimeMillis();

        LOG.info(param + ": " + res.size() + " player(s) retrieved ("+(t2-t1)+"ms)");

        return res;
    }

    public List<PersonEntity> getPeople(PlayersQueryParam param) {
        long t1 = System.currentTimeMillis();

        CriteriaBuilder b = em.getCriteriaBuilder();

        CriteriaQuery<PersonEntity> q = b.createQuery(PersonEntity.class);
        Root<PersonEntity> people = q.from(PersonEntity.class);

        Expression<String> _firstName = b.upper(b.function("unaccent", String.class, people.get(PersonEntity_.firstNames)));
        Expression<String> _lastName = b.upper(b.function("unaccent", String.class, people.get(PersonEntity_.lastName)));

        Predicate a = b.conjunction();
        List<Expression<Boolean>> wheres = a.getExpressions();

        int s = 0;
        if (param.getName() != null) {
            for (String n : param.getName().split("\\h+"))
            {
                String p = n.toUpperCase().trim();
                s += p.length();
                wheres
                    .add(b.or(b.like(_firstName, "%" + p + "%"), b.like(_lastName, "%" + p + "%")));
            }
        }

        if (s < 3)
            throw new IllegalArgumentException("query at least three characters");

        q.where(a);
        q.orderBy(b.asc(_lastName), b.asc(_firstName));
        List<PersonEntity> res =
                em.createQuery(q).setMaxResults(101).getResultList();

        long t2 = System.currentTimeMillis();

        LOG.info("{}: {} person(s) retrieved ({}ms)", param, res.size(), t2-t1);

        return res;
    }

    public List<FixtureEntity> _pageOfFixtures(FixturesQueryParam param) {
        long t1 = System.currentTimeMillis();

        CriteriaBuilder b = em.getCriteriaBuilder();

        CriteriaQuery<FixtureEntity> q = b.createQuery(FixtureEntity.class);
        Root<FixtureEntity> fixtures = q.from(FixtureEntity.class);

        if (param.getLastFixtureRef() != null) {
            Subquery<String> sq = q.subquery(String.class);
            Root<FixtureEntity> sfixture = sq.from(FixtureEntity.class);
            sq.select(sortKeyOf(sfixture));
            sq.where(b.equal(sfixture.get(FixtureEntity_.sourceRef), param.getLastFixtureRef()));

            Expression<String> v = sortKeyOf(fixtures);
            q.where(b.greaterThan(v, sq));
        }

        q.orderBy(b.asc(fixtures.get(FixtureEntity_.startTime)),
                  b.asc(fixtures.get(FixtureEntity_.tournament).get(TournamentEntity_.slug)),
                  b.asc(fixtures.get(FixtureEntity_.id)));

        List<FixtureEntity> res = em.createQuery(q)
                .setMaxResults(500)
                .getResultList();

        long t2 = System.currentTimeMillis();

        LOG.info("{}: next {} fixture(s) retrieved ({}ms)", param, res.size(), t2-t1);

        return res;
    }

    private Expression<String> sortKeyOf(Root<FixtureEntity> fixture) {
        CriteriaBuilder b = em.getCriteriaBuilder();

        Expression<String> v = fixture.get(FixtureEntity_.startTime).as(String.class);
        v = b.concat(v, "$");
        v = b.concat(v, fixture.get(FixtureEntity_.tournament).get(TournamentEntity_.slug));
        v = b.concat(v, "$");
        v = b.concat(v, fixture.get(FixtureEntity_.id).as(String.class));

        return v;
    }

    public List<FixtureEntity> getFixtures(FixturesQueryParam param) {
        long t1 = System.currentTimeMillis();

        boolean reverse = param.getReverseChrono() != null && param.getReverseChrono();

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<FixtureEntity> q = builder.createQuery(FixtureEntity.class);
        Root<FixtureEntity> fixtures = q.from(FixtureEntity.class);
        Join<FixtureEntity, TournamentEntity> tournament = fixtures.join(FixtureEntity_.tournament);

        Predicate a = builder.conjunction();
        List<Expression<Boolean>> wheres = a.getExpressions();

        wheres.add(builder.equal(fixtures.get(FixtureEntity_.status), ScheduleStatus.CONFIRMED));

        if (param.start != null)
            wheres.add(builder.greaterThanOrEqualTo(builder.coalesce(fixtures.get(FixtureEntity_.endTime), fixtures.get(FixtureEntity_.startTime)), param.start));
        else
            wheres.add(builder.isNotNull(fixtures.get(FixtureEntity_.startTime)));

        if (param.end != null)
            wheres.add(builder.lessThanOrEqualTo(builder.coalesce(fixtures.get(FixtureEntity_.startTime), fixtures.get(FixtureEntity_.endTime)), param.end));

        if (!param.seasonId.isEmpty()) {
            Join<TournamentEntity, SeasonEntity> season = tournament.join(TournamentEntity_.season);
            wheres.add(season.get(SeasonEntity_.id).in(param.seasonId));
        }
        if (!param.seriesId.isEmpty()) {
            Path<String> seriesSlug = tournament.join(TournamentEntity_.series).get(SeriesEntity_.slug);
            Predicate b = builder.disjunction();
            for (String ss : param.seriesId)
                b.getExpressions().add(builder.like(seriesSlug, ss + "%"));
            wheres.add(b);
        }
        if (!param.tournamentRef.isEmpty()) {
            Path<String> tournamentSlug = tournament.get(TournamentEntity_.slug);
            Predicate b = builder.disjunction();
            for (String ts : param.tournamentRef)
                b.getExpressions().add(builder.like(tournamentSlug, ts + "%"));
            wheres.add(b);
        }
        if (param.sourcePrefix != null) {
            Path<String> sourceRef = tournament.get(TournamentEntity_.sourceRef);
            wheres.add(builder.like(sourceRef, param.sourcePrefix + "%"));
        }

        
        if (param.getLastFixtureRef() != null) {
            Subquery<String> sq = q.subquery(String.class);
            Root<FixtureEntity> sfixture = sq.from(FixtureEntity.class);
            sq.select(sortKeyOf(sfixture));
            sq.where(builder.equal(sfixture.get(FixtureEntity_.sourceRef), param.getLastFixtureRef()));

            Expression<String> v = sortKeyOf(fixtures);
            wheres.add(reverse ?  builder.lessThan(v, sq) : builder.greaterThan(v, sq));
        }

      /*  {
            ListJoin<FixtureEntity, FixturePlayerEntity> player = fixtures.join(FixtureEntity_.players);
            Join<FixturePlayerEntity, PersonEntity> person = player.join(FixturePlayerEntity_.person);
            Path<String> personSlug = person.get(PersonEntity_.slug);
            builder.equal(personSlug, param....)
        }*/

        if (!param.clubRef.isEmpty() || !param.homeClubRef.isEmpty() || !param.awayClubRef.isEmpty() || param.hasGamesheet != null) {

            Join<FixtureEntity, FixtureSideEntity> homeSide = fixtures.join(FixtureEntity_.homeSide);
            Join<FixtureSideEntity, TeamEntity> homeTeam = homeSide.join(FixtureSideEntity_.team);
            Join<TeamEntity, ClubEntity> homeClub = homeTeam.join(TeamEntity_.club);

            Join<FixtureEntity, FixtureSideEntity> awaySide = fixtures.join(FixtureEntity_.awaySide);
            Join<FixtureSideEntity, TeamEntity> awayTeam = awaySide.join(FixtureSideEntity_.team);
            Join<TeamEntity, ClubEntity> awayClub = awayTeam.join(TeamEntity_.club);
            
            if (!param.clubRef.isEmpty() || !param.awayClubRef.isEmpty()) {
                Set<String> clubRefs = new HashSet<>();
                clubRefs.addAll(param.clubRef);
             // clubRefs.addAll(param.homeClubRef);
                clubRefs.addAll(param.awayClubRef);

                wheres.add(
                        builder.or(homeClub.get(ClubEntity_.slug).in(clubRefs),
                                   awayClub.get(ClubEntity_.slug).in(clubRefs)));
            }

            if (!param.homeClubRef.isEmpty() || !param.awayClubRef.isEmpty()) {
                Join<TournamentEntity, ClubEntity> tournamentArena = tournament.join(TournamentEntity_.arena, JoinType.LEFT);
                Join<FixtureEntity, ClubEntity> fixtureArena = fixtures.join(FixtureEntity_.arena, JoinType.LEFT);
                Expression<String> arenaRef = builder.<String>coalesce()
                        .value(fixtureArena.get(ClubEntity_.slug))
                        .value(tournamentArena.get(ClubEntity_.slug))
                        .value(homeClub.get(ClubEntity_.slug));

                if (!param.homeClubRef.isEmpty())
                    // only include fixtures on these clubs' own ice:
                    wheres.add(arenaRef.in(param.homeClubRef));

                if (!param.awayClubRef.isEmpty())
                    // only include fixtures on foreign ice:
                    wheres.add(arenaRef.in(param.awayClubRef).not());
            }

            if (param.hasGamesheet != null) {
                wheres.add(builder.equal(fixtures.get(FixtureEntity_.status), ScheduleStatus.CONFIRMED));

                // disregard un-scheduled or un-finished games when examining gamesheet status:
                Expression<Timestamp> now_ = builder.currentTimestamp();
                Path<Date> startTime = fixtures.get(FixtureEntity_.startTime);
                Predicate isPastStartTime = builder.greaterThan(now_, startTime);
                wheres.add(isPastStartTime);

                if (param.hasGamesheet) {
                    wheres.add(builder.equal(fixtures.get(FixtureEntity_.sheet), GamesheetStatus.READY));
                }
                else {
                    wheres.add(builder.equal(fixtures.get(FixtureEntity_.sheet), GamesheetStatus.MISSING));
                    reverse = !reverse;
                }
            }
        }
        q.where(a);

        q.orderBy(reverse ?  builder.desc(fixtures.get(FixtureEntity_.startTime)) : builder.asc(fixtures.get(FixtureEntity_.startTime)),
                  builder.asc(fixtures.get(FixtureEntity_.tournament).get(TournamentEntity_.slug)),
                  builder.asc(fixtures.get(FixtureEntity_.id)));

        TypedQuery<FixtureEntity> tq = em.createQuery(q);
        if (param.getMaxResults() != null)
            tq.setMaxResults(param.getMaxResults());
        List<FixtureEntity> res = tq.getResultList();

        long t2 = System.currentTimeMillis();

        LOG.info("{}: {} fixture(s) retrieved ({}ms)", param, res.size(), t2-t1);

        return res;

/*
        root.j
        query.select(root);
        query.where(
                builder.equal(root.get("slug"), slug),
                builder.equal(root.get("season").get("id"), seasonId));

        return em.createQuery(query).setMaxResults(1).getSingleResult();

        throw new RuntimeException("Not implemented");
*/
    }

    public FixtureEntity putGamesheet(Gamesheet gg) {

        FixtureEntity f = findFixtureBySourceRef(gg.getSourceRef());
        TournamentEntity t = f.getTournament();

        TeamEntity homeTeam = null;
        TeamEntity awayTeam = null;
        for (TeamDef td : gg.getTeams())
        {
            try {
                TeamEntity team = findOrCreateTeam(t, td.getClubRef(), td.getValue());
                if (td.getSide() == FixtureSideRole.HOME)
                    homeTeam = team;
                else if (td.getSide() == FixtureSideRole.AWAY)
                    awayTeam = team;
            }
            catch (Exception ex) {
                LOG.warn("# fixture {} no {} team - {}", f.getSourceRef(), td.getSide(), ex.getMessage());
            }
        }

        if (gg.getStartTime() != null && f.getStartTime() == null) {
            f.setStartTime(gg.getStartTime());
            Calendar d = Calendar.getInstance();
            d.setTime(f.getStartTime());
            int durationMinutes = t.getSeries().getFixtureDuration();
            d.add(Calendar.MINUTE, durationMinutes);
            f.setEndTime(d.getTime());
        }
        f.setStatus(ScheduleStatus.CONFIRMED);

        FixtureSideEntity homeSide = f.getHomeSide();
        if (homeSide == null && homeTeam != null) {
            homeSide = new FixtureSideEntity();
            homeSide.setTeam(homeTeam);
            homeSide.setPlayers(new LinkedList<>());
            em.persist(homeSide);
            f.setHomeSide(homeSide);
        }
        FixtureSideEntity awaySide = f.getAwaySide();
        if (awaySide == null && awayTeam != null) {
            awaySide = new FixtureSideEntity();
            awaySide.setTeam(awayTeam);
            awaySide.setPlayers(new LinkedList<>());
            em.persist(awaySide);
            f.setAwaySide(awaySide);
        }

        LOG.info("updating fixture {}", f);

        for (Roster r : gg.getRosters())
            updateFixtureRoster(f, r.getSide(), r.getPlayers());

        updateFixtureEvents(f, gg.getEvents());

        for (Roster r : gg.getRosters())
            trimFixtureRoster(f, r.getSide(), r.getPlayers());
        
        f.setSheet(GamesheetStatus.READY);

        return f;
    }

    private void updateFixtureEvents(FixtureEntity f, FixtureEvents events) {
        Map<String, FixtureEventEntity> all = new HashMap<>();
        for (FixtureEventEntity ee : f.getEvents())
            all.put(ee.signature(), ee);

        int homeGoals = 0, awayGoals = 0, totalEvents = 0;
        for (Event e : events.getGoalsAndPenalties())
            try
            {
                Integer jerseyNumber = e.getPlayer().getNumber();
                String timeCode = e.getTime();
                FixtureSideEntity partSide = f.getSide(e.getSide());
                FixturePlayerEntity fpe = partSide.getPlayerByJerseyNumber(jerseyNumber);
                if (fpe == null) {
                    fpe = new FixturePlayerEntity();
                    fpe.setJerseyNumber(jerseyNumber);
                    fpe.setSide(partSide);
                    fpe.setFixture(f);
                    LOG.info("{} attaching 'teamOffender'", e);
                    if (jerseyNumber == 9001) {
                        fpe.setPerson(teamOffender());
                        fpe.setPersonRole("TEAM");
                    }
                    else {
                        fpe.setPerson(unknownPlayer());
                        fpe.setPersonRole("PLAYER");
                        LOG.warn("player #{} not found in {} - attributing {} event to The Unknown Citizen", jerseyNumber, partSide.getTeam().getName(), e.getTime());
                    }
                    em.persist(fpe);
                    partSide.getPlayers().add(fpe);
                }
                if (e instanceof GoalEvent) {
                    GoalEvent ge = (GoalEvent)e;

                    GoalEventEntity ee = new GoalEventEntity();
                    ee.setFixture(f);
                    ee.setSide(e.getSide());
                    ee.setPlayer(fpe);
                    ee.setGameSituation(ge.getGameSituation());
                    if (ee.getGameSituation() == null)
                        ee.setGameSituation(GameSituation.EQ);
                    if (!StringUtils.isBlank(timeCode))
                        ee.setGameTimeSecond(GameTime.parse(timeCode));
                    else if (GameSituation.GWS.equals(ge.getGameSituation()))
                        ee.setGameTimeSecond(f.getTournament().getGwsTimeSecond());
                    ee.setAssists(new LinkedList<>());
                    for (PlayerRef a : ge.getAssists()) {
                        FixturePlayerEntity ape = partSide.getPlayerByJerseyNumber(a.getNumber());
                        ee.getAssists().add(ape);
                    }

                    if (all.remove(ee.signature()) == null) {
                        em.persist(ee);
                        LOG.info("{} created", ee.signature());
                    }
                    else {
                        LOG.debug("{} seen", ee.signature());
                    }

                    if (ee.isHome())
                        ++homeGoals;
                    else
                        ++awayGoals;
                }
                else if (e instanceof PenaltyEvent) {
                    PenaltyEvent pe = (PenaltyEvent)e;

                    PenaltyEventEntity ee = new PenaltyEventEntity();
                    ee.setFixture(f);
                    ee.setSide(e.getSide());
                    ee.setPlayer(fpe);
                    ee.setGameTimeSecond(GameTime.parse(timeCode));
                    ee.setOffense(pe.getOffense());
                    ee.setPenaltyMinutes(pe.getMinutes());
                    String stx = pe.getStartTime();
                    try {
                        ee.setStarttimeSecond(GameTime.parse(stx));
                    }
                    catch (IllegalArgumentException ex) {
                        ee.setStarttimeSecond(ee.getGameTimeSecond());
                    }
                    String etx = pe.getEndTime();
                    try {
                        ee.setEndtimeSecond(GameTime.parse(etx));
                    }
                    catch (IllegalArgumentException ex) {
                        ee.setEndtimeSecond(ee.getStarttimeSecond() + 60 * ee.getPenaltyMinutes());
                    }
                    if (ee.getOffense() == null)
                        throw new IllegalArgumentException("unrecognized offense: " + ee.toString());

                    if (all.remove(ee.signature()) == null) {
                        em.persist(ee);
                        LOG.info("{} created", ee.signature());
                    }
                    else {
                        LOG.debug("{} seen", ee.signature());
                    }
                }
                else {
                    LOG.warn("WTF? {}", e.getClass().getSimpleName());
                }
                ++totalEvents;
            }
        catch (Exception ex) {
            LOG.error("{} ignored", e.getClass().getSimpleName(), ex);
        }

        if (totalEvents == 0) {
            LOG.warn("{}: no game events in gamesheet", f.getSourceRef());
        }
        else {
            if (f.getHomeSide().getScore() == null)
                f.getHomeSide().setScore(homeGoals);
            if (f.getAwaySide().getScore() == null)
                f.getAwaySide().setScore(awayGoals);

            if (f.getHomeSide().getScore() == homeGoals && f.getAwaySide().getScore() == awayGoals) {
                LOG.debug("{}: game score unchanged: ({}-{})", f.getSourceRef(),  homeGoals, awayGoals);
            }
            else if (homeGoals == 0 && awayGoals == 0) {
                LOG.info("{}: no goal events in gamesheet. score not modified: ({}-{})", f.getSourceRef(), f.getHomeSide().getScore(), f.getAwaySide().getScore());
            }
            else {
                LOG.warn("{}: game score ({}-{}) updated from gamesheet: ({}-{})", f.getSourceRef(), f.getHomeSide().getScore(), f.getAwaySide().getScore(), homeGoals, awayGoals);
                f.getHomeSide().setScore(homeGoals);
                f.getAwaySide().setScore(awayGoals);
            }
        }


        for (FixtureEventEntity ee : all.values()) {
            LOG.info("{} gone", ee.signature());
            em.remove(ee);
        }
    }

    private void trimFixtureRoster(FixtureEntity fixture, FixtureSideRole role, List<Player> players) {
    }

    private void updateFixtureRoster(FixtureEntity fixture, FixtureSideRole role, List<Player> players) {
        FixtureSideEntity side = fixture.getSide(role);
        LOG.debug("===== {} ROSTER ======", side.getTeam().getName());

        Map<String, FixturePlayerEntity> all = new HashMap<>();
        for (FixturePlayerEntity fp : side.getPlayers()) {
            LOG.debug("already seen {}", fp.getRef());
            if (fp.getPerson().isTUC())
                continue;
            FixturePlayerEntity dupe = all.put(fp.getRef(), fp);
            if (dupe != null) {
                LOG.debug("duplicate " + dupe.getRef() + " removed!");
                em.remove(dupe);
            }
        }

        Set<Integer> jerseys = new HashSet<>();
        for (Player p : players) {
            if (p.getNumber() == null) {
                LOG.info("{} no jersey number - ignored", p);
                continue;
            }
            else if (!jerseys.add(p.getNumber())) {
                LOG.info("{} duplicate jersey #{} - ignored", p, p.getNumber());
                continue;
            }

            String playerRef = side.getTeam().getClub().getSlug() + ":" + p.getNumber() + ":" + String.valueOf(p.getLastName()).toLowerCase() + ":" + String.valueOf(p.getFirstNames()).toLowerCase();
            FixturePlayerEntity fp = all.remove(playerRef);
            if (fp == null) {
                fp = domain2entity(p, fixture, role);
                fp.setLineupLine(p.getLine());
                fp.setLineupPosition(p.getPos());
                em.persist(fp);
                side.getPlayers().add(fp);
             // nf.add(f);
                LOG.info("{} created", fp);
            }
            else {
                fp.setLineupLine(p.getLine());
                fp.setLineupPosition(p.getPos());
                LOG.debug("{} reused", fp);
            }
        }

        for (FixturePlayerEntity unused : all.values()) {
            if (unused.getJerseyNumber() == 9001)
                continue;
            for (FixtureEventEntity e : fixture.getEvents())
                if (e.usesPlayer(unused)) {
                    LOG.info("  #        {} removed!", e.signature());
                    em.remove(e);
                }
            LOG.info("# unused {} removed!", unused.getRef());
            em.remove(unused);
        }
    }

    private void xx(Predicate w, Path<String> path, String text) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        if (text == null)
            w.getExpressions().add(builder.isNull(path));
        else
            w.getExpressions().add(builder.equal(builder.upper(path), text.toUpperCase()));

    }

    private PersonEntity findPlayerPerson(Player p) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<PersonEntity> query = builder.createQuery(PersonEntity.class);
        Root<PersonEntity> root = query.from(PersonEntity.class);
        query.select(root);

        Predicate w = builder.conjunction();
        xx(w, root.get(PersonEntity_.lastName), p.getLastName());
        xx(w, root.get(PersonEntity_.firstNames), p.getFirstNames());
        query.where(w);
        return em.createQuery(query).setMaxResults(1).getSingleResult();
    }


    private final static Player UNKNOWN_PLAYER = new Player() {{
        setFirstNames("(ukendt)");
        setLastName("(ukendt)");
    }};

    private PersonEntity unknownPlayer() {
        return findOrCreatePlayerPerson(UNKNOWN_PLAYER);
    }

    private final static Player TEAM_OFFENDER = new Player() {{
        setFirstNames("(holdstraf)");
        setLastName("");
    }};

    private PersonEntity teamOffender() {
        PersonEntity person = findOrCreatePlayerPerson(TEAM_OFFENDER);
        LOG.info("team offender: {}", person);
        return person;
    }

    private PersonEntity findOrCreatePlayerPerson(Player p) {
        try {
            return findPlayerPerson(p);
        }
        catch (NoResultException ex) {
            PersonEntity pe = new PersonEntity();
            pe.setFirstNames(p.getFirstNames());
            pe.setLastName(p.getLastName());
            em.persist(pe);
            LOG.info("{} created", pe);
            return pe;
        }
    }

    private FixturePlayerEntity domain2entity(Player p, FixtureEntity f, FixtureSideRole r) {
        PersonEntity pe = findOrCreatePlayerPerson(p);
        FixturePlayerEntity fp = new FixturePlayerEntity();
        fp.setFixture(f);
        fp.setSide(f.getSide(r));
        fp.setPerson(pe);
        fp.setJerseyNumber(p.getNumber());
        fp.setPersonRole("PLAYER");
        return fp;
    }
}
