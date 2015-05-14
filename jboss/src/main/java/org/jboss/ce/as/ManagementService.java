package org.jboss.ce.as;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

import javax.management.MBeanServer;

import org.jboss.as.controller.ModelController;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.server.Services;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistryException;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.kohsuke.MetaInfServices;

/**
 * John Mazz
 * Kabir Khan
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@MetaInfServices
public class ManagementService implements ServiceActivator {
    private static final Logger log = Logger.getLogger(ManagementService.class.getName());

    private static volatile ModelController controller;
    private static volatile ExecutorService executor;

    private static volatile MBeanServer mbeanServer;

    static ModelControllerClient getClient() {
        return controller.createClient(executor);
    }

    static MBeanServer getMBeanServer() {
        return mbeanServer;
    }

    public void activate(ServiceActivatorContext context) throws ServiceRegistryException {
        log.info("[Hook] Activating management service ...");

        final GetManagementService service = new GetManagementService();
        context
            .getServiceTarget()
            .addService(ServiceName.of("management", "client", "getter"), service)
            .addDependency(Services.JBOSS_SERVER_CONTROLLER, ModelController.class, service.modelControllerValue)
            .addDependency(ServiceName.JBOSS.append("mbean", "server"), MBeanServer.class, service.mbeanServerValue)
            .install();
    }

    private class GetManagementService implements Service {
        private InjectedValue<ModelController> modelControllerValue = new InjectedValue<>();
        private InjectedValue<MBeanServer> mbeanServerValue = new InjectedValue<>();

        public Void getValue() throws IllegalStateException, IllegalArgumentException {
            return null;
        }

        public void start(StartContext context) throws StartException {
            executor = Executors.newFixedThreadPool(5, new ThreadFactory() {
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setDaemon(true);
                    t.setName("ManagementServiceModelControllerClientThread");
                    return t;
                }
            });
            controller = modelControllerValue.getValue();

            mbeanServer = mbeanServerValue.getValue();

            log.info("[Hook] Management service started ...");
        }

        public void stop(StopContext context) {
            try {
                executor.shutdownNow();
            } finally {
                executor = null;
                controller = null;
                mbeanServer = null;
            }
        }
    }
}
