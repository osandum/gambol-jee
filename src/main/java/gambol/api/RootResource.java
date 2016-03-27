package gambol.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * @author osa
 */

@Path("/")
public class RootResource {

    @GET
    public Response listAllClubs() {
        return Response.ok().build();
    }
}
