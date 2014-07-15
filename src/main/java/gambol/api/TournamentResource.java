package gambol.api;

import gambol.ejb.App;
import gambol.model.ClubEntity;
import gambol.model.FixtureEntity;
import gambol.model.FixtureSideEntity;
import gambol.model.TournamentEntity;
import gambol.xml.Fixture;
import gambol.xml.FixtureSideRole;
import gambol.xml.Fixtures;
import gambol.xml.Side;
import gambol.xml.Tournament;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;
import javax.ws.rs.WebApplicationException;
import static javax.ws.rs.core.MediaType.*;
import javax.ws.rs.core.Response;

/**
 *
 * @author osa
 */
//@RequestScoped
@Stateless
@LocalBean
@Path("tournament/{seasonId}/{slug}")
public class TournamentResource {

    private final static Logger LOG = Logger.getLogger(TournamentResource.class.getName());
    
    @PathParam("seasonId")
    String seasonId;

    @PathParam("slug")
    String slug;

    @EJB
    App gambol;

    @PersistenceContext
    private EntityManager em;
    

    @GET
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Tournament getTournament() {
        LOG.log(Level.INFO, ".... {0} {1}", new Object[]{slug, seasonId});
        
        for (TournamentEntity t : gambol.getAllTournaments())
            if (slug.equals(t.getSlug()) && seasonId.equals(t.getSeason().getId())) 
                return entity2domain(t);

        throw new WebApplicationException("Not found", Response.Status.NOT_FOUND);
    }

    @PUT
    @Consumes({APPLICATION_JSON, APPLICATION_XML})
    public Response putTournament(Tournament tt) {
        
        for (TournamentEntity t : gambol.getAllTournaments())
            if (slug.equals(t.getSlug()) && seasonId.equals(t.getSeason().getId())) {
                t.setSourceRef(tt.getSourceRef());
                return Response.noContent().build();
            }
        
        TournamentEntity t = new TournamentEntity();
        t.setSourceRef(tt.getSourceRef());
        t.setSlug(slug);
        t.setSeason(gambol.findOrCreateSeason(seasonId));
        t.setName(tt.getTitle());
        em.persist(t);
        
        return Response.ok().build();
    }

    @GET
    @Path("fixtures")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Response getTournamentFixtures() {
        LOG.log(Level.INFO, ".... {0} {1}", new Object[]{slug, seasonId});
        
        for (TournamentEntity t : gambol.getAllTournaments())
            if (slug.equals(t.getSlug()) && seasonId.equals(t.getSeason().getId())) {
                Fixtures res = new Fixtures();
                for (FixtureEntity f : t.getFixtures())
                    res.getFixtures().add(entity2domain(f));
                
                return Response.ok(res).build();
            }

        throw new WebApplicationException("Not found", Response.Status.NOT_FOUND);
    }

    @PUT
    @Path("fixtures")
    @Consumes({APPLICATION_JSON, APPLICATION_XML})
    public Response putTournamentFixtures(Fixtures fs) {
        LOG.log(Level.INFO, ".... {0} {1}", new Object[]{slug, seasonId});
        
        for (TournamentEntity t : gambol.getAllTournaments())
            if (slug.equals(t.getSlug()) && seasonId.equals(t.getSeason().getId())) {
                Map<String,FixtureEntity> all = new HashMap<String,FixtureEntity>();
                for (FixtureEntity f : t.getFixtures())
                    all.put(f.getSourceRef(), f);
                
                List<FixtureEntity> nf = new LinkedList<FixtureEntity>();
                for (Fixture fo : fs.getFixtures()) {
                    FixtureEntity f = all.get(fo.getSourceRef());
                    if (f == null) {
                        // new fixture
                        f = new FixtureEntity();
                        f.setSourceRef(fo.getSourceRef());
                        f.setTournament(t);
                        for (Side s : fo.getSides()) {
                            FixtureSideEntity fe = new FixtureSideEntity();
                            fe.setClub(gambol.findOrCreateClub(s.getTeam()));
                            fe.setScore(s.getScore());
                            FixtureSideRole role = s.getRole();
                            if (FixtureSideRole.HOME.equals(role))
                                f.setHomeSide(fe);
                            else if (FixtureSideRole.AWAY.equals(role))
                                f.setAwaySide(fe);
                        }
                        em.persist(f);
                        nf.add(f);
                    }
                    else {
                        // existing fixture, update:
                        domain2entity(fo, f);
                    }
                   //:.. 
                }
                
                return Response.ok().build();
            }

        throw new WebApplicationException("Not found", Response.Status.NOT_FOUND);
    }
    
    public static void domain2entity(Fixture f, FixtureEntity entity) {
        FixtureSideEntity homeSide = entity.getHomeSide();
        FixtureSideEntity awaySide = entity.getAwaySide();
        entity.setStartTime(f.getStartTime());
        entity.setEndTime(f.getEndTime());
        for (Side s : f.getSides()) {
            ; //...
        }
    }

    public static Fixture entity2domain(FixtureEntity entity) {
        if (entity == null)
            return null;
        
        Fixture model = new Fixture();
        model.setStartTime(entity.getStartTime());
        model.setEndTime(entity.getEndTime());
        model.getSides().add(entity2domain(FixtureSideRole.HOME, entity.getHomeSide()));
        model.getSides().add(entity2domain(FixtureSideRole.AWAY, entity.getAwaySide()));
        model.setSourceRef(entity.getSourceRef());
        
        return model;
    }

    public static Side entity2domain(FixtureSideRole role, FixtureSideEntity entity) {
        if (entity == null)
            return null;
        
        Side model = new Side();
        model.setRole(role);
        model.setTeam(entity.getClub().getSlug());
        model.setScore(entity.getScore());

        return model;
    }
    
    public static Tournament entity2domain(TournamentEntity entity) {
        if (entity == null)
            return null;

        Tournament model = new Tournament();
        model.setSeason(entity.getSeason().getName());
        model.setTitle(entity.getName());
        model.setSourceRef(entity.getSourceRef());
        
        return model;
    }
}
