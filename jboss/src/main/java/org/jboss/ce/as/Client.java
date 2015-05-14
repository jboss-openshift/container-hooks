package org.jboss.ce.as;

import org.jboss.as.controller.client.ModelControllerClient;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class Client {
    private ModelControllerClient client;

    public Client() {
        client = ManagementService.getClient();
        if (client == null) {
            throw new IllegalStateException("No management client -- did ManagementService kicked in?!");
        }
    }

    int getActiveRequests() {
        return 0;
    }
}
