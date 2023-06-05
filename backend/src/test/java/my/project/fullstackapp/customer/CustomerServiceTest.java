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
import java.nio.file.Files;
import java.nio.file.Path;
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
    void testGetAllCustomers() {
        // When
        underTest.getAllCustomers();

        // Then
        verify(customerRepository).findAll();
    }

    @Test
    void testGetCustomer() {
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
    void testGetCustomer_Exception_CustomerNotFound() {
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
    void testCreateCustomer() {
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
    void testCreateCustomer_Exception_EmailAlreadyExists() {
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
    void testUpdateCustomer_AllFields() {
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
    void testUpdateCustomer_Exception_CustomerNotFound() {
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
    void testUpdateCustomer_Exception_EmailAlreadyExists() {
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
    void testUpdateCustomer_NameOnly() {
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
    void testUpdateCustomer_EmailOnly() {
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
    void testUpdateCustomer_AgeOnly() {
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
    void testUpdateCustomer_GenderOnly() {
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
    void testUpdateCustomer_NoChangedFields() {
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
    void testDeleteCustomer() {
        // Given
        Integer customerId = 1;
        when(customerRepository.existsCustomerById(customerId)).thenReturn(true);

        // When
        underTest.deleteCustomer(customerId);

        // Then
        verify(customerRepository).deleteById(customerId);
    }

    @Test
    void testDeleteCustomer_Exception_CustomerNotFound() {
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
    void testUpdateCustomerProfileImage() throws IOException {
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
    void testUpdateCustomerProfileImage_Exception_CustomerNotFound() {
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
    void testUpdateCustomerProfileImage_Exception_FileNotFound() throws IOException {
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
    void testGetCustomerProfileImage() {
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
    void testGetCustomerProfileImage_Exception_FileNotFound() {
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

    @Test
    void testDeleteCustomerProfileImage() throws IOException {
        // Given
        Integer customerId = 1;
        String profileImageName =
                PROFILE_IMAGE_DIRECTORY.formatted(customerId) +
                        PROFILE_IMAGE_NAME.formatted(customerId, ".jpg");

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setProfileImage(profileImageName);

        Path profileImagePath = Path.of(profileImageName);
        Files.createDirectories(profileImagePath.getParent());
        Files.write(profileImagePath, "imageBytes".getBytes());

        // When
        underTest.deleteCustomerProfileImage(customer);

        // Then
        assertThat(Files.exists(profileImagePath)).isFalse();
        assertThat(customer.getProfileImage()).isNull();
    }

    @Test
    void testDeleteCustomerProfileImage_Exception_FileNotFound() {
        // Given
        Integer customerId = 1;
        String profileImageName =
                PROFILE_IMAGE_DIRECTORY.formatted(customerId) + PROFILE_IMAGE_NAME.formatted(customerId, ".jpg");

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setProfileImage(profileImageName);

        // When
        // Then
        assertThatThrownBy(() -> underTest.deleteCustomerProfileImage(customer))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("File not found");
    }
}