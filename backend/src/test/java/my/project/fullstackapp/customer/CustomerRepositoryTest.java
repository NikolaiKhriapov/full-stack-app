package my.project.fullstackapp.customer;

import com.github.javafaker.Faker;
import my.project.fullstackapp.AbstractTestcontainersTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomerRepositoryTest extends AbstractTestcontainersTest {

    @Autowired
    private CustomerRepository underTest;

    private static final Faker FAKER = new Faker();
    private static final Random RANDOM = new Random();

    @BeforeEach
    void setUp() {
        underTest.deleteAll();
    }

    @Test
    void testExistsCustomerByEmail() {
        String email = FAKER.internet().safeEmailAddress() + "-" + UUID.randomUUID();
        Customer customer = new Customer(
                FAKER.name().fullName(),
                email,
                FAKER.internet().password(),
                20,
                Gender.values()[RANDOM.nextInt(Gender.values().length)]
        );

        underTest.save(customer);

        var actual = underTest.existsCustomerByEmail(email);

        assertThat(actual).isTrue();
    }

    @Test
    void testExistsCustomerByEmailException() {
        String email = FAKER.internet().safeEmailAddress() + "-" + UUID.randomUUID();

        var actual = underTest.existsCustomerByEmail(email);

        assertThat(actual).isFalse();
    }

    @Test
    void testExistsCustomerById() {
        String email = FAKER.internet().safeEmailAddress() + "-" + UUID.randomUUID();
        Customer customer = new Customer(
                FAKER.name().fullName(),
                email,
                FAKER.internet().password(),
                20,
                Gender.values()[RANDOM.nextInt(Gender.values().length)]
        );

        underTest.save(customer);

        Integer customerId = underTest.findAll()
                .stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();

        var actual = underTest.existsCustomerById(customerId);

        assertThat(actual).isTrue();
    }

    @Test
    void testExistsCustomerByIdException() {
        Integer customerId = -1;

        var actual = underTest.existsCustomerById(customerId);

        assertThat(actual).isFalse();
    }
}