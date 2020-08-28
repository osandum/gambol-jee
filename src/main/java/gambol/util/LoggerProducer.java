package gambol.util;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
@Singleton
public class LoggerProducer {

    @Produces
    public Logger produceLogger(InjectionPoint injectionPoint) {
        Bean bean = injectionPoint.getBean();
        Class beanClass = bean.getBeanClass();
        Logger res = LoggerFactory.getLogger(beanClass);
        
        return res;
    }

}