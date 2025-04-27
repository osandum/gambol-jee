package gambol.util;

import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
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