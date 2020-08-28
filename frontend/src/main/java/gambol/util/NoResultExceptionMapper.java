package gambol.util;

import java.util.logging.Logger;
import javax.ejb.EJBTransactionRolledbackException;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author osa
 */
@Provider
public class NoResultExceptionMapper implements ExceptionMapper<EJBTransactionRolledbackException> {

    @Inject
    private Logger LOG;

    @Override
    public Response toResponse(EJBTransactionRolledbackException ex) {
        Throwable cause = ex.getCause();
        
        if (cause instanceof RuntimeException) {
            LOG.info(cause.getMessage());
            
            return Response.
                    status(Response.Status.BAD_REQUEST).
                    type("text/plain").
                    entity(cause.getMessage()).
                    build();
        }
        
        LOG.info(ex.getMessage());
        return Response.
                status(Response.Status.NOT_FOUND).
                type("text/plain").
                entity(ex.getMessage()).
                build();
    }
    
}
