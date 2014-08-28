package gambol.api;

import gambol.ejb.App;
import gambol.model.TournamentEntity;
import gambol.xml.Tournament;
import java.net.URI;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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

    @PersistenceContext
    private EntityManager em;

    @Context 
    private UriInfo uriInfo;
    
    @POST
    @Path("tournament")
    @Consumes({APPLICATION_JSON, APPLICATION_XML})
    public Response putTournament(Tournament tt) {
        // Do the work:
        TournamentEntity t = gambol.putTournament(tt);
        
        // Construct resource URL:
        URI tournamentUri = uriInfo.getBaseUriBuilder().path("tournament/{seasonId}/{tournamentSlug}").build(t.getSeason().getId(), t.getSlug());        
        
        return Response.created(tournamentUri).build();
    }
}
