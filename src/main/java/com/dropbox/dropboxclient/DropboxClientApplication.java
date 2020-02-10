package com.dropbox.dropboxclient;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.dropbox.dropboxclient.service.DropboxClientAccessor;

/**
 * This application will act as a client to retrieve information from Dropbox.
 */
@SpringBootApplication
public class DropboxClientApplication implements CommandLineRunner {
    final static Logger log = LoggerFactory.getLogger(DropboxClientApplication.class);

    @Autowired
    DropboxClientAccessor requestProcessor;

    public static void main(String[] args) throws IOException {
        SpringApplication app = new SpringApplication(DropboxClientApplication.class);
        // Disabling spring logo to make clean console.
        app.setBannerMode(Banner.Mode.OFF);
        // Disabling spring-boot default logs to make clean console.
        app.setLogStartupInfo(false);
        app.run(args);
    }

    /**
     * This method will parse the user provided command line arguments.
     */
    @Override
    public void run(String... args) throws Exception {
        int length = args.length;
        if (length > 0) {
            String first = args[0];
            switch (first) {
            case "auth":
                switch (length) {
                case 2:
                    log.error("\n Missing mandatory parametes appsecret, please verify.");
                    break;
                case 3:
                    requestProcessor.getAccessToken(args[1], args[2]);
                    break;
                default:
                    log.error("\n Invalid input parameters , please verify and retry.");
                    printHelpMenu();
                }
                break;
            case "info":
                switch (length) {
                case 1:
                    log.error("\n Missing authentication token, please verify and try again.");
                    break;
                case 2:
                    requestProcessor.fetchAndDisplayUserInfo(args[1], "");
                    break;
                case 3:
                    requestProcessor.fetchAndDisplayUserInfo(args[1], args[2]);
                    break;
                default:
                    log.error("\n Invalid input parameters , please verify and retry.");
                    printHelpMenu();
                }
                break;
            case "list":
                switch (length) {
                case 1:
                    log.error("\n Missing authentication token, please verify and try again.");
                    break;
                case 2:
                    requestProcessor.retrieveFileInfo(args[1], "", "");
                    break;
                case 3:
                    requestProcessor.retrieveFileInfo(args[1], args[2], "");
                    break;
                case 4:
                    requestProcessor.retrieveFileInfo(args[1], args[2], args[3]);
                    break;
                default:
                    log.error("\n Invalid input parameters, please verify and try again.");
                    printHelpMenu();
                }
                break;
            default:
                printHelpMenu();
            }
        } else {
            printHelpMenu();
        }

    }

    /**
     * This method prints the help menu on console.
     */
    private void printHelpMenu() {
        log.info("\n");
        log.info("|--------------------------------------------------|");
        log.info("|                   Help Menu                      |");
        log.info("|--------------------------------------------------|\n");
        log.info("auth    --> Authenticates and authorizes the access to Dropbox account \n");
        log.info("            Example:  java -jar dropbox-client.jar auth {appKey} {appSecret} \n");
        log.info("info    --> Retrieves and prints user's account information\n");
        log.info("            Example:  java -jar dropbox-client.jar info {authToken} {locale}\n");
        log.info("list    --> Prints files and folders information for specified path\n");
        log.info("            Example:  java -jar dropbox-client.jar list {dir} {locale}");
    }
}
