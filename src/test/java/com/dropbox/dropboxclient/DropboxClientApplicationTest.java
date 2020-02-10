package com.dropbox.dropboxclient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.dropbox.dropboxclient.service.DropboxClientAccessor;

/**
 * Test cases in this are used to just check the functionality and basic
 * execution.
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DropboxClientApplicationTest {

    @InjectMocks
    DropboxClientApplication application;

    @Mock
    DropboxClientAccessor clientAccessor;

    static PrintStream previousConsole = System.out;
    static ByteArrayOutputStream stream = new ByteArrayOutputStream();

    @BeforeClass
    public static void init() {
        System.setOut(new PrintStream(stream));
        System.setProperty("java.awt.headless", "false");
    }

    @Test
    public void testInfoOption() throws Exception {
        String[] args = { "info", "x1uhyp2m1l3c2zahqz7353au32mh7p" };
        application.run(args);
    }

    @Test
    public void testInfoOptionWithLocale() throws Exception {
        String[] args = { "info", "_R4HCO8AMBAAAAAAAAAAIdW-v1FTIGdUV1ja_xZ2WCkX-BxhFa8IuSufQpyHHEz0", "" };
        application.run(args);
    }

    @Test
    public void testAutWithValidOptions() throws Exception {
        String[] args = { "auth", "test", "test" };
        application.run(args);
    }

    @Test
    public void testAuthWithInValidOptions() throws Exception {
        String[] args = { "auth", "test" };
        application.run(args);
        Assert.assertTrue("Invalid options",
                stream.toString().contains("Missing mandatory parametes appsecret, please verify."));
    }

    @Test
    public void testListOption() throws Exception {
        String[] args = { "list", "_R4HCO8AMBAAAAAAAAAAIdW-v1FTIGdUV1ja_xZ2WCkX-BxhFa8IuSufQpyHHEz0" };
        application.run(args);
    }

    @Test
    public void testListOptionWithLocale() throws Exception {
        String[] args = { "list", "_R4HCO8AMBAAAAAAAAAAIdW-v1FTIGdUV1ja_xZ2WCkX-BxhFa8IuSufQpyHHEz0", "" };
        application.run(args);
    }

    @Test
    public void testListOptionWithInvalidParams() throws Exception {
        String[] args = { "list", "_R4HCO8AMBAAAAAAAAAAIdW-v1FTIGdUV1ja_xZ2WCkX-BxhFa8IuSufQpyHHEz0", "", "", "" };
        application.run(args);
        Assert.assertTrue("Invalid options",
                stream.toString().contains("Invalid input parameters, please verify and try again"));
    }

    @AfterClass
    public static void close() throws IOException {
        previousConsole.println(stream.toString());
        stream.close();
        previousConsole.close();
    }
}
