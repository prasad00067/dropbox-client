package com.dropbox.dropboxclient;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dropbox.core.DbxApiException;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxWebAuth;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.common.RootInfo;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.GetMetadataErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.DbxUserUsersRequests;
import com.dropbox.core.v2.users.FullAccount;
import com.dropbox.core.v2.users.Name;
import com.dropbox.core.v2.userscommon.AccountType;
import com.dropbox.dropboxclient.service.DropboxClientAccessor;

@RunWith(MockitoJUnitRunner.class)
public class DorpboxClientAccessorTest {
    @InjectMocks
    private DropboxClientAccessor process;
    final static private Logger log = LoggerFactory.getLogger(DropboxClientAccessor.class);
    static PrintStream previousConsole = System.out;
    static ByteArrayOutputStream stream = new ByteArrayOutputStream();

    @BeforeClass
    public static void init() {
        System.setOut(new PrintStream(stream));
    }

    @Test
    public void testGetAccessTokenValid() throws IOException, DbxException {
        log.info("*******Test case Started :  Retrieve access token from drop box api. ******\n");
        DbxWebAuth dbxWebAuth = Mockito.mock(DbxWebAuth.class);
        process.setWebAuth(dbxWebAuth);
        BufferedReader br = Mockito.mock(BufferedReader.class);
        process.setReader(br);
        Mockito.when(br.readLine()).thenReturn("testtoken");
        DbxAuthFinish authFinish = new DbxAuthFinish("testtoken", (long) 123, "refresh", "1234", " ", " ", "");
        Mockito.when(dbxWebAuth.authorize(Mockito.any())).thenReturn("https://test?clientId=xyx&secret=zxy");
        Mockito.when(dbxWebAuth.finishFromCode(Mockito.anyString())).thenReturn(authFinish);
        process.getAccessToken("x1uhyp2m1l3c2aa", "hqz7353au32mh8q");
        Assert.assertTrue("Token retrieved successfully", stream.toString().contains("testtoken"));
        log.info("---------------------Test case finished----------------------------------\n");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAccessTokenInvalid() {
        log.info("*******Test case Started : Retrieve access token from drop box api.******\n");
        log.info(" ");
        process.getAccessToken("test token", "test secret");
        log.info("---------------------Test case finished----------------------------------\n");
    }

    @Test
    public void testFetchAndDisplayUserInfo() throws DbxApiException, DbxException {
        log.info("*******Test case Started :Retrieve user details from drop box api. *******\n");
        DbxClientV2 client = Mockito.mock(DbxClientV2.class);
        DbxUserUsersRequests requestMock = Mockito.mock(DbxUserUsersRequests.class);
        Mockito.when(client.users()).thenReturn(requestMock);
        Name name = new Name("Prasad", "Vakapalli", "Prasad", "Prasad Vakapalli", "PV");
        FullAccount account = new FullAccount("1234567891234567891234567891234567891234", name, "test@gmail.com", true,
                false, "SE", "https://testling/xyz", false, AccountType.BASIC,
                new RootInfo("7062879152", "7062879152"));
        Mockito.when(requestMock.getCurrentAccount()).thenReturn(account);
        process.setDBXClient(client);
        process.fetchAndDisplayUserInfo("test token", "");
        Assert.assertTrue("User details retrieved successfully",
                stream.toString().contains("Display Name:  Prasad Vakapalli"));
        log.info("---------------------Test case finished----------------------------------\n");
    }

    @Test
    public void testFetchAndDisplayUserInfoInvalid() {
        log.info("*******Test case Started : Retrieve user details from drop box api failed.*******\n");
        process.fetchAndDisplayUserInfo("test token", "");
        log.info("---------------------Test case finished----------------------------------\n");
    }

    @Test
    public void testRetrieveFileInfoScenario1() throws GetMetadataErrorException, DbxException, IOException {
        log.info("*******Test case Started : Retrieve files information from drop box account. ****\n");
        DbxClientV2 client = Mockito.mock(DbxClientV2.class);
        DbxUserFilesRequests requests = Mockito.mock(DbxUserFilesRequests.class);
        FileMetadata metadata = Mockito.mock(FileMetadata.class);
        Mockito.when(metadata.getName()).thenReturn("test.pdf");
        Mockito.when(metadata.getSize()).thenReturn((long) 2000);
        Mockito.when(metadata.getClientModified()).thenReturn(new Date());
        Mockito.when(client.files()).thenReturn(requests);
        Mockito.when(requests.getMetadata(Mockito.anyString())).thenReturn(metadata);
        process.setDBXClient(client);
        process.retrieveFileInfo("accesstoken", "test", "test");
        Assert.assertTrue("Retrieve file information", stream.toString().contains("test.pdf"));
        log.info("---------------------Test case finished----------------------------------\n");
    }

    @Test
    public void testRetrieveFileInfoScenario2() throws GetMetadataErrorException, DbxException, IOException {
        log.info("*******Test case Started : Retrieve folders & files information from drop box account.****\n");
        DbxClientV2 client = Mockito.mock(DbxClientV2.class);
        DbxUserFilesRequests requests = Mockito.mock(DbxUserFilesRequests.class);
        FolderMetadata folderMetadata = Mockito.mock(FolderMetadata.class);
        ListFolderResult result = Mockito.mock(ListFolderResult.class);
        Mockito.when(requests.listFolder(Mockito.anyString())).thenReturn(result);
        Mockito.when(folderMetadata.getPathDisplay()).thenReturn("/Test123");
        List<Metadata> folderList = new ArrayList<Metadata>();
        folderList.add(folderMetadata);
        FileMetadata fileMetadata = Mockito.mock(FileMetadata.class);
        List<Metadata> fileList = new ArrayList<Metadata>();
        fileList.add(fileMetadata);
        Mockito.when(result.getEntries()).thenReturn(folderList).thenReturn(fileList);
        Mockito.when(result.getHasMore()).thenReturn(false);
        Mockito.when(fileMetadata.getSize()).thenReturn((long) 2000);
        Mockito.when(fileMetadata.getName()).thenReturn("test.pdf");
        Mockito.when(fileMetadata.getClientModified()).thenReturn(new Date());
        Mockito.when(client.files()).thenReturn(requests);
        process.setDBXClient(client);
        process.retrieveFileInfo("accesstoken", "/Test123", "test");
        Assert.assertTrue("Retrieve file information", stream.toString().contains("/Test123"));
        log.info("---------------------Test case finished----------------------------------\n");

    }

    /**
     * print output to console
     * 
     * @throws IOException
     */
    @AfterClass
    public static void close() throws IOException {
        previousConsole.println(stream.toString());
        stream.close();
        previousConsole.close();
    }
}
