package org.jboss.ce.chooks.valve;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletException;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class RequestsValve extends ValveBase {
    private static final AtomicLong counter = new AtomicLong();

    public static long getCounter() {
        return counter.get();
    }

    public void invoke(Request request, Response response) throws IOException, ServletException {
        counter.incrementAndGet();
        try {
            getNext().invoke(request, response);
        } finally {
            counter.decrementAndGet();
        }
    }
}
