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

@SpringBootTest(webEnvironment = RANDOM_PORT)
class CustomerControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    private final String CUSTOMERS_URI = "api/v1/customers";
    private final Faker FAKER = new Faker();
    private final Random RANDOM = new Random();

//    @Test
//    void getAllCustomers() {
//    }

    @Test
    void registerCustomer() {
        // create registration request
        Name fakerName = FAKER.name();
        String name = fakerName.fullName();
        String email = fakerName.firstName() + "." + fakerName.lastName() + "@foobar.com";
        Integer age = RANDOM.nextInt(18, 100);
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(name, email, age);

        // send a POST request
        webTestClient.post()
                .uri(CUSTOMERS_URI)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), CustomerRegistrationRequest.class)
                .exchange()
                .expectStatus().isOk();

        // get all customers
        List<Customer> allCustomers = webTestClient.get()
                .uri(CUSTOMERS_URI)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(new ParameterizedTypeReference<Customer>() {
                })
                .returnResult()
                .getResponseBody();

        // make sure that customer is present
        Customer expectedCustomer = new Customer(name, email, age);
        assertThat(allCustomers)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .contains(expectedCustomer);

        // get customer by id
        Integer customerId = allCustomers.stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();
        expectedCustomer.setId(customerId);

        webTestClient.get()
                .uri(CUSTOMERS_URI + "/{id}", customerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<Customer>() {
                })
                .isEqualTo(expectedCustomer);
    }

//    @Test
//    void getCustomer() {
//    }

    @Test
    void updateCustomer() {
        // create registration request
        Name fakerName = FAKER.name();
        String name = fakerName.fullName();
        String email = fakerName.firstName() + "." + fakerName.lastName() + "@foobar.com";
        Integer age = RANDOM.nextInt(18, 100);
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(name, email, age);

        // send a POST request
        webTestClient.post()
                .uri(CUSTOMERS_URI)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), CustomerRegistrationRequest.class)
                .exchange()
                .expectStatus().isOk();

        // get all customers
        List<Customer> allCustomers = webTestClient.get()
                .uri(CUSTOMERS_URI)
                .accept(MediaType.APPLICATION_JSON)
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

        // update customer
        Name fakerNameNew = FAKER.name();
        String nameNew = fakerName.fullName();
        String emailNew = fakerName.firstName() + "." + fakerName.lastName() + "@foobar.com";
        Integer ageNew = RANDOM.nextInt(18, 100);
        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest(nameNew, emailNew, ageNew);

        webTestClient.put()
                .uri(CUSTOMERS_URI + "/{id}", customerId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(updateRequest), CustomerUpdateRequest.class)
                .exchange()
                .expectStatus().isOk();

        // get customer by id
        Customer updatedCustomer = webTestClient.get()
                .uri(CUSTOMERS_URI + "/{id}", customerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Customer.class)
                .returnResult()
                .getResponseBody();

        Customer expectedCustomer = new Customer(customerId, nameNew, emailNew, ageNew);
        assertThat(updatedCustomer).isEqualTo(expectedCustomer);
    }

    @Test
    void deleteCustomer() {
        // create registration request
        Name fakerName = FAKER.name();
        String name = fakerName.fullName();
        String email = fakerName.firstName() + "." + fakerName.lastName() + "@foobar.com";
        Integer age = RANDOM.nextInt(18, 100);
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(name, email, age);

        // send a POST request
        webTestClient.post()
                .uri(CUSTOMERS_URI)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), CustomerRegistrationRequest.class)
                .exchange()
                .expectStatus().isOk();

        // get all customers
        List<Customer> allCustomers = webTestClient.get()
                .uri(CUSTOMERS_URI)
                .accept(MediaType.APPLICATION_JSON)
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

        // delete customer
        webTestClient.delete()
                .uri(CUSTOMERS_URI + "/{id}", customerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        // get customer by id
        webTestClient.get()
                .uri(CUSTOMERS_URI + "/{id}", customerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }
}