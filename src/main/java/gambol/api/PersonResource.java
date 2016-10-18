package gambol.api;

import gambol.ejb.App;
import gambol.model.FixtureEntity;
import gambol.model.FixturePlayerEntity;
import gambol.model.FixtureSideEntity;
import gambol.model.GoalEventEntity;
import gambol.model.PersonEntity;
import gambol.model.TournamentEntity;
import gambol.xml.FixtureEvents;
import gambol.xml.Fixtures;
import gambol.xml.GoalEvent;
import gambol.xml.Person;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
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
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * @author osa
 */
@Stateful
@Path("person/{personId}")
public class PersonResource {

    @Inject
    private Logger LOG;

    @EJB
    App gambol;

    @PathParam("personId")
    private long personId;

    @GET
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Person getPerson(@Context UriInfo uriInfo) {

        PersonEntity p = gambol.getPersonById(personId);
        if (p == null)
            throw new WebApplicationException("Not found", Response.Status.NOT_FOUND);
        
        LOG.info("# person "+personId+" loaded: " + p);
        
        return entity2person(p, uriInfo);
    }

    @GET
    @Path("fixtures")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Response getFixtures(@Context UriInfo uriInfo) {

        PersonEntity p = gambol.getPersonById(personId);
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

        PersonEntity p = gambol.getPersonById(personId);
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

    private Person entity2person(PersonEntity p, UriInfo uriInfo) {
        Person xo = new Person();
        xo.setFirstNames(p.getFirstNames());
        xo.setLastName(p.getLastName());
        return xo;
    }
    
}
