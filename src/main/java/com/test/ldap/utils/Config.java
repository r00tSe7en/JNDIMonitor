package com.test.ldap.utils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.UnixStyleUsageFormatter;

public class Config {
    public static String codeBase;
    @Parameter(names = {"-i", "--ip"}, description = "Local ip address ", required = true, order = 1)
    public static String ip = "0.0.0.0";

    @Parameter(names = {"-l", "--ldapPort"}, description = "Ldap bind port", order = 2)
    public static int ldapPort = 1389;

    @Parameter(names = {"-p", "--ApiPort"}, description = "Http api port", order = 3)
    public static int httpPort = 3456;

    @Parameter(names = {"-h", "--help"}, help = true, description = "Show this help")
    private static boolean help = false;

    public static void applyCmdArgs(String[] args) {
        //process cmd args
        JCommander jc = JCommander.newBuilder()
                .addObject(new Config())
                .build();
        try{
            jc.parse(args);
        }catch(Exception e){
            help = true;
        }

        jc.setProgramName("java -jar JNDIMonitor-2.0.1-SNAPSHOT.jar");
        jc.setUsageFormatter(new UnixStyleUsageFormatter(jc));

        if(help) {
            jc.usage(); //if -h specified, show help and exit
            System.exit(0);
        }

        Config.codeBase = "http://" + Config.ip + ":" + Config.httpPort + "/";
    }
}
