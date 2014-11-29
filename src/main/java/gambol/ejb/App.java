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
import gambol.model.TournamentTeamEntity;
import gambol.model.TournamentTeamEntity_;
import gambol.xml.Event;
import gambol.xml.Fixture;
import gambol.xml.FixtureSideRole;
import gambol.xml.Gamesheet;
import gambol.xml.GoalEvent;
import gambol.xml.PenaltyEvent;
import gambol.xml.Player;
import gambol.xml.Roster;
import gambol.xml.ScheduleStatus;
import gambol.xml.Side;
import gambol.xml.Tournament;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
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
            LOG.log(Level.INFO, "{0} created", res);
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
        SeasonEntity season = findOrCreateSeason(seasonId);

        String slug = tt.getSlug();
        String name = tt.getTitle();
        if (name == null)
            throw new IllegalArgumentException("Invalid tournament name");
        if (slug == null)
            slug = name.toLowerCase().replaceAll("[-_,. ]+", "-");

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

        return entity;
    }

    public TournamentEntity getTournament(String seasonId, String slug) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<TournamentEntity> query = builder.createQuery(TournamentEntity.class);
        Root<TournamentEntity> root = query.from(TournamentEntity.class);
        query.select(root);
        query.where(
                builder.equal(root.get("slug"), slug),
                builder.equal(root.get("season").get("id"), seasonId));
        return em.createQuery(query).setMaxResults(1).getSingleResult();
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
        if (!getSeasons().isEmpty())
            return;

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

        Map<String, FixtureEntity> all = new HashMap<String, FixtureEntity>();
        for (FixtureEntity f : t.getFixtures())
            all.put(f.getSourceRef(), f);

        List<FixtureEntity> nf = new LinkedList<FixtureEntity>();
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

    /*
        if (entity.getStartTime() != null && entity.getEndTime() == null) {
            Calendar d = Calendar.getInstance();
            d.setTime(entity.getStartTime());
            int durationMinutes = entity.getTournament().getSeries().getFixtureDuration();
            d.add(Calendar.MINUTE, durationMinutes);
            entity.setEndTime(d.getTime());
        }
    */
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

    public FixtureEntity getFixture(String sourceRef) {
        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<FixtureEntity> q = builder.createQuery(FixtureEntity.class);
        Root<FixtureEntity> fixtures = q.from(FixtureEntity.class);
        q.where(builder.like(fixtures.get(FixtureEntity_.sourceRef), sourceRef + "%"));

        return em.createQuery(q).getSingleResult();
    }

    public List<FixtureEntity> getFixtures(FixturesQueryParam param) {
        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<FixtureEntity> q = builder.createQuery(FixtureEntity.class);
        Root<FixtureEntity> fixtures = q.from(FixtureEntity.class);

        Predicate a = builder.conjunction();
        a.getExpressions().add(builder.equal(fixtures.get(FixtureEntity_.status), ScheduleStatus.CONFIRMED));

        if (param.start != null)
            a.getExpressions().add(builder.greaterThanOrEqualTo(builder.coalesce(fixtures.get(FixtureEntity_.endTime), fixtures.get(FixtureEntity_.startTime)), param.start));
        if (param.end != null)
            a.getExpressions().add(builder.lessThanOrEqualTo(builder.coalesce(fixtures.get(FixtureEntity_.startTime), fixtures.get(FixtureEntity_.endTime)), param.end));

        if (!param.seasonId.isEmpty() || !param.seriesId.isEmpty() || !param.tournamentRef.isEmpty()) {
            Join<FixtureEntity, TournamentEntity> tournament = fixtures.join(FixtureEntity_.tournament);
            if (!param.seasonId.isEmpty()) {
                Join<TournamentEntity, SeasonEntity> season = tournament.join(TournamentEntity_.season);
                a.getExpressions().add(season.get(SeasonEntity_.id).in(param.seasonId));
            }
            if (!param.seriesId.isEmpty()) {
                Path<String> seriesSlug = tournament.join(TournamentEntity_.series).get(SeriesEntity_.slug);
                Predicate b = builder.disjunction();
                for (String ss : param.seriesId)
                    b.getExpressions().add(builder.like(seriesSlug, ss + "%"));
                a.getExpressions().add(b);
            }
            if (!param.tournamentRef.isEmpty()) {
                Path<String> tournamentSlug = tournament.get(TournamentEntity_.slug);
                Predicate b = builder.disjunction();
                for (String ts : param.tournamentRef)
                    b.getExpressions().add(builder.like(tournamentSlug, ts + "%"));
                a.getExpressions().add(b);
            }
        }
        if (!param.clubRef.isEmpty() || !param.homeClubRef.isEmpty() || !param.awayClubRef.isEmpty()) {
            Join<FixtureEntity, FixtureSideEntity> homeSide = fixtures.join(FixtureEntity_.homeSide);
            Join<FixtureSideEntity, TournamentTeamEntity> homeTeam = homeSide.join(FixtureSideEntity_.team);
            Join<TournamentTeamEntity, ClubEntity> homeClub = homeTeam.join(TournamentTeamEntity_.club);

            Join<FixtureEntity, FixtureSideEntity> awaySide = fixtures.join(FixtureEntity_.awaySide);
            Join<FixtureSideEntity, TournamentTeamEntity> awayTeam = awaySide.join(FixtureSideEntity_.team);
            Join<TournamentTeamEntity, ClubEntity> awayClub = awayTeam.join(TournamentTeamEntity_.club);

            if (!param.clubRef.isEmpty())
                a.getExpressions().add(
                        builder.or(homeClub.get(ClubEntity_.slug).in(param.clubRef),
                                   awayClub.get(ClubEntity_.slug).in(param.clubRef)));
            if (!param.homeClubRef.isEmpty()) {
                // List all home-games for the specified club:
                a.getExpressions().add(homeClub.get(ClubEntity_.slug).in(param.homeClubRef));
            }
            if (!param.awayClubRef.isEmpty()) {
                // List all away-games for the specified club:
                a.getExpressions().add(awayClub.get(ClubEntity_.slug).in(param.awayClubRef));
                // ...but skip those against (other teams) in the same club, if any. This
                // is to avoid the same matches getting listed twice, if a club tournament
                // schedule is split between home games and away games, e.g. in separate
                // calendar colors:
                a.getExpressions().add(homeClub.get(ClubEntity_.slug).in(param.awayClubRef).not());
            }
        }
        q.where(a);
        q.orderBy(builder.asc(fixtures.get(FixtureEntity_.startTime)));

        List<FixtureEntity> res = em.createQuery(q).getResultList();

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
        String sourceRef = gg.getSourceRef();

        FixtureEntity f = getFixture(sourceRef);

        for (Roster r : gg.getRosters()) {
            FixtureSideRole side = r.getSide();
            FixtureSideEntity ss = f.getSide(side);
            LOG.info("### ===== " + ss.getTeam().getName() + " ROSTER ======");
            updateFixturePlayers(ss, r.getPlayers());
        }

        LOG.info("### ===== EVENTS ======");
        updateFixtureEvents(f, gg.getEvents());

        return f;
    }

    private final static Pattern MMMM_SS = Pattern.compile("(\\d+):(\\d+)");

    private static Integer gameTimeSecond(String mmmm_ss) {
        Matcher m = MMMM_SS.matcher(mmmm_ss);
        if (!m.matches())
            throw new IllegalArgumentException(mmmm_ss);
        int mm = Integer.parseInt(m.group(1));
        int ss = Integer.parseInt(m.group(2));
        return mm * 60 + ss;
    }

    private void updateFixtureEvents(FixtureEntity f, Gamesheet.Events events) {
        /*
         * The simple-naive strategy: remove all existing events, then add:
         */
        for (FixtureEventEntity e : f.getHomeSide().getEvents()) {
            LOG.info("### " + e + " gone");
            em.remove(e);
        }
        for (FixtureEventEntity e : f.getAwaySide().getEvents()) {
            LOG.info("### " + e + " gone");
            em.remove(e);
        }
        
        for (Event e : events.getGoalsAndPenalties()) {
            Integer playerNumber = e.getPlayer().getNumber();
            String timeCode = e.getTime();
            FixtureSideEntity partSide = f.getSide(e.getSide());
            FixturePlayerEntity fpe = partSide.getPlayerByJerseyNumber(e.getPlayer().getNumber());
            if (e instanceof GoalEvent) {
                GoalEvent ge = (GoalEvent)e;

                GoalEventEntity ee = new GoalEventEntity();
                ee.setSide(partSide);
                ee.setPlayer(fpe);
                ee.setGameTimeSecond(gameTimeSecond(timeCode));
                ee.setGameSituation(ge.getGameSituation());
                em.persist(ee);
                
                LOG.info("### " + ee + " created");
            }
            else if (e instanceof PenaltyEvent) {
                PenaltyEvent pe = (PenaltyEvent)e;
                
                PenaltyEventEntity ee = new PenaltyEventEntity();
                ee.setSide(partSide);
                ee.setPlayer(fpe);
                ee.setGameTimeSecond(gameTimeSecond(timeCode));
                ee.setOffense(pe.getOffense());
                ee.setEndtimeSecond(gameTimeSecond(pe.getEndTime()));
                ee.setPenaltyMinutes(pe.getMinutes());
                
                em.persist(ee);
                
                LOG.info("### " + ee + " created");
            }
        }
    }

    private void updateFixturePlayers(FixtureSideEntity ss, List<Player> players) {
        Map<String, FixturePlayerEntity> all = new HashMap<String, FixturePlayerEntity>();
        for (FixturePlayerEntity f : ss.getPlayers()) {
            LOG.info("### already seen " + f.getRef());
            FixturePlayerEntity dupe = all.put(f.getRef(), f);
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

            String playerRef = ss.getTeam().getClub().getSlug() + ":" + p.getNumber() + ":" + String.valueOf(p.getLastName()).toLowerCase() + ":" + String.valueOf(p.getFirstNames()).toLowerCase();
            FixturePlayerEntity fp = all.remove(playerRef);
            if (fp == null) {
                fp = domain2entity(p, ss);
                fp.setLineupLine(p.getLine());
                fp.setLineupPosition(p.getPos());
                em.persist(fp);
                ss.getPlayers().add(fp);
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

    private FixturePlayerEntity domain2entity(Player p, FixtureSideEntity ss) {
        PersonEntity pe = findOrCreatePlayerPerson(p);
        FixturePlayerEntity fp = new FixturePlayerEntity();
        fp.setSide(ss);
        fp.setPerson(pe);
        fp.setJerseyNumber(p.getNumber());
        return fp;
    }
}
