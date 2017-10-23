package gambol.api;

import gambol.ejb.App;
import gambol.ejb.FixturesQueryParam;
import gambol.ejb.PlayersQueryParam;
import gambol.model.ClubEntity;
import gambol.model.FixtureEntity;
import gambol.model.PersonEntity;
import gambol.util.DateParam;
import gambol.xml.Club;
import gambol.xml.Fixtures;
import gambol.xml.Person;
import gambol.xml.Series;
import gambol.xml.Tournament;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
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
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Geo;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.model.property.XProperty;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author osa
 */
@RequestScoped
@Path("/")
public class RootResource {

    @Inject
    private Logger LOG;

    @EJB
    App gambol;

    @GET
    @Path("series")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public List<Series> listAllSeries() {
        List<Series> res = new LinkedList<>();
        gambol.getSeries().stream().forEach((entity) -> {
            res.add(SeriesResource.entity2domain(entity));
        });
        return res;
    }

    @GET
    @Path("clubs")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public List<Club> listAllClubs() {
        List<Club> res = new LinkedList<>();
        gambol.getClubs().stream().forEach((entity) -> {
            res.add(ClubResource.entity2domain(entity));
        });
        return res;
    }

    @Context
    UriInfo uriInfo;

    @GET
    @Path("tournaments")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public List<Tournament> listAllTournaments(@Context UriInfo uriInfo) {
        List<Tournament> res = new LinkedList<>();
        gambol.getAllTournaments().stream().forEach((entity) -> {
            res.add(TournamentResource.entity2domain(entity, uriInfo));
        });
        return res; //Response.ok(res.toArray(new Tournament[0])).build();
    }

    @GET
    @Path("player")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public List<Person> findPlayer(
            @Context UriInfo uriInfo,
            @QueryParam("name") String name,
            @QueryParam("club") String clubRef) {
        
        PlayersQueryParam searchParams =  playerQ(name);
        List<PersonEntity> people = gambol.getPlayers(searchParams);
        
        
        List<Person> res = new LinkedList<>();
        people.stream().forEach((entity) -> {
            res.add(PersonResource.entity2person(entity, uriInfo));
        });
        return res;
    }

    @GET
    @Path("fixtures")
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Response getFixtures(
            @QueryParam("start") DateParam start,
            @QueryParam("end") DateParam end,
            @QueryParam("sheet") Boolean hasGamesheet,
            @QueryParam("season") List<String> seasonId,
            @QueryParam("series") List<String> seriesId,
            @QueryParam("tournament") List<String> tournamentRef,
            @QueryParam("club") List<String> clubRef,
            @QueryParam("home") List<String> homeClubRef,
            @QueryParam("away") List<String> awayClubRef,
            @Context UriInfo uriInfo) {

        FixturesQueryParam searchParams = fixtureQ(start, end, hasGamesheet, seasonId, seriesId, tournamentRef, clubRef, homeClubRef, awayClubRef);
        List<FixtureEntity> fixtures = gambol.getFixtures(searchParams);

        Fixtures res = new Fixtures();
        fixtures.stream().forEach((entity) -> {
            res.getFixtures().add(FixtureResource.entity2domain(entity, uriInfo));
        });

        return Response.ok(res).build();
    }

    private static Date u(DateParam p) {
        return p == null ? null : p.getValue();
    }

    private static PlayersQueryParam playerQ(String name) {
        PlayersQueryParam res = new PlayersQueryParam();
        res.setName(name);
        return res;
    }

    private static FixturesQueryParam fixtureQ(DateParam start, DateParam end, Boolean sheet, List<String> seasonId, List<String> seriesId, List<String> tournamentRef, List<String> clubRef, List<String> homeClubRef, List<String> awayClubRef) {
        FixturesQueryParam res = new FixturesQueryParam();
        res.setStart(u(start));
        res.setEnd(u(end));
        res.setGamesheet(sheet);
        res.setSeasonId(seasonId);
        res.setSeriesId(seriesId);
        res.setTournamentRef(tournamentRef);
        res.setClubRef(clubRef);
        res.setHomeClubRef(homeClubRef);
        res.setAwayClubRef(awayClubRef);
        return res;
    }

  /*@GET
    @Path("{seasonId}")
    @Produces(APPLICATION_JSON)
    public List<Tournament> getSeasonResource(@PathParam("seasonId") String seasonId) {
        return null;
    }*/

    @GET
    @Path("events")
    @Produces(APPLICATION_JSON)
    public List<FullCalendarEvent> getFixturesFullCalendarEvents(
            @QueryParam("start") DateParam start,
            @QueryParam("end") DateParam end,
            @QueryParam("sheet") Boolean hasGamesheet,
            @QueryParam("season") List<String> seasonId,
            @QueryParam("series") List<String> seriesId,
            @QueryParam("tournament") List<String> tournamentRef,
            @QueryParam("club") List<String> clubRef,
            @QueryParam("home") List<String> homeClubRef,
            @QueryParam("away") List<String> awayClubRef) throws ValidationException, IOException {

        LOG.info("### get fullcalendar events...");
        
        FixturesQueryParam searchParams = fixtureQ(start, end, hasGamesheet, seasonId, seriesId, tournamentRef, clubRef, homeClubRef, awayClubRef);
        List<FixtureEntity> fixtures = gambol.getFixtures(searchParams);

        List<FullCalendarEvent> res = new LinkedList<FullCalendarEvent>();
        for (FixtureEntity f : fixtures) {
            FullCalendarEvent e = new FullCalendarEvent();
            e.id = f.getSourceRef(); // TODO: make our own opaque ref
            e.title = f.getEventTitle();
            e.start = f.getStartTime();
            e.end = f.estimateEndTime();
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
            @QueryParam("sheet") Boolean hasGamesheet,
            @QueryParam("season") List<String> seasonId,
            @QueryParam("series") List<String> seriesId,
            @QueryParam("tournament") List<String> tournamentRef,
            @QueryParam("club") List<String> clubRef,
            @QueryParam("home") List<String> homeClubRef,
            @QueryParam("away") List<String> awayClubRef,
            @QueryParam("calname") String calname,
            @QueryParam("caldesc") String caldesc) throws ValidationException, IOException {

        LOG.info("### get a fixtures calendar...");

        FixturesQueryParam searchParams = fixtureQ(start, end, hasGamesheet, seasonId, seriesId, tournamentRef, clubRef, homeClubRef, awayClubRef);
        List<FixtureEntity> fixtures = gambol.getFixtures(searchParams);

        Calendar cal = getCalendar(fixtures, calname, caldesc);

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

    protected Calendar getCalendar(List<FixtureEntity> fixtures, String calname, String caldesc) {
        TimeZoneRegistry tzr = TimeZoneRegistryFactory.getInstance().createRegistry();
        TimeZone tz = tzr.getTimeZone("Europe/Copenhagen");
        VTimeZone vtz = tz.getVTimeZone();

        Calendar cal = new Calendar();
        cal.getComponents().add(vtz);
        cal.getProperties().add(new ProdId("-//Gambol//iCal4j 1.0//EN"));
        cal.getProperties().add(Version.VERSION_2_0);
        cal.getProperties().add(CalScale.GREGORIAN);
        if (calname != null)
            cal.getProperties().add(new XProperty("X-WR-CALNAME", calname));
        if (caldesc != null)
            cal.getProperties().add(new XProperty("X-WR-CALDESC", caldesc));
        cal.getProperties().add(new XProperty("X-PUBLISHED-TTL", "PT5M"));

        int n = 0;
        for (FixtureEntity f : fixtures) {
            DateTime start = new DateTime(f.getStartTime());
            DateTime end = new DateTime(f.estimateEndTime());
            String summary = f.getEventTitle();
            VEvent evt = new VEvent(start, end, summary);

            String uid = "GAMBOL:fixture:" + f.getSourceRef();
            evt.getProperties().add(new Uid(uid));

            String description = f.getEventDescription();
            evt.getProperties().add(new Description(description));

            ClubEntity arena = f.resolveArena();
            if (!StringUtils.isEmpty(arena.getAddress()))
                evt.getProperties().add(new Location(arena.getAddress()));
            if (arena.getLatitude() != null && arena.getLongitude() != null)
                evt.getProperties().add(new Geo(arena.getLatitude(), arena.getLongitude()));

            cal.getComponents().add(evt);

            ++n;
        }

        return cal;
    }
}
