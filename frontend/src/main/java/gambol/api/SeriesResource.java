package gambol.api;

import gambol.ejb.App;
import gambol.model.SeriesEntity;
import gambol.xml.Series;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;
import jakarta.ws.rs.core.Response;

/**
 * @author osa
 */
@RequestScoped
@Path("series/{slug}")
public class SeriesResource {

    @PathParam("slug")
    String slug;

    @EJB
    App gambol;

    @GET
    @Produces({APPLICATION_JSON, APPLICATION_XML})
    public Series getSeries() {
        for (SeriesEntity c : gambol.getSeries())
            if (slug.equals(c.getSlug()))
                return entity2domain(c);

        throw new WebApplicationException("Not found", Response.Status.NOT_FOUND);
    }

    @PUT
    @Consumes({APPLICATION_JSON, APPLICATION_XML})
    public Response putSeries(Series s) {
        gambol.updateOrCreateSeries(slug, domain2entity(s));
        return Response.noContent().build();
    }

    public static Series entity2domain(SeriesEntity entity) {
        Series s = new Series();
        s.setSlug(entity.getSlug());
        s.setName(entity.getName());
        
        return s;
    }

    private static SeriesEntity domain2entity(Series s) {
        SeriesEntity entity = new SeriesEntity();
        entity.setName(s.getName());
        return entity;
    }
    
}
