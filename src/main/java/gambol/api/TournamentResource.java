package gambol.api;

import gambol.ejb.App;
import gambol.model.FixtureEntity;
import gambol.model.SeasonEntity;
import gambol.model.TournamentEntity;
import gambol.xml.Fixture;
import gambol.xml.Fixtures;
import gambol.xml.Tournament;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import static javax.ws.rs.core.MediaType.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * @author osa
 */
@Stateful
@Path("tournament/{seasonId}/{slug}")
public class TournamentResource {

    private final static Logger LOG = Logger.getLogger(TournamentResource.class.getName());
    
    @PathParam("seasonId")
    String seasonId;

    @PathParam("slug")    
    String slug;

    @Inject
    App gambol;

    @PersistenceContext
    private EntityManager em;

    @GET
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Tournament getTournament(@Context UriInfo uriInfo) {
        LOG.log(Level.INFO, ".... {0}/{1}", new Object[]{seasonId, slug});
        
        TournamentEntity t = gambol.getTournament(seasonId, slug);
        Tournament model = entity2domain(t, uriInfo);
        
        for (FixtureEntity ff : t.getFixtures())
            model.getFixtures().add(FixtureResource.entity2domain(ff, uriInfo));
        
        return model;
    }

    @PUT
    @Consumes({APPLICATION_JSON, APPLICATION_XML})
    public Response putTournament(Tournament tt) {
        
        for (TournamentEntity t : gambol.getAllTournaments())
            if (slug.equals(t.getSlug()) && seasonId.equals(t.getSeason().getId())) {
                LOG.info("tournament " + t.getSeason().getName() + "/" + t.getSlug() + " exists already: " + t);
                t.setSourceRef(tt.getSourceRef());
                return Response.noContent().build();
            }
        
        TournamentEntity t = new TournamentEntity();
        t.setSourceRef(tt.getSourceRef());
        t.setSlug(slug);
        t.setSeason(gambol.findOrCreateSeason(seasonId));
        t.setName(tt.getTitle());
        em.persist(t);
        LOG.info("tournament " + t.getSeason().getName() + "/" + t.getSlug() + " created: " + t);
        
        return Response.ok().build();
    }

    @GET
    @Path("fixtures")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Response getTournamentFixtures(@Context UriInfo uriInfo) {
        LOG.log(Level.INFO, ".... {0}/{1}", new Object[]{seasonId, slug});
        
        TournamentEntity t = gambol.getTournament(seasonId, slug);

        Fixtures res = new Fixtures();
        for (FixtureEntity f : t.getFixtures())
            res.getFixtures().add(FixtureResource.entity2domain(f, uriInfo));

        return Response.ok(res).build();
    }

    @PUT
    @Path("fixtures")
    @Consumes({APPLICATION_JSON, APPLICATION_XML})
    public Response putTournamentFixtures(Fixtures fs) {
        LOG.log(Level.INFO, ".... {0} {1}", new Object[]{slug, seasonId});

        TournamentEntity t = gambol.getTournament(seasonId, slug);
        gambol.updateFixtures(t, fs.getFixtures());
        return Response.ok().build();
    }

    @GET
    @Path("fixture/{matchNumber}")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Response getFixture(
            @PathParam("matchNumber") String matchNumber,
            @Context UriInfo uriInfo) {
        LOG.log(Level.INFO, ".... {0} {1}", new Object[]{slug, seasonId});

        FixtureEntity f = gambol.getFixture(seasonId, slug, matchNumber);
        Fixture res = FixtureResource.entity2domain(f, uriInfo);
        
        return Response.ok(res).build();
    }
    
    public static Tournament entity2domain(TournamentEntity entity, UriInfo uriInfo) {
        if (entity == null)
            return null;

        Tournament model = new Tournament();
        SeasonEntity season = entity.getSeason();
        model.setSeason(season.getId());
        model.setSlug(entity.getSlug());
        model.setTitle(entity.getName());
        model.setSourceRef(entity.getSourceRef());
        model.setSeries(entity.getSeries().getSlug());
        if (entity.getArena() != null) 
            model.setArena(entity.getArena().getSlug());
        model.setDetails(uriInfo.getBaseUriBuilder().path(TournamentResource.class).build(season.getId(), entity.getSlug()));
        
        return model;
    }
}
