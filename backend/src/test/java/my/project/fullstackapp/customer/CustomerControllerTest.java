package my.project.fullstackapp.customer;

import com.github.javafaker.Faker;
import com.github.javafaker.Name;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class CustomerControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    private final String CUSTOMERS_PATH = "api/v1/customers";
    private final Faker FAKER = new Faker();
    private final Random RANDOM = new Random();

    @Test
    void registerCustomer() {
        // create registration request
        Name fakerName = FAKER.name();
        String name = fakerName.fullName();
        String email = fakerName.firstName() + "." + fakerName.lastName() + "@foobar.com";
        Integer age = RANDOM.nextInt(18, 100);
        String password = FAKER.internet().password();
        Gender gender = Gender.values()[RANDOM.nextInt(Gender.values().length)];
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(name, email, password, age, gender);

        // send a POST request
        String jwtToken = webTestClient.post()
                .uri(CUSTOMERS_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), CustomerRegistrationRequest.class)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Void.class)
                .getResponseHeaders().get(AUTHORIZATION).get(0);

        // get all customers
        List<CustomerDTO> allCustomers = webTestClient.get()
                .uri(CUSTOMERS_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(new ParameterizedTypeReference<CustomerDTO>() {})
                .returnResult()
                .getResponseBody();

        // get customer by id
        Integer customerId = allCustomers.stream()
                .filter(c -> c.email().equals(email))
                .map(CustomerDTO::id)
                .findFirst()
                .orElseThrow();

        // make sure that customer is present
        CustomerDTO expectedCustomer =
                new CustomerDTO(customerId, name, email, age, gender, List.of("ROLE_USER"), email);

        assertThat(allCustomers).contains(expectedCustomer);

        webTestClient.get()
                .uri(CUSTOMERS_PATH + "/{id}", customerId)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<CustomerDTO>() {
                })
                .isEqualTo(expectedCustomer);
    }

    @Test
    void updateCustomer() {
        // create registration request
        Name fakerName = FAKER.name();
        String name = fakerName.fullName();
        String email = fakerName.firstName() + "." + fakerName.lastName() + "@foobar.com";
        String password = FAKER.internet().password();
        Integer age = RANDOM.nextInt(18, 100);
        Gender gender = Gender.values()[RANDOM.nextInt(Gender.values().length)];

        CustomerRegistrationRequest request = new CustomerRegistrationRequest(name, email, password, age, gender);

        // send a POST request
        String jwtToken = webTestClient.post()
                .uri(CUSTOMERS_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), CustomerRegistrationRequest.class)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Void.class)
                .getResponseHeaders().get(AUTHORIZATION).get(0);

        // get all customers
        List<CustomerDTO> allCustomers = webTestClient.get()
                .uri(CUSTOMERS_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(new ParameterizedTypeReference<CustomerDTO>() {})
                .returnResult()
                .getResponseBody();

        // get customer by id
        Integer customerId = allCustomers.stream()
                .filter(c -> c.email().equals(email))
                .map(CustomerDTO::id)
                .findFirst()
                .orElseThrow();

        // update customer
        Name fakerNameNew = FAKER.name();
        String nameNew = fakerNameNew.fullName();
        Integer ageNew = RANDOM.nextInt(18, 100);
        Gender genderNew = Gender.values()[RANDOM.nextInt(Gender.values().length)];

        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest(nameNew, email, password, ageNew, genderNew);

        webTestClient.put()
                .uri(CUSTOMERS_PATH + "/{id}", customerId)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(updateRequest), CustomerUpdateRequest.class)
                .exchange()
                .expectStatus().isOk();

        // get customer by id
        CustomerDTO updatedCustomer = webTestClient.get()
                .uri(CUSTOMERS_PATH + "/{id}", customerId)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerDTO.class)
                .returnResult()
                .getResponseBody();

        CustomerDTO expectedCustomer = new CustomerDTO(customerId, nameNew, email, ageNew, genderNew, List.of("ROLE_USER"), email);
        assertThat(updatedCustomer).isEqualTo(expectedCustomer);
    }

    @Test
    void deleteCustomer() {
        // create registration request
        Name fakerName = FAKER.name();
        String name = fakerName.fullName();
        String email = fakerName.firstName() + "." + fakerName.lastName() + "@foobar.com";
        Integer age = RANDOM.nextInt(18, 100);
        String password = FAKER.internet().password();
        Gender gender = Gender.values()[RANDOM.nextInt(Gender.values().length)];

        CustomerRegistrationRequest request1 = new CustomerRegistrationRequest(name, email, password, age, gender);
        CustomerRegistrationRequest request2 = new CustomerRegistrationRequest(name, email + "?", password, age, gender);

        // send a POST request to create customer 1
        webTestClient.post()
                .uri(CUSTOMERS_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request1), CustomerRegistrationRequest.class)
                .exchange()
                .expectStatus().isOk();

        // send a POST request to create customer 2
        String jwtToken = webTestClient.post()
                .uri(CUSTOMERS_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request2), CustomerRegistrationRequest.class)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Void.class)
                .getResponseHeaders().get(AUTHORIZATION).get(0);

        // get all customers
        List<Customer> allCustomers = webTestClient.get()
                .uri(CUSTOMERS_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(new ParameterizedTypeReference<Customer>() {
                })
                .returnResult()
                .getResponseBody();

        // get customer by id
        Integer customerId = allCustomers.stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();

        // customer 2 deletes customer 1
        webTestClient.delete()
                .uri(CUSTOMERS_PATH + "/{id}", customerId)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk();

        // customer 2 gets customer 1 by id
        webTestClient.get()
                .uri(CUSTOMERS_PATH + "/{id}", customerId)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isNotFound();
    }
}