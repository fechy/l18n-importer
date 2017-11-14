package org.kingmonkey.tools.l18n;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class Launcher
{
    private static String secretPath = null;
    private static String configPath = null;

    public static void main (String[] args)
    {
        checkArgs(args);

        try {
            GDocHelper gdoc = new GDocHelper(secretPath, configPath);
            gdoc.get();
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    private static void checkArgs(String[] args) {
        if (args.length == 0) {
            printErrorAndExit("Missing options");
        }

        for (String anArg : args) {
            if (anArg.startsWith("--secret=")) {
                secretPath = anArg.replace("--secret=", "");
            } else if (anArg.startsWith("--config")) {
                configPath = anArg.replace("--config=", "");;
            }
        }

        if (configPath == null) {
            printErrorAndExit("Missing config path");
        }

        if (secretPath == null) {
            printErrorAndExit("Missing secret path");
        }

        if (!configPath.endsWith(".json")) {
            printErrorAndExit("The config file should be JSON");
        }

        if (!secretPath.endsWith(".json")) {
            printErrorAndExit("The secret file should be JSON");
        }
    }

    private static void printErrorAndExit(String error) {
        System.out.println(error);
        System.exit(0);
    }
}