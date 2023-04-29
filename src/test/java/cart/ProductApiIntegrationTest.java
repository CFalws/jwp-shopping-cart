package cart;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.util.Map;
import java.util.Map.Entry;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(value = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ProductApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("상품이 정상적으로 추가되었을 때 OK 응답 코드를 반환한다")
    void create() throws JSONException {
        // given
        JSONObject productAddRequest = parseJSON(Map.of("name", "총30자길이의문자열입니다____________________", "image-url", "a".repeat(1000), "price", 0));

        // when
        var response = given().contentType(MediaType.APPLICATION_JSON_VALUE).body(productAddRequest.toString()).when().post("/product").then().extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "", "일이삼사오육칠팔구십일이삼사오육칠팔구십일이삼사오육칠팔구십일"})
    @DisplayName("상품이 추가 실패 때 BAD REQUEST 응답 코드를 반환한다")
    void createFailName(String name) throws JSONException {
        JSONObject productAddRequest = parseJSON(Map.of(
                "name", name,
                "image-url", "url",
                "price", 1000
        ));

        ExtractableResponse<Response> response = given().contentType(MediaType.APPLICATION_JSON_VALUE).body(productAddRequest.toString()).when().post("/product").then().extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("상품이 추가 실패 때 BAD REQUEST 응답 코드를 반환한다")
    void createFailUrl() throws JSONException {
        JSONObject productAddRequest = parseJSON(Map.of(
                "name", "name",
                "image-url", "a".repeat(1001),
                "price", 1000
        ));

        ExtractableResponse<Response> response = given().contentType(MediaType.APPLICATION_JSON_VALUE).body(productAddRequest.toString()).when().post("/product").then().extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 1_000_000_001})
    @DisplayName("상품이 추가 실패 때 BAD REQUEST 응답 코드를 반환한다")
    void createFailPrice(int price) throws JSONException {
        JSONObject productAddRequest = parseJSON(Map.of(
                "name", "name",
                "image-url", "url",
                "price", price
        ));

        ExtractableResponse<Response> response = given().contentType(MediaType.APPLICATION_JSON_VALUE).body(productAddRequest.toString()).when().post("/product").then().extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void update() throws JSONException {
        int updateCount = jdbcTemplate.update(
                "INSERT INTO products (name, image_url, price) VALUES ('에밀', 'emil.png', 1000)");
        assertThat(updateCount).isEqualTo(1);

        JSONObject productUpdateRequest = parseJSON(Map.of(
                "id", 1,
                "name", "도이",
                "image-url", "doy.png",
                "price", 10000
        ));

        ExtractableResponse<Response> response = given().contentType(MediaType.APPLICATION_JSON_VALUE).body(productUpdateRequest.toString()).when().put("/product").then().extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void delete() {
        int updateCount = jdbcTemplate.update(
                "INSERT INTO products (name, image_url, price) VALUES ('에밀', 'emil.png', 1000)");
        assertThat(updateCount).isEqualTo(1);

        ExtractableResponse<Response> response = given().when().delete("/product/1").then().extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    private JSONObject parseJSON(Map<String, Object> parameters) throws JSONException {
        JSONObject parsed = new JSONObject();
        for (Entry<String, Object> entry : parameters.entrySet()) {
            parsed.put(entry.getKey(), entry.getValue());
        }
        return parsed;
    }
}