package it.pa.tributi.context;

import javax.management.ServiceNotFoundException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class ServiceLocator {
    public static <T> T get(Class<T> type) throws ServiceNotFoundException {
        try {
            String beanName = type.getSimpleName().substring(1);
            String jndi = "java:module/" + beanName;
            return type.cast(new InitialContext().lookup(jndi));
        } catch (NamingException e) {
            throw new ServiceNotFoundException("Non trovato: " + type.getName());
        }
    }
}
