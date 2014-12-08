package gambol.api;

import gambol.ejb.App;
import gambol.model.FixtureEntity;
import gambol.model.FixtureEventEntity;
import gambol.model.FixturePlayerEntity;
import gambol.model.FixtureSideEntity;
import gambol.model.GoalEventEntity;
import gambol.model.PenaltyEventEntity;
import gambol.model.SeasonEntity;
import gambol.model.TournamentEntity;
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
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import javax.ws.rs.core.UriInfo;

/**
 * @author osa
 */
@Stateless
@LocalBean
@Path("fixture/{fixtureId}")
public class FixtureResource {

    @EJB
    App gambol;

    @PersistenceContext
    private EntityManager em;
    
    @PathParam("fixtureId")
    private long fixtureId;

    @GET
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Gamesheet getGamesheet(@Context UriInfo uriInfo) {
        
        FixtureEntity f = gambol.getFixtureById(fixtureId);
        return entity2gamesheet(f, uriInfo);
    }
    
    public static Gamesheet entity2gamesheet(FixtureEntity f, UriInfo uriInfo) {
        Gamesheet sheet = new Gamesheet();
        
        Roster homeR = new Roster();
        homeR.setSide(FixtureSideRole.HOME);
        for (FixturePlayerEntity p : f.getHomeSide().getPlayers())
            homeR.getPlayers().add(pentity2domain(p));
        sheet.getRosters().add(homeR);
        
        Roster awayR = new Roster();
        awayR.setSide(FixtureSideRole.AWAY);
        for (FixturePlayerEntity p : f.getAwaySide().getPlayers())
            awayR.getPlayers().add(pentity2domain(p));
        sheet.getRosters().add(awayR);
        
        FixtureEvents fe = new FixtureEvents();
        for (FixtureEventEntity e : f.getHomeSide().getEvents())
            fe.getGoalsAndPenalties().add(eentity2domain(FixtureSideRole.HOME, e));
        for (FixtureEventEntity e : f.getAwaySide().getEvents())
            fe.getGoalsAndPenalties().add(eentity2domain(FixtureSideRole.AWAY, e));
        
//      Collections.sort(fe.getGoalsAndPenalties());
        
        sheet.setEvents(fe);
        
        sheet.setSourceRef(f.getSourceRef());
        
        return sheet;
    }
    
    private static Event eentity2domain(FixtureSideRole side, FixtureEventEntity e) {
        if (e instanceof GoalEventEntity) {
            GoalEventEntity gee = (GoalEventEntity)e;
            GoalEvent ge = new GoalEvent();
            PlayerRef pr = new PlayerRef();
            pr.setNumber(gee.getPlayer().getJerseyNumber());
            ge.setPlayer(pr);
            ge.setSide(side);
            ge.setGameSituation(gee.getGameSituation());
            ge.setTime(App.gameTimeCode(e.getGameTimeSecond()));
            return ge;
        }
        if (e instanceof PenaltyEventEntity) {
            PenaltyEventEntity pee = (PenaltyEventEntity)e;
            PenaltyEvent pe = new PenaltyEvent();
            PlayerRef pr = new PlayerRef();
            pr.setNumber(pee.getPlayer().getJerseyNumber());
            pe.setPlayer(pr);
            pe.setSide(side);
            pe.setOffense(pee.getOffense());
            pe.setMinutes(pee.getPenaltyMinutes());
            pe.setEndTime(App.gameTimeCode(pee.getEndtimeSecond()));
            pe.setTime(App.gameTimeCode(e.getGameTimeSecond()));
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
        TournamentEntity tournament = entity.getTournament();
        String tourRef = tournament.getSlug();
        model.setTournamentRef(tourRef);
        SeasonEntity season = tournament.getSeason();
        String seasonRef = season.getId();
        model.setSeason(seasonRef);
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
}
