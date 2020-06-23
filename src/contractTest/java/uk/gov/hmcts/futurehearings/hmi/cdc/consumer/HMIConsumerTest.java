package uk.gov.hmcts.futurehearings.hmi.cdc.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.TreeMap;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.model.RequestResponsePact;
import com.google.common.collect.Maps;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
public class HMIConsumerTest {

    private static final String EMPLOYEE_DETAILS_URL = "/employee";
    private static final String EMPLOYEE_1_DETAILS_URL = "/employee/1";
    private static String ACCESS_TOKEN = "111";

    @Pact(provider = "SandL_API", consumer = "HMI_API")
    public RequestResponsePact executeGetEmployeeDetailsAndGet200
            (PactDslWithProvider builder) {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);

        return builder
                .given("An employee exists")
                .uponReceiving("Provider returns user details to Annotation API")
                .path(EMPLOYEE_DETAILS_URL)
                .method(HttpMethod.GET.toString())
                .query("id=1")
                //.headers(headers)
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .body(createUserDetailsResponse())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeGetEmployeeDetailsAndGet200")
    public void should_get_employee_details_for_given_data(MockServer mockServer) throws JSONException {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);

        String actualResponseBody =
                SerenityRest
                        .given()
                        //.headers(headers)
                        //.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .log().all()
                        .when()
                        .get(mockServer.getUrl() + EMPLOYEE_DETAILS_URL+"?id=1")
                        .then()
                        .statusCode(200)
                        .and()
                        .extract()
                        .body()
                        .asString();

        verifyResponseObject(actualResponseBody);
    }

   @Pact(provider = "SandL_API", consumer = "HMI_API")
    public RequestResponsePact executePostEmployeeDetailsAndReceive200(PactDslWithProvider builder) {

       Map<String, String> headers = Maps.newHashMap();
       headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> params = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        params.put("id", "1");
        params.put("firstName", "Trial Name");
        params.put("lastName", "Trial Surname");
        params.put("email", "pats_john@hotmail.com");
        //params.put("roles", rolesArray);

        return builder
                .given("An employee should be updated", params)
                .uponReceiving("Provider updates the details to SandL")
                .path(EMPLOYEE_1_DETAILS_URL)
                .method(HttpMethod.POST.toString())
                .headers(headers)
                .willRespondWith()
                .status(HttpStatus.CREATED.value())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executePostEmployeeDetailsAndReceive200")
    public void should_post_employee_details_for_an_update(MockServer mockServer) throws JSONException {


        String actualResponseBody =
                SerenityRest
                        .given()
                        //.headers(headers)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .when()
                        .post(mockServer.getUrl() + EMPLOYEE_1_DETAILS_URL)
                        .then()
                        .statusCode(HttpStatus.CREATED.value())
                        .and()
                        .extract()
                        .body()
                        .asString();
    }

    private static void verifyResponseObject(final String actualResponseBody) throws JSONException {

        JSONObject response = new JSONObject(actualResponseBody);
        assertThat(actualResponseBody).isNotNull();
        assertThat(response).hasNoNullFieldsOrProperties();
        assertThat(response.getString("id")).isNotBlank();
        assertEquals(1, response.getInt("id"));
        assertThat(response.getString("firstName")).isNotBlank();
        assertEquals("Lokesh", response.getString("firstName"));
        assertThat(response.getString("lastName")).isNotBlank();
        assertEquals("Gupta", response.getString("lastName"));
        assertThat(response.getString("email")).isNotBlank();
        assertEquals("howtodoinjava@gmail.com", response.getString("email"));

    }


    private PactDslJsonBody createUserDetailsResponse() {

        return new PactDslJsonBody()
                .integerType("id", 1)
                .stringType("firstName", "Lokesh")
                .stringType("lastName", "Gupta")
                .stringType("email", "howtodoinjava@gmail.com");

    }
}
