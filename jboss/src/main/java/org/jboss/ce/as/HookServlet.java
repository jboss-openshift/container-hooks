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
    private long checkPeriod = 1000L; // 1sec
    private long gracePeriod = 2 * 60 * 1000L; // 2min default

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

        String gp = config.getInitParameter("grace-period");
        if (gp != null) {
            gracePeriod = Long.parseLong(gp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log("HOOOOOOK!");

        long time = 0;
        while (isInProgress() && time < gracePeriod) {
            try {
                time += checkPeriod;
                Thread.sleep(checkPeriod);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException(e);
            }
        }
        log(String.format("In-progress check time: %s [%s]", time, gracePeriod));
    }

    private boolean isInProgress() throws IOException {
        long requests = getClient().getActiveRequests();

        log(String.format("Requests # - %s", requests));

        //noinspection RedundantIfStatement
        if (requests > 0) {
            return true;
        }

        return false;
    }
}
