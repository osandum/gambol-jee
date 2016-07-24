package gambol.api;

import gambol.ejb.App;
import gambol.model.FixtureEntity;
import gambol.model.TournamentEntity;
import gambol.xml.Gamesheet;
import gambol.xml.Tournament;
import java.net.URI;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import static javax.ws.rs.core.MediaType.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * @author osa
 */
@Stateless
@LocalBean
@Path("source")
public class SourcesResource {
    @EJB
    App gambol;

    @Context
    private UriInfo uriInfo;

    @POST
    @Path("tournament")
    @Consumes({APPLICATION_JSON, APPLICATION_XML})
    public Response putTournament(Tournament tt) {
        // Do the work:
        TournamentEntity t = gambol.putTournamentSrc(tt);

        // Construct resource URL:
        URI tournamentUri = uriInfo.getBaseUriBuilder().path("tournament/{seasonId}/{tournamentSlug}").build(t.getSeason().getId(), t.getSlug());

        return Response.created(tournamentUri).build();
    }

    @POST
    @Path("gamesheet")
    @Consumes({APPLICATION_JSON, APPLICATION_XML})
    public Response putGamesheet(Gamesheet gg) {
        // Do the work:
        FixtureEntity f = gambol.putGamesheet(gg);

        // Construct resource URL:
        TournamentEntity t = f.getTournament();
        URI tournamentUri = uriInfo.getBaseUriBuilder().path("gamesheet/{fixtureId}").build(f.getId());

        return Response.created(tournamentUri).build();
    }
}
