package gambol.api;

import gambol.ejb.App;
import gambol.model.ClubEntity;
import gambol.model.FixtureEntity;
import gambol.model.FixtureEventEntity;
import gambol.model.FixturePlayerEntity;
import gambol.model.FixtureSideEntity;
import gambol.model.GoalEventEntity;
import gambol.model.PenaltyEventEntity;
import gambol.model.SeasonEntity;
import gambol.model.TournamentEntity;
import gambol.model.TeamEntity;
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
import gambol.xml.Side;
import gambol.xml.TeamDef;
import gambol.xml.Tournament;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import static javax.ws.rs.core.MediaType.*;
import javax.ws.rs.core.UriInfo;

/**
 * @author osa
 */
@Stateful
@Path("fixture/{fixtureId}")
public class FixtureResource {

    @Inject
    private Logger LOG;

    @EJB
    App gambol;

    @PathParam("fixtureId")
    private long fixtureId;

    @GET
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Gamesheet getGamesheet(@Context UriInfo uriInfo) {

        FixtureEntity f = gambol.getFixtureById(fixtureId);
        
        LOG.info("# fixture "+fixtureId+" loaded: " + f);
        
        return entity2gamesheet(f, uriInfo);
    }

    public static Gamesheet entity2gamesheet(FixtureEntity f, UriInfo uriInfo) {
        Gamesheet sheet = new Gamesheet();

        Roster homeR = new Roster();
        homeR.setSide(FixtureSideRole.HOME);
        for (FixturePlayerEntity p : f.getHomeSide().getPlayers())
            homeR.getPlayers().add(pentity2domain(p));
        sheet.getRosters().add(homeR);
        TeamDef homeT = new TeamDef();
        homeT.setSide(FixtureSideRole.HOME);
        TeamEntity ht = f.getHomeSide().getTeam();
        homeT.setClubRef(ht.getClub().getSlug());
        homeT.setValue(ht.getName());
        sheet.getTeams().add(homeT);

        Roster awayR = new Roster();
        awayR.setSide(FixtureSideRole.AWAY);
        for (FixturePlayerEntity p : f.getAwaySide().getPlayers())
            awayR.getPlayers().add(pentity2domain(p));
        sheet.getRosters().add(awayR);
        TeamDef awayT = new TeamDef();
        awayT.setSide(FixtureSideRole.AWAY);
        TeamEntity wt = f.getAwaySide().getTeam();
        awayT.setClubRef(wt.getClub().getSlug());
        awayT.setValue(wt.getName());
        sheet.getTeams().add(awayT);

        FixtureEvents fe = new FixtureEvents();
        for (FixtureEventEntity e : f.getEvents())
            fe.getGoalsAndPenalties().add(eentity2domain(e));

        sheet.setEvents(fe);
        sheet.setSourceRef(f.getSourceRef());
        sheet.setMatchNumber(f.getMatchNumber());
        sheet.setTournament(tentity2domain(f.getTournament()));
        sheet.setStartTime(f.getStartTime());

        return sheet;
    }

    private static Event eentity2domain(FixtureEventEntity e) {
        if (e instanceof GoalEventEntity) {
            GoalEventEntity gee = (GoalEventEntity)e;
            GoalEvent ge = new GoalEvent();
            PlayerRef pr = new PlayerRef();
            pr.setNumber(gee.getPlayer().getJerseyNumber());
            ge.setPlayer(pr);
            ge.setSide(e.getSide());
            ge.setTime(App.gameTimeCode(e.getGameTimeSecond()));
            
            for (FixturePlayerEntity as : gee.getAssists()) {
                PlayerRef ar = new PlayerRef();
                ar.setNumber(as.getJerseyNumber());
                ge.getAssists().add(ar);
            }            
            ge.setGameSituation(gee.getGameSituation());

            return ge;
        }
        if (e instanceof PenaltyEventEntity) {
            PenaltyEventEntity pee = (PenaltyEventEntity)e;
            PenaltyEvent pe = new PenaltyEvent();
            PlayerRef pr = new PlayerRef();
            pr.setNumber(pee.getPlayer().getJerseyNumber());
            pe.setPlayer(pr);
            pe.setSide(e.getSide());
            pe.setTime(App.gameTimeCode(e.getGameTimeSecond()));
            
            pe.setOffense(pee.getOffense());
            pe.setMinutes(pee.getPenaltyMinutes());
            pe.setStartTime(App.gameTimeCode(pee.getStarttimeSecond()));
            pe.setEndTime(App.gameTimeCode(pee.getEndtimeSecond()));
            
            return pe;
        }
        return null;
    }

    private static Player pentity2domain(FixturePlayerEntity p) {
        Player pp = new Player();
        pp.setFirstNames(p.getPerson().getFirstNames());
        pp.setLastName(p.getPerson().getLastName());
        pp.setNumber(p.getJerseyNumber());
        pp.setLine(p.getLineupLine());
        pp.setPos(p.getLineupPosition());
        return pp;
    }

    public static Fixture entity2domain(FixtureEntity entity, UriInfo uriInfo) {
        if (entity == null)
            return null;

        Fixture model = new Fixture();
        model.setStartTime(entity.getStartTime());
        model.setEndTime(entity.estimateEndTime());
        model.getSides().add(entity2domain(FixtureSideRole.HOME, entity.getHomeSide()));
        model.getSides().add(entity2domain(FixtureSideRole.AWAY, entity.getAwaySide()));
        model.setMatchNumber(entity.getMatchNumber());
        model.setSourceRef(entity.getSourceRef());
        model.setGamesheet(entity.getSheet());
        TournamentEntity tournament = entity.getTournament();
        String tourRef = tournament.getSlug();
        model.setTournamentRef(tourRef);
        SeasonEntity season = tournament.getSeason();
        String seasonRef = season.getId();
        model.setSeason(seasonRef);
        model.setSchedule(entity.getStatus());
        model.setMatchDetails(uriInfo.getBaseUriBuilder().path(FixtureResource.class).build(entity.getId()));

        return model;
    }

    public static Side entity2domain(FixtureSideRole role, FixtureSideEntity entity) {
        if (entity == null)
            return null;

        Side model = new Side();
        model.setRole(role);
        Side.Team team = new Side.Team();
        team.setClubRef(entity.getTeam().getClub().getSlug());
        team.setValue(entity.getTeam().getName());
        model.setTeam(team);
        model.setScore(entity.getScore());

        return model;
    }

    private static Tournament tentity2domain(TournamentEntity entity) {
        if (entity == null)
            return null;
        
        Tournament model = new Tournament();
        model.setSeason(entity.getSeason().getId());
        ClubEntity a = entity.getArena();
        if (a != null)
            model.setArena(a.getSlug());
        model.setSeries(entity.getSeries().getSlug());
        model.setTitle(entity.getName());
        
        return model;
    }
}
