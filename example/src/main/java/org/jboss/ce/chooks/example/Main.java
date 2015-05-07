package org.jboss.ce.chooks.example;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.PathHandler;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class Main {
    public static void main(String[] args) {
        Starter starter = new Starter();
        Runtime.getRuntime().addShutdownHook(new Thread(new Stopper(starter)));
        new Thread(starter).start();
    }

    private static class Starter implements Runnable {
        private Undertow undertow;

        public void run() {
            PathHandler rootHandler = new PathHandler();

            HttpHandler hook = new HttpHandler() {
                public void handleRequest(HttpServerExchange exchange) throws Exception {
                    System.out.println("HOOOOOOOOOOK!");
                }
            };
            rootHandler.addExactPath("hook", hook);

            HttpHandler poke = new HttpHandler() {
                public void handleRequest(HttpServerExchange exchange) throws Exception {
                    exchange.startBlocking();
                    exchange.getOutputStream().write("poke".getBytes());
                }
            };
            rootHandler.addExactPath("poke", poke);

            int port = Integer.parseInt(System.getProperty("port", "8080"));
            String host = System.getProperty("host", "0.0.0.0");

            Undertow.Builder builder = Undertow.builder();
            builder.addHttpListener(port, host, rootHandler);
            undertow = builder.build();

            undertow.start();

            System.out.println(String.format("Undertow started [%s:%s]", host, port));
        }
    }

    private static class Stopper implements Runnable {
        private Starter starter;

        public Stopper(Starter starter) {
            this.starter = starter;
        }

        public void run() {
            Undertow ut = starter.undertow;
            if (ut != null) {
                System.out.println("Stopping Undertow ...");
                ut.stop();
            }
        }
    }
}
