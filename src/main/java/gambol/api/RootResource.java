package gambol.api;

import gambol.ejb.App;
import gambol.model.ClubEntity;
import gambol.model.FixtureEntity;
import gambol.model.FixtureSideEntity;
import gambol.model.SeasonEntity;
import gambol.model.TournamentEntity;
import gambol.xml.Club;
import gambol.xml.Fixture;
import gambol.xml.Fixtures;
import gambol.xml.Side;
import gambol.xml.Tournament;
import gambol.xml.TournamentTemp;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import static javax.ws.rs.core.MediaType.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Geo;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author osa
 */
@RequestScoped
@Path("/")
public class RootResource {

    @EJB
    App gambol;
    
    @GET
    @Path("clubs")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public List<Club> listAllClubs() {
        List<Club> res = new LinkedList<Club>();
        for (ClubEntity entity : gambol.getClubs()) {
            res.add(ClubResource.entity2domain(entity));
        }
        return res;
    }
    
    @Context
    UriInfo uriInfo;
    
    @GET
    @Path("tournaments")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Response listAllTournaments() {
        List<Tournament> res = new LinkedList<Tournament>();
        for (TournamentEntity entity : gambol.getAllTournaments()) {
            res.add(TournamentResource.entity2domain(entity));
        }
        return Response.ok(res.toArray(new Tournament[0])).build();
    }
    
    @GET
    @Path("fixtures")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Response getFixtures(
            @QueryParam("season") List<String> seasonId,
            @QueryParam("tournament") List<String> tournamentRef,
            @QueryParam("club") List<String> clubRef) {
        
        List<FixtureEntity> fixtures = gambol.getFixtures(seasonId, tournamentRef, clubRef);
        
        Fixtures res = new Fixtures();
        for (FixtureEntity entity : fixtures) {
            res.getFixtures().add(TournamentResource.entity2domain(entity));
        }
        
        return Response.ok(res).build();
    }
    
    @GET
    @Path("fixtures.ics")
    @Produces("text/calendar")
    public Response getFixturesCalendar(
            @QueryParam("season") List<String> seasonId,
            @QueryParam("tournament") List<String> tournamentRef,
            @QueryParam("club") List<String> clubRef) throws ValidationException, IOException {
        
        List<FixtureEntity> fixtures = gambol.getFixtures(seasonId, tournamentRef, clubRef);

        Calendar cal = getCalendar(fixtures);
        
        cal.validate();
        
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        new CalendarOutputter().output(cal, bo);
        bo.close();
        byte[] res = bo.toByteArray();
        
        return Response.ok(res).build();
    }
    
    protected Calendar getCalendar(List<FixtureEntity> fixtures) {
        TimeZoneRegistry tzr = TimeZoneRegistryFactory.getInstance().createRegistry();
        VTimeZone tz = tzr.getTimeZone("Europe/Copenhagen").getVTimeZone();
        
        Calendar cal = new Calendar();
        cal.getComponents().add(tz);
        cal.getProperties().add(new ProdId("-//Gambol//iCal4j 1.0//EN"));
        cal.getProperties().add(Version.VERSION_2_0);
        cal.getProperties().add(CalScale.GREGORIAN);
        int n = 0;
        for (FixtureEntity f : fixtures) {
            TournamentEntity tournament = f.getTournament();
            FixtureSideEntity home = f.getHomeSide();
            FixtureSideEntity away = f.getAwaySide();

            // Create the event
            String eventName = home.getTeam().getName() + " \u2013 " + away.getTeam().getName();
            eventName += " (" + tournament.getName() + ", kamp " + f.getSourceRef() + ")";
            if (home.getScore() != null && away.getScore() != null) {
                eventName += ": " + home.getScore() + "-" + away.getScore();
            }
            DateTime start = new DateTime(f.getStartTime());
            DateTime end = new DateTime(f.getEndTime());
            VEvent evt = new VEvent(start, end, eventName);
            
            String uid = "GAMBOL:fixture:" + f.getSourceRef();
            evt.getProperties().add(new Uid(uid));
            
            ClubEntity homeClub = home.getTeam().getClub();
            if (!StringUtils.isEmpty(homeClub.getAddress())) 
                evt.getProperties().add(new Location(homeClub.getAddress()));
            if (homeClub.getLatitude() != null && homeClub.getLongitude() != null)
                evt.getProperties().add(new Geo(homeClub.getLatitude(), homeClub.getLongitude()));
            
            cal.getComponents().add(evt);
            
            System.out.println(f.getStartTime() + ": " + home.getTeam() + "-" + away.getTeam());
            ++n;
        }
        
        return cal;
        
    }
}
