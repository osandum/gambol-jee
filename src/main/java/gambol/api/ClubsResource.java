package gambol.api;

import gambol.ejb.App;
import gambol.model.Club;
import gambol.model.Tournament;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
public class ClubsResource {
    @EJB
    App gambol;

    @GET
    @Path("clubs")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public List<Club> listAllClubs() {
        return gambol.getClubs();
    }

//    @Inject
//    ClubResource clubResource;
    
//    @Path("clubs/{slug}")
//    public ClubResource getClub() {
//        return clubResource;
//    }
    
    @Context
    UriInfo uriInfo;
    
    @GET
    @Path("tournaments")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Response listAllTournaments() {
        List<Tournament> res = gambol.getAllTournaments();
        return Response.ok(res.toArray(new Tournament[0]))
                .build();
    }
}
