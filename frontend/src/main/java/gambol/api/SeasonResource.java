package gambol.api;

import gambol.ejb.App;
import gambol.model.SeasonEntity;
import gambol.model.TournamentEntity;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author osa
 */
@RequestScoped
@Path("some-season/{seasonId: [0-9]{4}}")
public class SeasonResource {

    private final static Logger LOG = LoggerFactory.getLogger(SeasonResource.class);

    @PathParam("seasonId")
    String seasonId;

    @EJB
    App gambol;

    private SeasonEntity _season() {
        for (SeasonEntity se : gambol.getSeasons()) 
            if (seasonId.equals(se.getId())) {
                LOG.info("... {}? found {}: \"{}\"", seasonId, se.getId(), se.getName());
                return se;
            }
            else {
                LOG.info("... {}? not found {}: \"{}\"", seasonId, se.getId(), se.getName());
            }        
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    
    @GET
    @Path("{tournamentRef}")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public TournamentResource getTournament(@PathParam("tournamentRef") String slug) {
        LOG.info("GET {}/{}", seasonId, slug);

        TournamentEntity tt = gambol.getTournament(seasonId, slug);
        return null;
    }

}
