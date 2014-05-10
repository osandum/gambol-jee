package gambol.api;

import gambol.ejb.App;
import gambol.model.Club;
import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import static javax.ws.rs.core.MediaType.*;
import javax.ws.rs.core.Response;

/**
 * @author osa
 */
public class ClubResource {

    @PathParam("slug")
    String slug;

    @EJB
    App gambol;

    @GET
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Club getClub() {
        for (Club c : gambol.getClubs()) {
            if (slug.equals(c.getSlug())) {
                return c;
            }
        }

        throw new WebApplicationException("Not found", Response.Status.NOT_FOUND);
    }

}
