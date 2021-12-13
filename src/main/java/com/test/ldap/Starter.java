package com.test.ldap;

import com.test.ldap.utils.Config;
import java.io.File;
import java.io.IOException;

public class Starter {
    public static void main(String[] args) throws IOException {
        Config.applyCmdArgs(args);
        LdapServer.start();
        HTTPServer.start();
        File f =new File("./tmp.txt");
        if(!f.exists()){
            f.createNewFile();
        }
    }
}
