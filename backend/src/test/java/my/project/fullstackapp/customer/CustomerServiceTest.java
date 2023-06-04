package my.project.fullstackapp.customer;

import my.project.fullstackapp.exception.DuplicateResourceException;
import my.project.fullstackapp.exception.RequestValidationException;
import my.project.fullstackapp.exception.ResourceNotFoundException;
import my.project.fullstackapp.filestorage.FileStorageProperties;
import my.project.fullstackapp.filestorage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    private CustomerService underTest;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    private final CustomerDTOMapper customerDTOMapper = new CustomerDTOMapper();
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private FileStorageProperties fileStorageProperties;
    @Mock
    private MessageSource messageSource;

    private static final String PROFILE_IMAGE_DIRECTORY = "src/test/resources/garbage/static/images/user-%s/profile-image/";
    private static final String PROFILE_IMAGE_NAME = "%s-profile-image%s";
    private static final Random RANDOM = new Random();

    @BeforeEach
    void setUp() {
        underTest = new CustomerService(
                customerRepository,
                customerDTOMapper,
                passwordEncoder,
                fileStorageService,
                messageSource
        );
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
        when(messageSource.getMessage("exception.authentication.emailAlreadyExists", null, Locale.getDefault()))
                .thenReturn("Exception message");

        // When
        assertThatThrownBy(() -> underTest.createCustomer(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Exception message");
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
        when(messageSource.getMessage("exception.customer.notFound", null, Locale.getDefault()))
                .thenReturn("Exception message");

        // When
        // Then
        assertThatThrownBy(() -> underTest.getCustomer(customerId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Exception message");
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
    void willThrowWhenUpdateCustomerNotFound() {
        // Given
        Integer customerId = 10;

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        CustomerUpdateRequest request = new CustomerUpdateRequest(
                "Nikolai1",
                "nikolai1@gmail.com",
                "password",
                27,
                Gender.values()[RANDOM.nextInt(Gender.values().length)]
        );

        when(messageSource.getMessage("exception.customer.notFound", null, Locale.getDefault()))
                .thenReturn("Exception message");

        // When
        assertThatThrownBy(() -> underTest.updateCustomer(customerId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Exception message");

        // Then
        verify(customerRepository).findById(customerId);
        verifyNoMoreInteractions(customerRepository);
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
        when(messageSource.getMessage("exception.authentication.emailAlreadyExists", null, Locale.getDefault()))
                .thenReturn("Exception message");

        // When
        assertThatThrownBy(() -> underTest.updateCustomer(customerId, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Exception message");
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
    void canUpdateOnlyCustomerGender() {
        // Given
        Integer customerId = 10;
        Customer customer = new Customer(
                "Nikolai",
                "nikolai@gmail.com",
                "password",
                27,
                Gender.MALE
        );
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        CustomerUpdateRequest request = new CustomerUpdateRequest(
                null,
                null,
                null,
                null,
                Gender.FEMALE);

        // When
        underTest.updateCustomer(customerId, request);

        // Then
        ArgumentCaptor<Customer> argument = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(argument.capture());
        assertThat(argument.getValue().getName()).isEqualTo(customer.getName());
        assertThat(argument.getValue().getEmail()).isEqualTo(customer.getEmail());
        assertThat(argument.getValue().getPassword()).isEqualTo(customer.getPassword());
        assertThat(argument.getValue().getAge()).isEqualTo(customer.getAge());
        assertThat(argument.getValue().getGender()).isEqualTo(request.gender());
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

        when(messageSource.getMessage("exception.customer.noChanges", null, Locale.getDefault()))
                .thenReturn("Exception message");

        // When
        assertThatThrownBy(() -> underTest.updateCustomer(customerId, request))
                .isInstanceOf(RequestValidationException.class)
                .hasMessage("Exception message");

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
        when(messageSource.getMessage("exception.customer.notFound", null, Locale.getDefault()))
                .thenReturn("Exception message");

        // When
        assertThatThrownBy(() -> underTest.deleteCustomer(customerId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Exception message");

        // Then
        verify(customerRepository, never()).deleteById(customerId);
    }

    @Test
    void updateCustomerProfileImage() throws IOException {
        // Given
        when(fileStorageProperties.getProfileImageDirectory()).thenReturn(PROFILE_IMAGE_DIRECTORY);
        when(fileStorageProperties.getProfileImageName()).thenReturn(PROFILE_IMAGE_NAME);

        Integer customerId = RANDOM.nextInt(1, 1000);
        String name = "Nikolai";
        String email = "nikolai@gmail.com";
        String password = "password";
        Integer age = 27;
        Gender gender = Gender.values()[RANDOM.nextInt(Gender.values().length)];

        Customer customer = new Customer(customerId, name, email, password, age, gender);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        String profileImage =
                fileStorageProperties.getProfileImageDirectory().formatted(customerId) +
                        fileStorageProperties.getProfileImageName().formatted(customerId, ".jpg");
        MultipartFile multipartFile = new MockMultipartFile("file.jpg", "Hello World".getBytes());
        when(fileStorageService.putProfileImage(customerId, multipartFile.getBytes(), multipartFile.getOriginalFilename()))
                .thenReturn(profileImage);

        // When
        underTest.updateCustomerProfileImage(customerId, multipartFile);

        // Then
        ArgumentCaptor<Customer> argument = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(argument.capture());
        assertThat(argument.getValue().getProfileImage()).isEqualTo(profileImage);

        assertThat(argument.getValue().getName()).isEqualTo(name);
        assertThat(argument.getValue().getEmail()).isEqualTo(email);
        assertThat(argument.getValue().getPassword()).isEqualTo(password);
        assertThat(argument.getValue().getAge()).isEqualTo(age);
        assertThat(argument.getValue().getGender()).isEqualTo(gender);
    }

    @Test
    void willThrowWhenUpdateCustomerProfileImageCustomerNotFound() {
        // Given
        Integer customerId = RANDOM.nextInt(1, 1000);
        MultipartFile multipartFile = new MockMultipartFile("file.jpg", "Hello World".getBytes());

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());
        when(messageSource.getMessage("exception.customer.notFound", null, Locale.getDefault()))
                .thenReturn("Exception message");

        // When
        assertThatThrownBy(() -> underTest.updateCustomerProfileImage(customerId, multipartFile))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Exception message");

        // Then
        verify(customerRepository).findById(customerId);
        verifyNoMoreInteractions(customerRepository);
        verifyNoInteractions(fileStorageService);
    }

    @Test
    void willThrowWhenUpdateCustomerProfileImageFileNotFound() throws IOException {
        // Given
        Integer customerId = RANDOM.nextInt(1, 1000);
        String name = "Nikolai";
        String email = "nikolai@gmail.com";
        String password = "password";
        Integer age = 27;
        Gender gender = Gender.values()[RANDOM.nextInt(Gender.values().length)];

        Customer customer = new Customer(customerId, name, email, password, age, gender);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getBytes()).thenThrow(IOException.class);
        when(messageSource.getMessage("exception.customer.profileImage.notUploaded", null, Locale.getDefault()))
                .thenReturn("Exception message");

        // When
        assertThatThrownBy(() -> underTest.updateCustomerProfileImage(customerId, multipartFile))
                .isInstanceOf(RuntimeException.class)
                .hasRootCauseInstanceOf(IOException.class)
                .hasMessage("Exception message");

        // Then
        verify(customerRepository, never()).save(any());
    }

    @Test
    void getCustomerProfileImage() {
        // Given
        Integer customerId = 10;
        String name = "Nikolai";
        String email = "nikolai@gmail.com";
        String password = "password";
        String profileImage = "someImage.jpg";
        Integer age = 28;
        Gender gender = Gender.values()[RANDOM.nextInt(Gender.values().length)];
        byte[] profileImageBytes = "image".getBytes();

        Customer customer =
                new Customer(customerId, name, email, password, age, gender, profileImage);

        CustomerDTO customerDTO =
                new CustomerDTO(customerId, name, email, age, gender, profileImage, List.of("ROLE_USER"));

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        when(fileStorageService.getProfileImage(customerDTO.profileImage())).thenReturn(profileImageBytes);

        // When
        byte[] actualImageBytes = underTest.getCustomerProfileImage(customerId);

        // Then
        assertThat(actualImageBytes).isEqualTo(profileImageBytes);
    }

    @Test
    void willThrowWhenGetCustomerProfileImage() {
        // Given
        Integer customerId = 10;
        String name = "Nikolai";
        String email = "nikolai@gmail.com";
        String password = "password";
        Integer age = 28;
        Gender gender = Gender.values()[RANDOM.nextInt(Gender.values().length)];

        Customer customer =
                new Customer(customerId, name, email, password, age, gender);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(messageSource.getMessage("exception.customer.profileImage.notFound", null, Locale.getDefault()))
                .thenReturn("Exception message");

        // When
        // Then
        assertThatThrownBy(() -> underTest.getCustomerProfileImage(customerId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Exception message");

        verifyNoInteractions(fileStorageService);
    }
}