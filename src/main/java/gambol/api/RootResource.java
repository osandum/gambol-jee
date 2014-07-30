package gambol.api;

import gambol.ejb.App;
import gambol.model.ClubEntity;
import gambol.model.FixtureEntity;
import gambol.model.SeasonEntity;
import gambol.model.TournamentEntity;
import gambol.xml.Club;
import gambol.xml.Fixture;
import gambol.xml.Tournament;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import static javax.ws.rs.core.MediaType.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author osa
 */
@RequestScoped
@Path("/")
public class RootResource {
    @EJB
    App gambol;

    @GET
    @Path("clubs")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public List<Club> listAllClubs() {
        List<Club> res = new LinkedList<Club>();
        for (ClubEntity entity : gambol.getClubs())
            res.add(ClubResource.entity2domain(entity));
        return res;
    }
    
    @Context
    UriInfo uriInfo;
    
    @GET
    @Path("tournaments")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Response listAllTournaments() {
        List<Tournament> res = new LinkedList<Tournament>();
        for (TournamentEntity entity : gambol.getAllTournaments())
            res.add(TournamentResource.entity2domain(entity));
        return Response.ok(res.toArray(new Tournament[0])).build();
    }
    
    @GET
    @Path("fixtures")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Response getFixtures(
            @QueryParam("season") List<String> seasonId,
            @QueryParam("tournament") List<String> tournamentRef,
            @QueryParam("club") List<String> clubRef) {
        
        List<FixtureEntity> fixtures = gambol.getFixtures(seasonId, tournamentRef, clubRef);
        
        List<Fixture> res = new LinkedList<Fixture>();
        for (FixtureEntity entity : fixtures)
            res.add(TournamentResource.entity2domain(entity));
        
        return Response.ok(res.toArray(new Fixture[0])).build();
    }
        
}
