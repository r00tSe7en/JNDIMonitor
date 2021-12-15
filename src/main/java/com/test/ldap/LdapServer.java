package com.test.ldap;

import com.test.ldap.controllers.LdapController;
import com.test.ldap.controllers.LdapMapping;
import com.test.ldap.utils.Config;
import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.listener.interceptor.InMemoryInterceptedSearchResult;
import com.unboundid.ldap.listener.interceptor.InMemoryOperationInterceptor;
import org.reflections.Reflections;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

public class LdapServer extends InMemoryOperationInterceptor {

    TreeMap<String, LdapController> routes = new TreeMap<String, LdapController>();

    public static void start() {
        try {
            InMemoryDirectoryServerConfig serverConfig = new InMemoryDirectoryServerConfig("dc=example,dc=com");
            serverConfig.setListenerConfigs(new InMemoryListenerConfig(
                    "listen",
                    InetAddress.getByName("0.0.0.0"),
                    Config.ldapPort,
                    ServerSocketFactory.getDefault(),
                    SocketFactory.getDefault(),
                    (SSLSocketFactory) SSLSocketFactory.getDefault()));

            serverConfig.addInMemoryOperationInterceptor(new LdapServer());
            InMemoryDirectoryServer ds = new InMemoryDirectoryServer(serverConfig);
            ds.startListening();
            System.out.println("[+] LDAP Server Start Listening on " + Config.ldapPort + "...");
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public LdapServer() throws Exception {

        //find all classes annotated with @LdapMapping
        Set<Class<?>> controllers = new Reflections(this.getClass().getPackage().getName())
                .getTypesAnnotatedWith(LdapMapping.class);

        //instantiate them and store in the routes map
        for(Class<?> controller : controllers) {
            Constructor<?> cons = controller.getConstructor();
            LdapController instance = (LdapController) cons.newInstance();
            String[] mappings = controller.getAnnotation(LdapMapping.class).uri();
            for(String mapping : mappings) {
                if(mapping.startsWith("/"))
                    mapping = mapping.substring(1); //remove first forward slash

                routes.put(mapping, instance);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.unboundid.ldap.listener.interceptor.InMemoryOperationInterceptor#processSearchResult(com.unboundid.ldap.listener.interceptor.InMemoryInterceptedSearchResult)
     */
    @Override
    public void processSearchResult(InMemoryInterceptedSearchResult result) {
        String base1 = result.getRequest().getBaseDN();
        //if bas1 in tmp.txt not write
        if(strFind(base1, new File("./tmp.txt"))){
            return;
        }
        String base2 = result.getConnectedAddress();
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss a");
        Date date = new Date();
        String base3 = sdf.format(date);
        String base = base1+','+base2+','+base3;
        if (base.length()>1) {
            System.out.println("[+] Received LDAP Query: " + base);
            String ResultfilePath = "./tmp.txt";
            try {
                FileWriter writer = new FileWriter(ResultfilePath, true);
                writer.write(base + System.getProperty("line.separator"));
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
