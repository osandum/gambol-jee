package gambol.api;

import gambol.ejb.App;
import gambol.model.ClubEntity;
import gambol.model.FixtureEntity;
import gambol.model.TournamentEntity;
import gambol.util.DateParam;
import gambol.xml.Club;
import gambol.xml.Fixtures;
import gambol.xml.Tournament;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
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
            @QueryParam("start") DateParam start,
            @QueryParam("end") DateParam end,
            @QueryParam("season") List<String> seasonId,
            @QueryParam("tournament") List<String> tournamentRef,
            @QueryParam("club") List<String> clubRef,
            @QueryParam("home") List<String> homeClubRef,
            @QueryParam("away") List<String> awayClubRef) {
        
        List<FixtureEntity> fixtures = gambol.getFixtures(u(start), u(end), seasonId, tournamentRef, clubRef, homeClubRef, awayClubRef);
        
        Fixtures res = new Fixtures();
        for (FixtureEntity entity : fixtures) {
            res.getFixtures().add(TournamentResource.entity2domain(entity));
        }
        
        return Response.ok(res).build();
    }
     
    private static Date u(DateParam p) {
        return p == null ? null : p.getValue();
    }
    
    @GET
    @Path("events")
    @Produces(APPLICATION_JSON)
    public List<FullCalendarEvent> getFixturesFullCalendarEvents(
            @QueryParam("start") DateParam start,
            @QueryParam("end") DateParam end,
            @QueryParam("season") List<String> seasonId,
            @QueryParam("tournament") List<String> tournamentRef,
            @QueryParam("club") List<String> clubRef,
            @QueryParam("home") List<String> homeClubRef,
            @QueryParam("away") List<String> awayClubRef) throws ValidationException, IOException {
        
        List<FixtureEntity> fixtures = gambol.getFixtures(u(start), u(end), seasonId, tournamentRef, clubRef, homeClubRef, awayClubRef);
        
        List<FullCalendarEvent> res = new LinkedList<FullCalendarEvent>();
        for (FixtureEntity f : fixtures) {
            FullCalendarEvent e = new FullCalendarEvent();
            e.id = f.getSourceRef(); // TODO: make our own opaque ref
            e.title = f.getEventTitle(); 
            e.start = f.getStartTime();
            e.end = f.getEndTime();
            res.add(e);
        }
        return res;
    }
    
    @GET
    @Path("fixtures.ics")
    @Produces("text/calendar; charset=UTF-8")
    public Response getFixturesCalendar(
            @QueryParam("start") DateParam start,
            @QueryParam("end") DateParam end,
            @QueryParam("season") List<String> seasonId,
            @QueryParam("tournament") List<String> tournamentRef,
            @QueryParam("club") List<String> clubRef,
            @QueryParam("home") List<String> homeClubRef,
            @QueryParam("away") List<String> awayClubRef) throws ValidationException, IOException {
        
        List<FixtureEntity> fixtures = gambol.getFixtures(u(start), u(end), seasonId, tournamentRef, clubRef, homeClubRef, awayClubRef);

        Calendar cal = getCalendar(fixtures);
        
        cal.validate();
        
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        new CalendarOutputter().output(cal, bo);
        bo.close();
        byte[] res = bo.toByteArray();

        String f = "";
        for (String p : clubRef)
            f += p + "-";
        for (String p : tournamentRef)
            f += p + "-";
        for (String p : seasonId)
            f += p + "-";
        
        return Response.ok(res)
                .header("Content-Disposition", "attachment;filename=" + f + "fixtures.ics")
                .build();
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
            String eventName = f.getEventTitle();
            DateTime start = new DateTime(f.getStartTime());
            DateTime end = new DateTime(f.getEndTime());
            VEvent evt = new VEvent(start, end, eventName);
            
            String uid = "GAMBOL:fixture:" + f.getSourceRef();
            evt.getProperties().add(new Uid(uid));
            
            ClubEntity homeClub = f.getHomeSide().getTeam().getClub();
            if (!StringUtils.isEmpty(homeClub.getAddress())) 
                evt.getProperties().add(new Location(homeClub.getAddress()));
            if (homeClub.getLatitude() != null && homeClub.getLongitude() != null)
                evt.getProperties().add(new Geo(homeClub.getLatitude(), homeClub.getLongitude()));
            
            cal.getComponents().add(evt);
            
            ++n;
        }
        
        return cal;
        
    }
}
