package gambol.util;

import org.slf4j.Logger;
import jakarta.ejb.EJBTransactionRolledbackException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.LoggerFactory;

/**
 *
 * @author osa
 */
@Provider
public class NoResultExceptionMapper implements ExceptionMapper<EJBTransactionRolledbackException> {

    private static final Logger LOG = LoggerFactory.getLogger(NoResultExceptionMapper.class);

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
