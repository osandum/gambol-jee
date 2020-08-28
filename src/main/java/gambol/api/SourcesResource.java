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
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import static javax.ws.rs.core.MediaType.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author osa
 */
@Stateless
@LocalBean
@Path("source")
public class SourcesResource {

    private final static Logger LOG = LoggerFactory.getLogger(SourcesResource.class);
    
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
        URI scheduleUri = uriInfo.getBaseUriBuilder().path("tournament/{seasonId}/{tournamentSlug}").build(t.getSeason().getId(), t.getSlug());

        LOG.debug("schedule: {}", scheduleUri);
        
        return Response
                .created(scheduleUri)
                .entity("Upload complete. See: " + scheduleUri + "\n")
                .build();
    }

    @POST
    @Path("gamesheet")
    @Consumes({APPLICATION_JSON, APPLICATION_XML})
    public Response putGamesheet(@Valid Gamesheet gg) {
        Tournament tt = gg.getTournament();
        if (tt == null) {
            LOG.warn("# invalid gamesheet {}", gg.getSourceRef());
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Invalid gamesheet\n")
                    .build();
        }
        
        // Do the work:
        FixtureEntity f = gambol.putGamesheet(gg);

        // Construct resource URL:
        TournamentEntity t = f.getTournament();
        URI sheetUri = uriInfo.getBaseUriBuilder().path("fixture/{fixtureId}").build(f.getId());

        LOG.debug("{} {} gamesheet: {}", gg.getSourceRef(), tt.getSeason(), sheetUri);
        
        return Response
                .created(sheetUri)
                .entity("Upload complete. See: " + sheetUri + "\n")
                .build();
    }
}
