package gambol.api;

import gambol.ejb.App;
import gambol.model.Club;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import static javax.ws.rs.core.MediaType.*;

/**
 *
 * @author osa
 */
@Path("/clubs")
@RequestScoped
public class ClubsResource {
    @EJB
    App gambol;

    @GET
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public List<Club> listAllClubs() {
        return gambol.getClubs();
    }

    @Inject
    ClubResource clubResource;
    
    @Path("{slug}")
    public ClubResource getClub() {
        return clubResource;
    }
}
