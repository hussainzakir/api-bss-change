package com.trinet.restassured;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Properties;

import org.hamcrest.Matchers;
import org.springframework.core.io.ClassPathResource;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/// mvn -e -X -s ~/dev/jenkins-shared-libraries/resources/com/trinet/common/settings.xml -Dbaseuri=https://feature-bnft-10477.api-bss-cicd.eng.houston.trinet-k8s.com -Dauthcookiename=TriNetAuthCookieSLM16 -Dtest=RestAssuredRunner test
public class RestAssuredRunner extends TestCase {

    private Properties properties;
    private final String PROPERTIES_FILE = "restassured.properties";

    // the keys below need to align with the keys in PROPERTIES_FILE referenced
    // above
    private String PROP_KEY_ENDPOINT_AUTH = "endpoint_auth";
    private String PROP_KEY_ENDPOINT_VERSION = "endpoint_version";
    private String PROP_KEY_COMPANY_ID = "company_id";
    private String PROP_KEY_AUTH_EMPLID = "auth_emplid";
    private String PROP_KEY_AUTH_PASSWD = "auth_userpassword";

    public RestAssuredRunner(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(RestAssuredRunner.class);
    }

    @Override
    protected void setUp() throws Exception {
        properties = new Properties();
        properties.load(new ClassPathResource(PROPERTIES_FILE).getInputStream());
    }

    /**
     * Return a live Auth token from ForgeRock to make authenticated requests
     * against microservice endpoints
     */
    public String getAuthCookie() {
        String credentialsBody = "{\"emplid\":\"" + properties.getProperty(PROP_KEY_AUTH_EMPLID)
                + "\",\"userpassword\":\"" + properties.getProperty(PROP_KEY_AUTH_PASSWD) + "\"}";
        RequestSpecification myRequest = RestAssured.given().contentType(ContentType.JSON).body(credentialsBody);
        Response myresponse = myRequest.accept(ContentType.JSON)
                .post(System.getProperty("baseuri") + properties.getProperty(PROP_KEY_ENDPOINT_AUTH));
        return myresponse.asString();
    }

    /**
     * REST Assured test for unauthenticated endpoint
     */
    // @PropertySource("classpath:testenv.properties ")
    public void testVersionEndpoint() {
        String url = System.getProperty("baseuri") + properties.getProperty(PROP_KEY_ENDPOINT_VERSION);
        String authCookie = getAuthCookie();

        System.out.println("url; " + url);
        RequestSpecification myRequest = RestAssured.given().cookie(System.getProperty("authcookiename"), authCookie);

        Response myresponse = myRequest.accept(ContentType.JSON).get(url);
        System.out.println("authCookie:" + authCookie);
        System.out.println("Response:" + myresponse.toString());
        assertThat(myresponse.getStatusCode(), Matchers.equalTo(200));
    }

}