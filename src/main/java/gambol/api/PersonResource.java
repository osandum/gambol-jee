package gambol.api;

import gambol.ejb.App;
import gambol.model.FixtureEntity;
import gambol.model.FixturePlayerEntity;
import gambol.model.FixtureSideEntity;
import gambol.model.GoalEventEntity;
import gambol.model.PenaltyEventEntity;
import gambol.model.PersonEntity;
import gambol.model.TournamentEntity;
import gambol.xml.FixtureEvents;
import gambol.xml.Fixtures;
import gambol.xml.Person;
import gambol.xml.Player;
import java.util.Date;
import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author osa
 */
@Stateful
@Path("person/{slug}")
public class PersonResource {

    private final static Logger LOG = LoggerFactory.getLogger(PersonResource.class);

    @EJB
    App gambol;

    @PathParam("slug")
    private String slug;

    @GET
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Person getPerson(@Context UriInfo uriInfo) {

        PersonEntity p = gambol.findPerson(slug);
        if (p == null)
            throw new WebApplicationException("Not found", Response.Status.NOT_FOUND);
        
        LOG.info("# person "+slug+" loaded: " + p);
        
        return entity2person(p, uriInfo);
    }

    @GET
    @Path("fixtures")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Response getFixtures(@Context UriInfo uriInfo) {

        PersonEntity p = gambol.findPerson(slug);
        if (p == null)
            throw new WebApplicationException("Not found", Response.Status.NOT_FOUND);

        Fixtures res = new Fixtures();
        
        for (FixturePlayerEntity fp : gambol.getFixturesByPlayer(p.getId())) {
            FixtureEntity fixture = fp.getFixture();
            res.getFixtures().add(FixtureResource.entity2domain(fixture, uriInfo));

            TournamentEntity tournament = fixture.getTournament();
            Date date = fixture.getStartTime();
            FixtureSideEntity home = fixture.getHomeSide();
            FixtureSideEntity away = fixture.getAwaySide();
            
            LOG.info(date + " " + tournament.getSeries().getName() + "("+ tournament.getSeason().getName() +") " + home.getTeam().getName() + "-" + away.getTeam().getName() + ": "+fp.getSide().getTeam().getClub().getName() +" #" + fp.getJerseyNumber() + " " + fp.getLineupLine() + "/" + fp.getLineupPosition());
        }
        
        return Response.ok(res).build();
    }

    @GET
    @Path("goals")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Response getGoals(@Context UriInfo uriInfo) {

        PersonEntity p = gambol.findPerson(slug);
        if (p == null)
            throw new WebApplicationException("Not found", Response.Status.NOT_FOUND);

        FixtureEvents res = new FixtureEvents();
        
        for (GoalEventEntity ge : gambol.getGoalsByPlayer(p.getId())) {
            FixtureEntity fixture = ge.getFixture();
            res.getGoalsAndPenalties().add(ge.asXml(uriInfo));

            TournamentEntity tournament = fixture.getTournament();
            Date date = fixture.getStartTime();
            FixtureSideEntity home = fixture.getHomeSide();
            FixtureSideEntity away = fixture.getAwaySide();
            
            LOG.info(date + " " + tournament.getSeries().getName() + "("+ tournament.getSeason().getName() +") " + home.getTeam().getName() + "-" + away.getTeam().getName() + ": "+ + ge.getGameTimeSecond() + " " + ge.getGameSituation());
        }
        
        return Response.ok(res).build();
    }

    @GET
    @Path("penalties")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Response getPenalties(@Context UriInfo uriInfo) {

        PersonEntity p = gambol.findPerson(slug);
        if (p == null)
            throw new WebApplicationException("Not found", Response.Status.NOT_FOUND);

        FixtureEvents res = new FixtureEvents();
        
        for (PenaltyEventEntity pe : gambol.getPenaltiesByPlayer(p.getId())) {
            FixtureEntity fixture = pe.getFixture();
            res.getGoalsAndPenalties().add(pe.asXml(uriInfo));

            TournamentEntity tournament = fixture.getTournament();
            Date date = fixture.getStartTime();
            FixtureSideEntity home = fixture.getHomeSide();
            FixtureSideEntity away = fixture.getAwaySide();
            
            LOG.info(date + " " + tournament.getSeries().getName() + "("+ tournament.getSeason().getName() +") " + home.getTeam().getName() + "-" + away.getTeam().getName() + ": "+ + pe.getGameTimeSecond() + " " + pe.getOffense() + ":" + pe.getPenaltyMinutes());
        }
        
        return Response.ok(res).build();
    }

    public static Person entity2person(PersonEntity p, UriInfo uriInfo) {
        if (p == null)
            return null;

        Person xo = new Person();
        xo.setFirstNames(p.getFirstNames());
        xo.setLastName(p.getLastName());
        xo.setSlug(p.getSlug());
        xo.setDetails(uriInfo.getBaseUriBuilder().path(PersonResource.class).build(p.getSlug()));
        return xo;
    }
    

    static Player entity2player(FixturePlayerEntity fp, UriInfo uriInfo) {
        if (fp == null)
            return null;

        Player xo = new Player();
        PersonEntity p = fp.getPerson();
        xo.setFirstNames(p.getFirstNames());
        xo.setLastName(p.getLastName());
        xo.setDetails(uriInfo.getBaseUriBuilder().path(PersonResource.class).build(p.getSlug()));
        return xo;
    }
}
