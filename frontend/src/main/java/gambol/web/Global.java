package gambol.web;

import gambol.ejb.App;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Web application lifecycle listener.
 *
 * @author osa
 */
@WebListener
public class Global implements ServletContextListener {

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
