package gambol.web;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.yaml.snakeyaml.Yaml;

/**
 * Web application lifecycle listener.
 *
 * @author osa
 */
@WebListener 
public class Global implements ServletContextListener {

    private final static Logger LOG = Logger.getLogger(Global.class.getName());
    
    @Override
    public void contextInitialized(ServletContextEvent evt) {
        InputStream is = getClass().getResourceAsStream("initial-data.yml");
        Map<String, List<Object>> oo = (Map<String, List<Object>>) new Yaml().load(is);
        
        LOG.info(oo.toString());
    }

    @Override
    public void contextDestroyed(ServletContextEvent evt) {
    }
}
