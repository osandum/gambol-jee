package gambol.api;

import gambol.ejb.App;
import gambol.model.SeriesEntity;
import gambol.xml.Series;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import javax.ws.rs.core.Response;

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
