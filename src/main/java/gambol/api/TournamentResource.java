package gambol.api;

import gambol.ejb.App;
import gambol.ejb.FixturesQueryParam;
import gambol.model.FixtureEntity;
import gambol.model.SeasonEntity;
import gambol.model.TournamentEntity;
import gambol.xml.Fixture;
import gambol.xml.Fixtures;
import gambol.xml.Tournament;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
//import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;
import javax.ws.rs.container.ResourceContext;
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
@Path("{seasonId: [0-9]{4}}/{slug}")
public class TournamentResource {

    private final static Logger LOG = LoggerFactory.getLogger(TournamentResource.class);

    @Context
    ResourceContext context;
    
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
        LOG.info(".... {}/{}", seasonId, slug);
        
        TournamentEntity t = _tournament();
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
        LOG.info(".... {}/{}", seasonId, slug);
        
        TournamentEntity t = gambol.getTournament(seasonId, slug);

        Fixtures res = new Fixtures();
        for (FixtureEntity f : t.getFixtures())
            res.getFixtures().add(FixtureResource.entity2domain(f, uriInfo));

        return Response.ok(res).build();
    }

    private TournamentEntity _tournament() {
        return gambol.getTournament(seasonId, slug);
    }
    
    @PUT
    @Path("fixtures")
    @Consumes({APPLICATION_JSON, APPLICATION_XML})
    public Response putTournamentFixtures(Fixtures fs) {
        LOG.info(".... {} {}", slug, seasonId);

        TournamentEntity t = _tournament();
        gambol.updateFixtures(t, fs.getFixtures());
        return Response.ok().build();
    }

    private final static TimeZone CPH = TimeZone.getTimeZone("Europe/Copenhagen");

    @Inject
    Instance<FixtureResource> fixtureResources;
    
//  @GET
    @Path("{mm}/{dd}/{home: [^_]+}_{away}")
//  @Produces({APPLICATION_JSON, APPLICATION_XML})
    public FixtureResource getFixtureByDate(
            @PathParam("mm") int month,
            @PathParam("dd") int dayOfMonth,
            @PathParam("home") String home,
            @PathParam("away") String away,
            @Context UriInfo uriInfo) {
                
        FixturesQueryParam p = new FixturesQueryParam();
        p.setSeasonId(Collections.singletonList(seasonId));
        p.setTournamentRef(Collections.singletonList(slug));
        p.setHomeClubRef(Collections.singletonList(home));
        p.setAwayClubRef(Collections.singletonList(away));
        
        Calendar c = Calendar.getInstance(CPH);
        int year = Integer.parseInt(seasonId);
        c.set(Calendar.YEAR, month >= 7 ? year : year + 1);
        c.set(Calendar.MONTH, month-1);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Date dayStart = c.getTime();
        c.add(Calendar.DATE, 1);
        Date dayEnd = c.getTime();
        p.setStart(dayStart);
        p.setEnd(dayEnd);
        
        LOG.info("search for {}...", p);
        for (FixtureEntity f : gambol.getFixtures(p)) {
            c.setTime(f.getStartTime());
            int mm = c.get(Calendar.MONTH) + 1;
            int dd = c.get(Calendar.DAY_OF_MONTH);
            if (month == mm && dayOfMonth == dd) {
                LOG.info("...{}/{} === {}/{} ({})", mm, dd, month, dayOfMonth, f);
                
                FixtureResource res = fixtureResources.get();
                res.setFixture(f);
                LOG.info("...found fixture[{}]", f.getId());
                return res;
            }
        }
        
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @GET
    @Path("fixture/{matchNumber}")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Response getFixture(
            @PathParam("matchNumber") String matchNumber,
            @Context UriInfo uriInfo) {
        LOG.info(".... {} {}", slug, seasonId);

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
