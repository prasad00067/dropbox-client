package com.dropbox.dropboxclient.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuth;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;
import com.dropbox.core.v2.users.Name;

@Component
public class DropboxClientAccessor {

    final static private Logger log = LoggerFactory.getLogger(DropboxClientAccessor.class);
    final private String pattern = "MM-dd-yyyy HH:mm:ss";
    final private SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
    private DbxClientV2 clientInstance;
    private DbxWebAuth webAuthInstance;
    private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    /**
     * This method is used to retrieve the accessToken from Dropbox using Dropbox
     * API.
     * 
     * @param appKey
     *            - dropbox user key.
     * @param appSecret
     *            - dropbox user secret.
     * @throws DropboxClientException
     */
    public void getAccessToken(String appKey, String appSecret) {
        final DbxAppInfo appInfo = new DbxAppInfo(appKey, appSecret);
        final DbxRequestConfig requestConfig = new DbxRequestConfig("desktop-client");
        final DbxWebAuth webAuth = this.getWebAuth(requestConfig, appInfo);
        final DbxWebAuth.Request webAuthRequest = DbxWebAuth.newRequestBuilder().withNoRedirect().build();
        final String authorizeUrl = webAuth.authorize(webAuthRequest);
        System.out.format("1. Go to  %s%n", authorizeUrl);
        System.out.println("2. Click \"Allow\" (you might have to log in first).");
        System.out.println("3. Copy the authorization code.");
        System.out.print("Enter the authorization code here: ");

        String code = null;
        try {
            code = this.reader.readLine();
        } catch (IOException e) {
            log.error("Invalid input data: Error :{}", e.getMessage());
        }
        code = code.trim();
        try {
            final DbxAuthFinish authFinish = webAuth.finishFromCode(code);
            log.info("Authorization completed successfully.");
            String accessToken = authFinish.getAccessToken();
            log.info("Your access token: {}", accessToken);
        } catch (DbxException ex) {
            log.error("Error occured while authorizing the request: {}", ex.getMessage());
        }
    }

    /**
     * This method is used to display the user account information.
     * 
     * @param accessToken
     *            - accessToken of the user account
     * @param locale
     *            - user locale
     */
    public void fetchAndDisplayUserInfo(final String accessToken, final String locale) {
        DbxRequestConfig config = DbxRequestConfig.newBuilder("desktop-client").withUserLocale(locale).build();
        DbxClientV2 client = this.getDBXClient(config, accessToken);
        try {
            FullAccount account = client.users().getCurrentAccount();
            Name name = account.getName();
            System.out.println();
            System.out.println("--------------------------------------------------------------------------");
            System.out.format("User ID:       %s%s%n", "", account.getAccountId());
            System.out.format("Display Name:  %s%n", name.getDisplayName());
            System.out.format("Name:          %s %s (%s)%n", name.getGivenName(), name.getSurname(),
                    name.getFamiliarName());
            System.out.format("E-mail:        %s (%s)%n", account.getEmail(),
                    account.getEmailVerified() ? "verified" : "");
            System.out.format("Country:       %s%n", account.getCountry());
            System.out.format("Referral link: %s%n", account.getReferralLink());
            System.out.println("--------------------------------------------------------------------------");
        } catch (DbxException e) {
            log.error("Error occured while geting user information from dropbox api: {}", e.getMessage());
        }

    }

    /**
     * This method is used to retrieve files metadata from dropbox to display.
     * 
     * @param accessToken
     *            - user accessToken to authorize.
     * @param path
     *            - directory or file path.
     * @param locale
     *            - user locale.
     */
    public void retrieveFileInfo(String accessToken, String path, String locale) {
        DbxRequestConfig config = DbxRequestConfig.newBuilder("desktop-client").withUserLocale(locale).build();
        DbxClientV2 client = this.getDBXClient(config, accessToken);
        getDirectoryInformation(path, client);
    }

    /**
     * This method is used to retrieve all the files metadata information from
     * dropbox account.
     * 
     * @param path
     *            - path or directory.
     * @param client
     *            - Dropbox api client instance.
     */
    private void getDirectoryInformation(final String path, final DbxClientV2 client) {
        try {
            Metadata metaInfo = null;
            if (!path.isEmpty()) {
                metaInfo = client.files().getMetadata(path);
            }
            if (metaInfo instanceof FileMetadata) {
                printFileMetadata((FileMetadata) metaInfo);
            } else {
                ListFolderResult result = client.files().listFolder(path);
                while (true) {
                    for (Metadata metadata : result.getEntries()) {
                        if (metadata instanceof FolderMetadata) {
                            System.out.format("%n%s\t\t\t : Dir%n", metadata.getPathDisplay());
                            System.out.print("  ");
                            getDirectoryInformation(metadata.getPathDisplay(), client);
                        } else {
                            printFileMetadata((FileMetadata) metadata);
                        }
                    }
                    if (!result.getHasMore()) {
                        break;
                    }
                    result = client.files().listFolderContinue(result.getCursor());
                }

            }
        } catch (IllegalArgumentException e) {
            log.error("\n Invalid path: {}, please verify and retry again. Cause: {}", path, e.getMessage());
        } catch (DbxException e) {
            log.error("\n Internal error cause:{}", e.getMessage());
        }
    }

    /**
     * This method is used to print the file metadata to the application console.
     * 
     * @param fileMetadata
     *            - file metadata instance.
     */
    private void printFileMetadata(final FileMetadata fileMetadata) {
        try {
            final Path filePath = new File(fileMetadata.getName()).toPath();
            final String mimeType = Files.probeContentType(filePath);
            System.out.format("- /%s \t: file, %s, %s, modified at: \"%s\"%n", fileMetadata.getName(),
                    readableFileSize(fileMetadata.getSize()), mimeType,
                    formatDate(fileMetadata.getClientModified(), ""));
        } catch (IOException e) {
            log.error("Exception occured while retrieving file information. {}", e);
        }
    }

    /**
     * This method is used to convert the bytes into human readable file size.
     * Example: bytes, KB, MB, GB, TB etc.
     * 
     * @param bytes
     *            - no of bytes
     * @return file size in human readable format. Ex: 1KB
     */
    private String readableFileSize(final long bytes) {
        final String s = bytes < 0 ? "-" : "";
        long b = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        return b < 1000L ? bytes + " bytes"
                : b < 999_950L ? String.format("%s%.1f KB", s, b / 1e3)
                        : (b /= 1000) < 999_950L ? String.format("%s%.1f MB", s, b / 1e3)
                                : (b /= 1000) < 999_950L ? String.format("%s%.1f GB", s, b / 1e3)
                                        : (b /= 1000) < 999_950L ? String.format("%s%.1f TB", s, b / 1e3)
                                                : (b /= 1000) < 999_950L ? String.format("%s%.1f PB", s, b / 1e3)
                                                        : String.format("%s%.1f EB", s, b / 1e6);
    }

    /**
     * This method is used to convert the Date object into 'MM-dd-yyyy HH:mm:ss'
     * format.
     * 
     * @param date
     *            - date object
     * @param locale
     *            - user locale
     * @return formated date in string format Ex: 02-09-2020 13:48:26.
     */
    private String formatDate(Date date, String locale) {
        return this.simpleDateFormat.format(date);
    }

    public DbxClientV2 getDBXClient(DbxRequestConfig config, String accessToken) {
        if (this.clientInstance == null) {
            return new DbxClientV2(config, accessToken);
        }
        return this.clientInstance;
    }

    public void setDBXClient(DbxClientV2 client) {
        this.clientInstance = client;
    }

    /**
     * This method is used to create the instance to DbxWebAuth class.
     * 
     * @param requestConfig
     *            - request config instance
     * @param appInfo
     *            - DbxAppInfo instance with user detials
     * @return DbxWebAuth instance.
     */
    public DbxWebAuth getWebAuth(DbxRequestConfig requestConfig, DbxAppInfo appInfo) {
        if (webAuthInstance == null) {
            return new DbxWebAuth(requestConfig, appInfo);
        }
        return webAuthInstance;
    }

    public void setWebAuth(DbxWebAuth webAuth) {
        this.webAuthInstance = webAuth;
    }

    public BufferedReader getReader() {
        return this.reader;
    }

    public void setReader(BufferedReader reader) {
        this.reader = reader;
    }
}
