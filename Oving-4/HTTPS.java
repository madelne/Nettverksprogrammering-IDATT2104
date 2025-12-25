import com.sun.net.httpserver.*;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.security.*;

public class HTTPS {

    public static void main(String[] args) {
        try {
            int port = 8443;
            HttpsServer server = HttpsServer.create(new InetSocketAddress(port), 0);

            SSLContext sslContext = SSLContext.getInstance("TLS");

            char[] password = "password".toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            FileInputStream fis = new FileInputStream("keystore.jks");
            ks.load(fis, password);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, password);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            server.setHttpsConfigurator(new HttpsConfigurator(sslContext){
                public void configure(HttpsParameters params){
                    try{
                        SSLContext context = getSSLContext();
                        SSLEngine engine = context.createSSLEngine();
                        params.setNeedClientAuth(false);
                        params.setCipherSuites(engine.getEnabledCipherSuites());
                        params.setProtocols(engine.getEnabledProtocols());
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });

            server.createContext("/", new MyHttpHandler());
            server.setExecutor(null);
            server.start();
            System.out.println("HTTPS Server startet på port " + port);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class MyHttpHandler implements HttpHandler{
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        String request = exchange.getRequestURI().getPath();
        String response;

        if (requestMethod.equalsIgnoreCase("GET")) {
            if (request.equals("/")) {
                response = "<html><body><h1>Home page</h1></body></html>";
            } else if (request.equals("/page1")) {
                response = "<html><body><h1>Page 1</h1></body></html>";
            } else if (request.equals("/page2")) {
                response = "<html><body><h1>Page 2</h1></body></html>";
            } else {
                response = "<html><body><h1>404 - Not Found</h1></body></html>";
            }
        } else {
            response = "<html><body><h1>405 - Method Not Allowed</h1></body></html>";
            exchange.sendResponseHeaders(405, response.length());
        }


        //String response = "<html><body><h1>HTTPS-server!</h1></body></html>";
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, response.length());

        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
