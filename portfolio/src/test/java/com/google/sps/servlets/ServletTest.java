package com.google.sps.servlets;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import org.junit.After;
import org.junit.Before;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

/**
 * Setups the environment and gives a configuration for
 * request, response, writer and stringWriter, which
 * can be necessary in the testing of servlets.
 * @author Olga Shimanskaia <olgashimanskaia@gmail.com>
 */
public class ServletTest {
    protected final String testUserId = "test_user";
    protected final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig(),
                    new LocalUserServiceTestConfig())
                    .setEnvIsLoggedIn(true)
                    .setEnvEmail("test-user@test.com")
                    .setEnvAuthDomain("test.com")
                    .setEnvAttributes(
                            Collections.singletonMap(
                                    "com.google.appengine.api.users.UserService.user_id_key",
                                    testUserId
                            )
                    );

    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected PrintWriter writer;
    protected StringWriter stringWriter;

    @Before
    public void setUp() throws IOException {
        helper.setUp();

        this.request = mock(HttpServletRequest.class);
        this.response = mock(HttpServletResponse.class);
        this.stringWriter = new StringWriter();
        this.writer = new PrintWriter(this.stringWriter);
        when(this.response.getWriter()).thenReturn(this.writer);
    }

    @After
    public void tearDown() {
        helper.tearDown();
    }
}