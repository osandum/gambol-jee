package gambol.api;

import gambol.ejb.App;
import gambol.model.ClubEntity;
import gambol.xml.Club;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import static javax.ws.rs.core.MediaType.*;
import javax.ws.rs.core.Response;

/**
 * @author osa
 */
@RequestScoped
@Path("club/{slug}")
public class ClubResource {

    @PathParam("slug")
    String slug;

    @EJB
    App gambol;

    @GET
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Club getClub() {
        for (ClubEntity c : gambol.getClubs())
            if (slug.equals(c.getSlug()))
                return entity2domain(c);

        throw new WebApplicationException("Not found", Response.Status.NOT_FOUND);
    }

    @PUT
    @Consumes({APPLICATION_JSON, APPLICATION_XML})
    public Response putClub(Club club) {
        gambol.updateOrCreateClub(slug, domain2entity(club));
        return Response.noContent().build();
    }

    public static Club entity2domain(ClubEntity entity) {
        Club c = new Club();
        c.setSlug(entity.getSlug());
        c.setName(entity.getName());
        c.setCountry(entity.getCountryIso2());
        if (entity.getAddress() != null || (entity.getLatitude() != null && entity.getLongitude() != null)) {
            Club.Address address = new Club.Address();
            address.setValue(entity.getAddress());
            address.setLat(entity.getLatitude());
            address.setLon(entity.getLongitude());
            c.setAddress(address);
        }
        return c;
    }

    private static ClubEntity domain2entity(Club club) {
        ClubEntity entity = new ClubEntity();
        entity.setName(club.getName());
        entity.setCountryIso2(club.getCountry());
        Club.Address addr = club.getAddress();
        if (addr != null) {
            entity.setLatitude(addr.getLat());
            entity.setLongitude(addr.getLon());
            entity.setAddress(addr.getValue());
        }
        return entity;
    }
}
