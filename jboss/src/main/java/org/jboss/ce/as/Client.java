package org.jboss.ce.as;

import static org.jboss.as.controller.client.helpers.ClientConstants.INCLUDE_RUNTIME;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP_ADDR;
import static org.jboss.as.controller.client.helpers.ClientConstants.READ_RESOURCE_OPERATION;
import static org.jboss.as.controller.client.helpers.ClientConstants.SUBSYSTEM;

import java.io.IOException;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class Client {
    private ModelControllerClient client;

    Client() {
        client = ManagementService.getClient();
        if (client == null) {
            throw new IllegalStateException("No management client -- did ManagementService kicked in?!");
        }
    }

    static ModelNode checkResult(ModelNode result) {
        if (result.hasDefined("outcome") && "success".equals(result.get("outcome").asString())) {
            return result.get("result");
        } else if (result.hasDefined("failure-description")) {
            throw new IllegalStateException(result.get("failure-description").toString());
        } else if (result.hasDefined("domain-failure-description")) {
            throw new IllegalStateException(result.get("domain-failure-description").toString());
        } else if (result.hasDefined("host-failure-descriptions")) {
            throw new IllegalStateException(result.get("host-failure-descriptions").toString());
        } else {
            throw new IllegalStateException(result.get("outcome").asString());
        }
    }

    // TODO -- this is not the right stat ...
    int getActiveRequests() throws IOException {
        ModelNode request = new ModelNode();
        request.get(OP).set(READ_RESOURCE_OPERATION);
        request.get(INCLUDE_RUNTIME).set(true);
        request.get(OP_ADDR).add(SUBSYSTEM, "web");
        request.get(OP_ADDR).add("connector", "http");
        ModelNode response = client.execute(request);
        ModelNode result = checkResult(response);
        return result.get("requestCount").asInt() - 1; // -1, remove our request
    }
}
