package gambol.web;

import gambol.ejb.App;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Web application lifecycle listener.
 *
 * @author osa
 */
@WebListener 
public class Global implements ServletContextListener {

    @Inject
    private Logger LOG;

    @Inject
    private App app;
        
    @Override
    public void contextInitialized(ServletContextEvent evt) {
        app.sayHello();
        
        
  /*      
        EntityTransaction x = em.getTransaction();
        x.begin();
        try {
            }
            x.commit();
        }
        catch (Exception ex)
        {
            x.rollback();
        }
*/
        app.loadInitialData();
    }

    @Override
    public void contextDestroyed(ServletContextEvent evt) {
    }
}
