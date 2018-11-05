package gambol.api;

import gambol.ejb.App;
import gambol.model.ClubEntity;
import gambol.model.FixtureEntity;
import gambol.model.FixtureEventEntity;
import gambol.model.FixturePlayerEntity;
import gambol.model.FixtureSideEntity;
import gambol.model.SeasonEntity;
import gambol.model.TournamentEntity;
import gambol.model.TeamEntity;
import gambol.xml.Fixture;
import gambol.xml.FixtureEvents;
import gambol.xml.FixtureSideRole;
import gambol.xml.Gamesheet;
import gambol.xml.GamesheetStatus;
import gambol.xml.Player;
import gambol.xml.Roster;
import gambol.xml.Side;
import gambol.xml.TeamDef;
import gambol.xml.Tournament;
import java.util.Date;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import static javax.ws.rs.core.MediaType.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author osa
 */
@Stateful
@Path("fixture/{fixtureId}")
public class FixtureResource {

    private final static Logger LOG = LoggerFactory.getLogger(FixtureResource.class);

    @EJB
    App gambol;

    @PathParam("fixtureId")
    private long fixtureId;

    @GET
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Gamesheet getGamesheet(@Context UriInfo uriInfo) {

        FixtureEntity f = gambol.getFixtureById(fixtureId);
        if (f == null)
            throw new WebApplicationException("Not found", Response.Status.NOT_FOUND);

        LOG.info("# fixture loaded: {}", f);

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
            fe.getGoalsAndPenalties().add(e.asXml(null));

        sheet.setEvents(fe);
        sheet.setSourceRef(f.getSourceRef());
        sheet.setMatchNumber(f.getMatchNumber());
        sheet.setTournament(tentity2domain(f.getTournament()));
        sheet.setStartTime(f.getStartTime());

        return sheet;
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
        model.setGamesheet(entity.getGamesheetStatus());
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
