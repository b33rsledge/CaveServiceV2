import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * CDT test of the Alfa CaveService API
 * <p>
 * Usage run the test cases
 * <p>
 * Test locally before CDT test
 * docker network create alfa
 * docker run --name alpha-redis --hostname redisdb --network=alfa --rm -d redis redis-server
 * docker run --rm  --network=alfa -p 8080:8080  -ti ricoris/caveservice java -jar /caveservice.jar --spring.redis.host=alpha-redis --spring.redis.port=6379
 * docker run --rm  --network=alfa -p 8080:8080  -ti ricoris/caveservice java -jar /caveservice.jar --spring.redis.host=alpha-redis --spring.redis.port=6379 --spring.profiles.active=fakeStorage
 * <p>
 * Trobleshooting
 * We did update the guild.gradle in integration folder to  1.16.0 due to Illegalstateexception because on some error on Windows 10
 * inttestImplementation 'org.testcontainers:testcontainers:1.16.0'
 */
public class CDTForCaveService {

    public static final int SERVER_PORT = 8080;
    public static final int SERVER_REDIS_PORT = 6379;
    public static final String REDIS_NETWORKNAME = "redisdb";

    public static final String MEMORY_STORAGE = "memoryStorage";

    @Rule
    public GenericContainer alfaCaveService =
            new GenericContainer("b33rsledge/caveservice:v2")
                    .withExposedPorts(SERVER_PORT)
                    .withCommand("java -jar /root/caveservice/caveservice.jar --spring.redis.host=" + REDIS_NETWORKNAME + " --spring.redis.port=" + SERVER_REDIS_PORT + " --storage.room="+ MEMORY_STORAGE);

    private String serverRootUrl;

    @Before
    public void setup() {
        String address = alfaCaveService.getContainerIpAddress();
        Integer port = alfaCaveService.getMappedPort(SERVER_PORT);
        serverRootUrl = "http://" + address + ":" + port + "";

    }

    @Test
    public void shouldGetHttpResponses() {

        HttpResponse<JsonNode> reply =
                Unirest.get(serverRootUrl + "/v2/room/(0,0,0)").asJson();
        assertThat(reply.getStatus(), is(HttpServletResponse.SC_OK));

        reply =
                Unirest.get(serverRootUrl + "/v2/room/(0,1,0)").asJson();
        assertThat(reply.getStatus(), is(HttpServletResponse.SC_OK));

        reply =
                Unirest.get(serverRootUrl + "/v2/room/(9,9,9)").asJson();
        assertThat(reply.getStatus(), is(HttpServletResponse.SC_NOT_FOUND));

    }

    @Test
    public void shouldPostValidDataAndCreatedResponseUnauthorized() {

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json");

        Map<String, Object> fields = new HashMap<>();

        String body = "{\n" +
                "    \"description\":\"Hej\",\n" +
                "    \"creatorId\":\"42\"\n" +
                "}";

        HttpResponse<String> reply =
                Unirest.post(serverRootUrl + "/v2/room/(0,0,0)")
                        .headers(headers)

                        .body(body)
                        .asString();
        assertThat(reply.getStatus(), is(HttpServletResponse.SC_UNAUTHORIZED));
        //assertThat(reply.getStatus(), is(HttpServletResponse.SC_FORBIDDEN));

    }

    @Test
    public void shouldPostValidDataAndCreatedRoomAndGetItBack() {
        // x axis  West -1, East +1
        // y axis North +1, South -1
        // z axis Up +1, Down -1
        String roomDown = "(0,0,-1)";
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json");

        Map<String, Object> fields = new HashMap<>();

        String body = "{\n" +
                "    \"description\":\"Post room a Down\",\n" +
                "    \"creatorId\":\"42\"\n" +
                "}";

        HttpResponse<String> reply =
                Unirest.post(serverRootUrl + "/v2/room/" + roomDown)
                        .headers(headers)

                        .body(body)
                        .asString();
        assertThat(reply.getStatus(), is(HttpServletResponse.SC_CREATED));
        assertTrue(reply.getBody().contains("Post room a Down"));

        //Getting the room back
        HttpResponse<String> reply1 =
                Unirest.get(serverRootUrl + "/v2/room/" + roomDown).asString();
        assertThat(reply1.getStatus(), is(HttpServletResponse.SC_OK));
        assertTrue(reply1.getBody().contains("Post room a Down"));

    }

    @Test
    public void shouldPostValidDataAndCreatedRoomAndUpdateDescriptionAgain() {
        // x axis  West -1, East +1
        // y axis North +1, South -1
        // z axis Up +1, Down -1
        String roomDown = "(0,0,-1)";
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json");

        Map<String, Object> fields = new HashMap<>();

        String body = "{\n" +
                "    \"description\":\"Post room a Down\",\n" +
                "    \"creatorId\":\"42\"\n" +
                "}";

        HttpResponse<String> reply =
                Unirest.post(serverRootUrl + "/v2/room/" + roomDown)
                        .headers(headers)

                        .body(body)
                        .asString();
        assertThat(reply.getStatus(), is(HttpServletResponse.SC_CREATED));
        assertTrue(reply.getBody().contains("Post room a Down"));
        body = "{\n" +
                "    \"description\":\"Fancy Update\",\n" +
                "    \"creatorId\":\"42\"\n" +
                "}";
        HttpResponse<String> reply1 =
                Unirest.put(serverRootUrl + "/v2/room/" + roomDown)
                        .headers(headers)

                        .body(body)
                        .asString();
        assertThat(reply1.getStatus(), is(HttpServletResponse.SC_OK));
        assertTrue(reply1.getBody().contains("Fancy Update"));

        HttpResponse<String> reply2 =
                Unirest.get(serverRootUrl + "/v2/room/" + roomDown + "/exits")
                        .headers(headers)
                        .asString();
        assertFalse(reply2.getBody().contains("DOWN"));
        assertFalse(reply2.getBody().contains("SOUTH"));
        assertFalse(reply2.getBody().contains("NORTH"));
        assertFalse(reply2.getBody().contains("EAST"));
        assertFalse(reply2.getBody().contains("WEST"));
        assertTrue(reply2.getBody().contains("UP"));

    }

    @Test
    public void shouldGetExitsResponseForDefaultRoomZeros() {

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json");

        Map<String, Object> fields = new HashMap<>();


        HttpResponse<String> reply =
                Unirest.get(serverRootUrl + "/v2/room/(0,0,0)/exits")
                        .headers(headers)
                        .asString();
        assertThat(reply.getStatus(), is(HttpServletResponse.SC_OK));
        assertTrue(reply.getBody().contains("NORTH"));
        assertTrue(reply.getBody().contains("EAST"));
        assertTrue(reply.getBody().contains("WEST"));
        assertTrue(reply.getBody().contains("UP"));


    }
}


