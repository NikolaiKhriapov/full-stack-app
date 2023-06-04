package my.project.fullstackapp.auth;

import com.github.javafaker.Faker;
import com.github.javafaker.Name;
import my.project.fullstackapp.customer.CustomerDTO;
import my.project.fullstackapp.customer.CustomerRegistrationRequest;
import my.project.fullstackapp.customer.Gender;
import my.project.fullstackapp.jwt.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class AuthenticationControllerTest {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private JwtUtil jwtUtil;

    private final String AUTHENTICATION_PATH = "api/v1/auth";
    private final String CUSTOMERS_PATH = "api/v1/customers";
    private final Faker FAKER = new Faker();
    private final Random RANDOM = new Random();

    @Test
    void login() {
        // create registration request
        Name fakerName = FAKER.name();
        String name = fakerName.fullName();
        String email = fakerName.firstName() + "." + fakerName.lastName() + "@foobar.com";
        Integer age = RANDOM.nextInt(18, 100);
        String password = FAKER.internet().password();
        Gender gender = Gender.values()[RANDOM.nextInt(Gender.values().length)];

        AuthenticationRequest authenticationRequest =
                new AuthenticationRequest(email, password);

        webTestClient.post()
                .uri(AUTHENTICATION_PATH + "/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(authenticationRequest), AuthenticationRequest.class)
                .exchange()
                .expectStatus().isUnauthorized();

        CustomerRegistrationRequest customerRegistrationRequest =
                new CustomerRegistrationRequest(name, email, password, age, gender);

        webTestClient.post()
                .uri(CUSTOMERS_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(customerRegistrationRequest), CustomerRegistrationRequest.class)
                .exchange()
                .expectStatus().isOk();

        EntityExchangeResult<AuthenticationResponse> result = webTestClient.post()
                .uri(AUTHENTICATION_PATH + "/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(authenticationRequest), AuthenticationRequest.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<AuthenticationResponse>() {
                })
                .returnResult();

        String jwtToken = result.getResponseHeaders().get(AUTHORIZATION).get(0);
        CustomerDTO customerDTO = result.getResponseBody().customerDTO();

        assertThat(jwtUtil.isTokenValid(jwtToken, customerDTO.email())).isTrue();

        assertThat(customerDTO.name()).isEqualTo(name);
        assertThat(customerDTO.email()).isEqualTo(email);
        assertThat(customerDTO.age()).isEqualTo(age);
        assertThat(customerDTO.gender()).isEqualTo(gender);
        assertThat(customerDTO.roles()).isEqualTo(List.of("ROLE_USER"));
    }
}
