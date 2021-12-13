package com.test.ldap;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.test.ldap.utils.Config;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;
import java.io.IOException;
import java.net.InetSocketAddress;

public class HTTPServer {
    public static void start() throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(Config.httpPort), 0);
        httpServer.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                try {
                    String[] value = httpExchange.getRequestURI().getQuery().split("=");
                    if((value[0].equals("api"))&&(value[1].length()>0)){
                        try{
                            if(strFind(value[1], new File("./tmp.txt"))){
                                httpExchange.getResponseHeaders().set("Content-type", "text/plain");
                                String response = "yes\n";
                                httpExchange.sendResponseHeaders(200, response.getBytes().length);
                                httpExchange.getResponseBody().write(response.getBytes());
                                httpExchange.close();
                            }else {
                                httpExchange.getResponseHeaders().set("Content-type", "text/plain");
                                String response = "no\n";
                                httpExchange.sendResponseHeaders(200, response.getBytes().length);
                                httpExchange.getResponseBody().write(response.getBytes());
                                httpExchange.close();
                            }
                        } catch(Exception e){
                            httpExchange.close();
                            e.printStackTrace();
                        }
                    }else {
                        httpExchange.close();
                    }
                } catch (Exception e) {
                    /* ignore */
                    httpExchange.close();
                }
            }
        });
            httpServer.setExecutor(null);
            httpServer.start();
            System.out.println("[+] HTTP Server Start Listening on " + Config.httpPort + "...");
        }

    public static boolean strFind(String searchString , File f) {
        boolean result = false;
        Scanner in = null;
        try {
            in = new Scanner(new FileReader(f));
            while(in.hasNextLine() && !result) {
                result = in.nextLine().indexOf(searchString) >= 0;
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        finally {
            try { in.close() ; } catch(Exception e) { /* ignore */ }
        }
        return result;
    }
}
