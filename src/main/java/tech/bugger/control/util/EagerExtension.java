package tech.bugger.control.util;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBean;

/**
 * Extension for the {@link Eager} annotation.
 */
public class EagerExtension implements Extension {

    /**
     * List of all {@link Bean}s using the {@link Eager} annotation.
     */
    private final List<Bean<?>> eagerBeansList = new ArrayList<>();

    /**
     * Collects a possible {@link Eager} bean and adds it to the list of managed eager beans.
     *
     * @param event The current process bean event.
     * @param <T>   The type of the process bean.
     */
    public <T> void collect(@Observes final ProcessBean<T> event) {
        if (event.getAnnotated().isAnnotationPresent(Eager.class)
                && event.getAnnotated().isAnnotationPresent(ApplicationScoped.class)) {
            eagerBeansList.add(event.getBean());
        }
    }

    /**
     * Loads all {@link Eager} beans.
     *
     * @param event       The current deployment validation event.
     * @param beanManager The bean manager to use.
     */
    public void load(@Observes final AfterDeploymentValidation event, final BeanManager beanManager) {
        for (Bean<?> bean : eagerBeansList) {
            // note that toString() is important to instantiate the bean
            beanManager.getReference(bean, bean.getBeanClass(), beanManager.createCreationalContext(bean)).toString();
        }
    }

}

// -----------------------------------------------------------------------------
// end of file
// -----------------------------------------------------------------------------
