package org.jboss.ce.as;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class HookServlet extends HttpServlet {
    private long checkPeriod = 1000L;

    private Client client;

    private synchronized Client getClient() {
        if (client == null) {
            client = new Client();
        }
        return client;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        String cp = config.getInitParameter("check-period");
        if (cp != null) {
            checkPeriod = Long.parseLong(cp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.err.println("HOOOOOOK!");

        while (isInProgress()) {
            try {
                Thread.sleep(checkPeriod);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException(e);
            }
        }
    }

    private boolean isInProgress() {
        int requests = getClient().getActiveRequests();
        //noinspection RedundantIfStatement
        if (requests > 0) {
            return true;
        }

        return false;
    }
}
