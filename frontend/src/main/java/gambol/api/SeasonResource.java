package gambol.api;

import gambol.ejb.App;
import gambol.model.SeasonEntity;
import gambol.model.TournamentEntity;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import javax.ws.rs.core.Response;
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
