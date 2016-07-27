package gambol.ejb;

import gambol.model.ClubEntity;
import gambol.model.ClubEntity_;
import gambol.model.FixtureEntity;
import gambol.model.FixtureEntity_;
import gambol.model.FixtureEventEntity;
import gambol.model.FixturePlayerEntity;
import gambol.model.FixtureSideEntity;
import gambol.model.FixtureSideEntity_;
import gambol.model.GoalEventEntity;
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
import gambol.xml.Event;
import gambol.xml.Fixture;
import gambol.xml.FixtureEvents;
import gambol.xml.FixtureSideRole;
import gambol.xml.Gamesheet;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang.StringUtils;
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
        // OMG!
        CriteriaQuery<ClubEntity> cq = em.getCriteriaBuilder().createQuery(ClubEntity.class);
        CriteriaQuery<ClubEntity> all = cq.select(cq.from(ClubEntity.class)); //.orderBy(Ord);
        TypedQuery<ClubEntity> allQuery = em.createQuery(all);
        List<ClubEntity> clubs = allQuery.getResultList();

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
        LOG.info("#### updating tournament "+slug+" \""+name+"\"");

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
        } catch (NoResultException ex) {
            TeamEntity res = new TeamEntity();
            res.setTournament(tournament);
            ClubEntity club = findOrCreateClub(clubRef);
            res.setClub(club);
            res.setName(teamName);
            res.setSlug(club.getSlug());
            em.persist(res);
            return res;
        }
    }

    private static String asSlug(String name, int maxLength) {
        String slug = name
                .replaceAll("[-_/,. ]+", "-")
                .replaceAll("[-\\p{IsAlphabetic}\\p{IsDigit}]+", "")
                .toLowerCase();
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
        entity.setCountryIso2(c.getCountryIso2());
    }

    public void updateFixtures(TournamentEntity t, List<Fixture> fixtures) {
        // Uh-oh, this thing leaves orphaned FixtureSideEntity rows behind.

        Map<String, FixtureEntity> all = new HashMap<>();
        for (FixtureEntity f : t.getFixtures())
            all.put(f.getSourceRef(), f);

        List<FixtureEntity> nf = new LinkedList<>();
        for (Fixture fo : fixtures) {
            FixtureEntity f = all.remove(fo.getSourceRef());
            if (f == null) {
                // new fixture
                f = new FixtureEntity();
                f.setTournament(t);
                domain2entity(fo, f);
                em.persist(f);
                nf.add(f);
                LOG.info(fo.getSourceRef() + " not found: new fixture created: " + f);
            } else {
                // existing fixture, update:
                f.setTournament(t);
                domain2entity(fo, f);
                LOG.info(fo.getSourceRef() + ": fixture updated: " + f);
            }
        }

        for (FixtureEntity f : all.values()) {
            em.remove(f);
            LOG.info(f.getSourceRef() + ": history");
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

        CriteriaQuery<FixtureEntity> q = builder.createQuery(FixtureEntity.class);
        Root<FixtureEntity> fixtures = q.from(FixtureEntity.class);
        q.where(builder.like(fixtures.get(FixtureEntity_.sourceRef), sourceRef + "%"));

        return em.createQuery(q).getSingleResult();
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
    
    public List<FixtureEntity> getFixtures(FixturesQueryParam param) {
        long t1 = System.currentTimeMillis();
        
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

        if (!param.seasonId.isEmpty() || !param.seriesId.isEmpty() || !param.tournamentRef.isEmpty()) {
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
        }
        
        if (!param.clubRef.isEmpty() || !param.homeClubRef.isEmpty() || !param.awayClubRef.isEmpty() || param.hasGamesheet != null) {
            
            Join<FixtureEntity, FixtureSideEntity> homeSide = fixtures.join(FixtureEntity_.homeSide);
            Join<FixtureSideEntity, TeamEntity> homeTeam = homeSide.join(FixtureSideEntity_.team);
            Join<TeamEntity, ClubEntity> homeClub = homeTeam.join(TeamEntity_.club);

            Join<FixtureEntity, FixtureSideEntity> awaySide = fixtures.join(FixtureEntity_.awaySide);
            Join<FixtureSideEntity, TeamEntity> awayTeam = awaySide.join(FixtureSideEntity_.team);
            Join<TeamEntity, ClubEntity> awayClub = awayTeam.join(TeamEntity_.club);

            if (!param.clubRef.isEmpty() || !param.awayClubRef.isEmpty()) {
                Set<String> clubRefs = new HashSet<String>();
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
                // disregard un-scheduled or un-finished games when examining gamesheet status:
                Expression<Timestamp> now_ = builder.currentTimestamp();
                wheres.add(builder.equal(fixtures.get(FixtureEntity_.status), ScheduleStatus.CONFIRMED));
                wheres.add(builder.greaterThan(now_, fixtures.get(FixtureEntity_.endTime)));

                // so far, just check that both sides have players registed:
                Predicate gs = builder.and(
                    builder.isNotEmpty(homeSide.get(FixtureSideEntity_.players)),
                    builder.isNotEmpty(awaySide.get(FixtureSideEntity_.players)));
                
                if (!param.hasGamesheet)
                    gs = gs.not();
                wheres.add(gs);
            }
                
        }
        q.where(a);
        q.orderBy(builder.asc(fixtures.get(FixtureEntity_.startTime)));

        List<FixtureEntity> res = em.createQuery(q).getResultList();

        long t2 = System.currentTimeMillis();
        
        LOG.info(param + ": " + res.size() + " fixture(s) retrieved ("+(t2-t1)+"ms)");
        
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
            TeamEntity team = findOrCreateTeam(t, td.getClubRef(), td.getValue());
            if (td.getSide() == FixtureSideRole.HOME)
                homeTeam = team;
            else if (td.getSide() == FixtureSideRole.AWAY)
                awayTeam = team;
        }
        
        f.setStartTime(gg.getStartTime());
        if (f.getStartTime() != null && f.getEndTime() == null) {
            Calendar d = Calendar.getInstance();
            d.setTime(f.getStartTime());
            int durationMinutes = t.getSeries().getFixtureDuration();
            d.add(Calendar.MINUTE, durationMinutes);
            f.setEndTime(d.getTime());
        }
        f.setStatus(ScheduleStatus.CONFIRMED);
        
        FixtureSideEntity homeSide = f.getHomeSide();
        if (homeSide == null) {
            homeSide = new FixtureSideEntity();
            homeSide.setTeam(homeTeam);
            homeSide.setPlayers(new LinkedList<>());
            em.persist(homeSide);
            f.setHomeSide(homeSide);
        }
        FixtureSideEntity awaySide = f.getAwaySide();
        if (awaySide == null) {
            awaySide = new FixtureSideEntity();
            awaySide.setTeam(awayTeam);
            awaySide.setPlayers(new LinkedList<>());
            em.persist(awaySide);
            f.setAwaySide(awaySide);
        }

        LOG.info("#### updating fixture "+f);
        
        for (Roster r : gg.getRosters()) {
            FixtureSideRole side = r.getSide();
            LOG.info("     " + side + " roster ======");
            updateFixtureSidePlayers(f, side, r.getPlayers());
        }
        
        updateFixtureEvents(f, gg.getEvents());
        
        return f;
    }

    private final static Pattern MMMM_SS = Pattern.compile("(\\d+):(\\d+)");

    public static Integer gameTimeSecond(String mmmm_ss) {
        Matcher m = MMMM_SS.matcher(mmmm_ss);
        if (!m.matches())
            throw new IllegalArgumentException(mmmm_ss);
        int mm = Integer.parseInt(m.group(1));
        int ss = Integer.parseInt(m.group(2));
        return mm * 60 + ss;
    }

    public static String gameTimeCode(Integer gameSecond) {
        return String.format("%d:%02d", gameSecond / 60, gameSecond % 60);
    }

    private void updateFixtureEvents(FixtureEntity f, FixtureEvents events) {
        /*
         * The simple-naive strategy: remove all existing events, then add:
         */
        for (FixtureEventEntity e : f.getEvents()) {
            LOG.info("### " + e + " gone");
            em.remove(e);
        }
        
        for (Event e : events.getGoalsAndPenalties()) {
            Integer jerseyNumber = e.getPlayer().getNumber();
            String timeCode = e.getTime();
            FixtureSideEntity partSide = f.getSide(e.getSide());
            FixturePlayerEntity fpe = partSide.getPlayerByJerseyNumber(jerseyNumber);
            if (e instanceof GoalEvent) {
                GoalEvent ge = (GoalEvent)e;

                GoalEventEntity ee = new GoalEventEntity();
                ee.setFixture(f);
                ee.setSide(e.getSide());
                ee.setPlayer(fpe);
                ee.setGameTimeSecond(gameTimeSecond(timeCode));
                ee.setGameSituation(ge.getGameSituation());
                
                ee.setAssists(new LinkedList<>());
                for (PlayerRef a : ge.getAssists()) {
                    FixturePlayerEntity ape = partSide.getPlayerByJerseyNumber(a.getNumber());                    
                    ee.getAssists().add(ape);
                }

                em.persist(ee);                
                LOG.info("### " + ee + " created");
            }
            else if (e instanceof PenaltyEvent) {
                PenaltyEvent pe = (PenaltyEvent)e;
                
                PenaltyEventEntity ee = new PenaltyEventEntity();
                ee.setFixture(f);
                ee.setSide(e.getSide());
                ee.setPlayer(fpe);
                ee.setGameTimeSecond(gameTimeSecond(timeCode));
                ee.setOffense(pe.getOffense());
                ee.setPenaltyMinutes(pe.getMinutes());
                String etx = pe.getEndTime();
                if (!StringUtils.isEmpty(etx))
                    ee.setEndtimeSecond(gameTimeSecond(etx));
                else
                    ee.setEndtimeSecond(ee.getGameTimeSecond() + 60 * ee.getPenaltyMinutes());
                
                em.persist(ee);
                
                LOG.info("### " + ee + " created");
            }
        }
    }

    private void updateFixtureSidePlayers(FixtureEntity fixture, FixtureSideRole role, List<Player> players) {
        FixtureSideEntity side = fixture.getSide(role);
        LOG.info("### ===== " + side.getTeam().getName() + " ROSTER ======");
        
        Map<String, FixturePlayerEntity> all = new HashMap<>();
        for (FixturePlayerEntity fp : side.getPlayers()) {
            LOG.info("### already seen " + fp.getRef());
            FixturePlayerEntity dupe = all.put(fp.getRef(), fp);
            if (dupe != null) {
                LOG.info("### duplicate " + dupe.getRef() + " removed!");
                em.remove(dupe);
            }
        }

        for (Player p : players) {
            if (p.getNumber() == null) {
                LOG.info("### "+p+" ignored");
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
                LOG.info(fp + " created");
            }
            else {
                fp.setLineupLine(p.getLine());
                fp.setLineupPosition(p.getPos());
                LOG.info(fp + " reused");
            }
        }

        for (FixturePlayerEntity unused : all.values()) {
            LOG.info("### unused " + unused.getRef() + " removed!");
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


    private PersonEntity findOrCreatePlayerPerson(Player p) {
        try {
            return findPlayerPerson(p);
        }
        catch (NoResultException ex) {
            PersonEntity pe = new PersonEntity();
            pe.setFirstNames(p.getFirstNames());
            pe.setLastName(p.getLastName());
            em.persist(pe);
            LOG.info(pe + " created");
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
        return fp;
    }
}
