package my.project.fullstackapp.customer;

import my.project.fullstackapp.exception.DuplicateResourceException;
import my.project.fullstackapp.exception.RequestValidationException;
import my.project.fullstackapp.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    private CustomerService underTest;
//    @Mock
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    private final CustomerDTOMapper customerDTOMapper = new CustomerDTOMapper();

    private static final Random RANDOM = new Random();

    @BeforeEach
    void setUp() {
        underTest = new CustomerService(customerRepository, customerDTOMapper, passwordEncoder);
    }

    @Test
    void getAllCustomers() {
        // When
        underTest.getAllCustomers();

        // Then
        verify(customerRepository).findAll();
    }

    @Test
    void createCustomer() {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "Nikolai",
                "nikolai@gmail.com",
                "password",
                27,
                Gender.values()[RANDOM.nextInt(Gender.values().length)]
        );
        when(customerRepository.existsCustomerByEmail(request.email())).thenReturn(false);

        String passwordHash = "$#JDKFSDSDdaklfjls";
        when(passwordEncoder.encode(request.password())).thenReturn(passwordHash);

        // When
        underTest.createCustomer(request);

        // Then
        ArgumentCaptor<Customer> argument = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(argument.capture());

        assertThat(argument.getValue().getId()).isNull();
        assertThat(argument.getValue().getName()).isEqualTo(request.name());
        assertThat(argument.getValue().getEmail()).isEqualTo(request.email());
        assertThat(argument.getValue().getPassword()).isEqualTo(passwordHash);
        assertThat(argument.getValue().getAge()).isEqualTo(request.age());
        assertThat(argument.getValue().getGender()).isEqualTo(request.gender());
    }

    @Test
    void willThrowExceptionWhenEmailExistsWhileCreateCustomer() {
        // Given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "Nikolai",
                "nikolai@gmail.com",
                "password",
                27,
                Gender.values()[RANDOM.nextInt(Gender.values().length)]
        );
        when(customerRepository.existsCustomerByEmail(request.email())).thenReturn(true);

        // When
        assertThatThrownBy(() -> underTest.createCustomer(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email already taken");
        // Then
        verify(customerRepository, never()).save(any());
    }

    @Test
    void getCustomer() {
        // Given
        Integer customerId = 10;
        Customer customer = new Customer(
                customerId,
                "Nikolai",
                "nikolai@gmail.com",
                "password",
                28,
                Gender.values()[RANDOM.nextInt(Gender.values().length)]
        );
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        CustomerDTO expected = customerDTOMapper.apply(customer);

        // When
        CustomerDTO actual = underTest.getCustomer(10);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void willThrowExceptionWhenGetCustomerReturnsEmptyOptional() {
        // Given
        Integer customerId = 10;
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // When
        // Then
        assertThatThrownBy(() -> underTest.getCustomer(customerId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer with id [%s] not found".formatted(customerId));
    }

    @Test
    void canUpdateAllCustomerFields() {
        // Given
        Integer customerId = 10;
        Customer customer = new Customer(
                "Nikolai",
                "nikolai@gmail.com",
                "password",
                27,
                Gender.values()[RANDOM.nextInt(Gender.values().length)]
        );
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        CustomerUpdateRequest request = new CustomerUpdateRequest(
                "Nikolai1",
                "nikolai1@gmail.com",
                "password",
                27,
                Gender.values()[RANDOM.nextInt(Gender.values().length)]
        );
        when(customerRepository.existsCustomerByEmail(request.email())).thenReturn(false);

        // When
        underTest.updateCustomer(customerId, request);

        // Then
        ArgumentCaptor<Customer> argument = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(argument.capture());
        assertThat(argument.getValue().getName()).isEqualTo(request.name());
        assertThat(argument.getValue().getEmail()).isEqualTo(request.email());
        assertThat(argument.getValue().getPassword()).isEqualTo(request.password());
        assertThat(argument.getValue().getAge()).isEqualTo(request.age());
        assertThat(argument.getValue().getGender()).isEqualTo(request.gender());
    }

    @Test
    void willThrowExceptionWhenEmailExistsWhileUpdateCustomer() {
        // Given
        Integer customerId = 10;
        Customer customer = new Customer(
                "Nikolai",
                "nikolai@gmail.com",
                "password",
                27,
                Gender.values()[RANDOM.nextInt(Gender.values().length)]
        );
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        CustomerUpdateRequest request = new CustomerUpdateRequest(
                null,
                "nikolai1@gmail.com",
                null,
                null,
                null);
        when(customerRepository.existsCustomerByEmail(request.email())).thenReturn(true);

        // When
        assertThatThrownBy(() -> underTest.updateCustomer(customerId, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email already taken");
        // Then
        verify(customerRepository, never()).save(any());
    }

    @Test
    void canUpdateOnlyCustomerName() {
        // Given
        Integer customerId = 10;
        Customer customer = new Customer(
                "Nikolai",
                "nikolai@gmail.com",
                "password",
                27,
                Gender.values()[RANDOM.nextInt(Gender.values().length)]
        );
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        CustomerUpdateRequest request = new CustomerUpdateRequest(
                "Nikolai1",
                null,
                null,
                null,
                null
        );

        // When
        underTest.updateCustomer(customerId, request);

        // Then
        ArgumentCaptor<Customer> argument = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(argument.capture());
        assertThat(argument.getValue().getName()).isEqualTo(request.name());
        assertThat(argument.getValue().getEmail()).isEqualTo(customer.getEmail());
        assertThat(argument.getValue().getPassword()).isEqualTo(customer.getPassword());
        assertThat(argument.getValue().getAge()).isEqualTo(customer.getAge());
        assertThat(argument.getValue().getGender()).isEqualTo(customer.getGender());
    }

    @Test
    void canUpdateOnlyCustomerEmail() {
        // Given
        Integer customerId = 10;
        Customer customer = new Customer(
                "Nikolai",
                "nikolai@gmail.com",
                "password",
                27,
                Gender.values()[RANDOM.nextInt(Gender.values().length)]
        );
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        CustomerUpdateRequest request = new CustomerUpdateRequest(
                null,
                "nikolai1@gmail.com",
                null,
                null,
                null
        );
        when(customerRepository.existsCustomerByEmail(request.email())).thenReturn(false);

        // When
        underTest.updateCustomer(customerId, request);

        // Then
        ArgumentCaptor<Customer> argument = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(argument.capture());
        assertThat(argument.getValue().getName()).isEqualTo(customer.getName());
        assertThat(argument.getValue().getEmail()).isEqualTo(request.email());
        assertThat(argument.getValue().getPassword()).isEqualTo(customer.getPassword());
        assertThat(argument.getValue().getAge()).isEqualTo(customer.getAge());
        assertThat(argument.getValue().getGender()).isEqualTo(customer.getGender());
    }

    @Test
    void canUpdateOnlyCustomerAge() {
        // Given
        Integer customerId = 10;
        Customer customer = new Customer(
                "Nikolai",
                "nikolai@gmail.com",
                "password",
                27,
                Gender.values()[RANDOM.nextInt(Gender.values().length)]
        );
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        CustomerUpdateRequest request = new CustomerUpdateRequest(
                null,
                null,
                null,
                30,
                null);

        // When
        underTest.updateCustomer(customerId, request);

        // Then
        ArgumentCaptor<Customer> argument = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(argument.capture());
        assertThat(argument.getValue().getName()).isEqualTo(customer.getName());
        assertThat(argument.getValue().getEmail()).isEqualTo(customer.getEmail());
        assertThat(argument.getValue().getPassword()).isEqualTo(customer.getPassword());
        assertThat(argument.getValue().getAge()).isEqualTo(request.age());
        assertThat(argument.getValue().getGender()).isEqualTo(customer.getGender());
    }

    @Test
    void willThrowExceptionWhenNoChangesWhileUpdateCustomer() {
        // Given
        Integer customerId = 10;
        Customer customer = new Customer(
                "Nikolai",
                "nikolai@gmail.com",
                "password",
                27,
                Gender.values()[RANDOM.nextInt(Gender.values().length)]
        );
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        CustomerUpdateRequest request = new CustomerUpdateRequest(
                customer.getName(),
                customer.getEmail(),
                customer.getPassword(),
                customer.getAge(),
                customer.getGender()
        );

        // When
        assertThatThrownBy(() -> underTest.updateCustomer(customerId, request))
                .isInstanceOf(RequestValidationException.class)
                .hasMessage("No data changes found");

        // Then
        verify(customerRepository, never()).save(any());
    }

    @Test
    void deleteCustomer() {
        // Given
        Integer customerId = 1;
        when(customerRepository.existsCustomerById(customerId)).thenReturn(true);

        // When
        underTest.deleteCustomer(customerId);

        // Then
        verify(customerRepository).deleteById(customerId);
    }

    @Test
    void willThrowExceptionWhenDeleteCustomerIfNotExists() {
        // Given
        Integer customerId = 1;
        when(customerRepository.existsCustomerById(customerId)).thenReturn(false);

        // When
        assertThatThrownBy(() -> underTest.deleteCustomer(customerId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer with id [%s] not found".formatted(customerId));

        // Then
        verify(customerRepository, never()).deleteById(customerId);
    }
}