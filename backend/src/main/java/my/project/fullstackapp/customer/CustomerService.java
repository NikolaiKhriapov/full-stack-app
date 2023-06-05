package my.project.fullstackapp.customer;

import lombok.RequiredArgsConstructor;
import my.project.fullstackapp.exception.DuplicateResourceException;
import my.project.fullstackapp.exception.RequestValidationException;
import my.project.fullstackapp.exception.ResourceNotFoundException;
import my.project.fullstackapp.filestorage.FileStorageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerDTOMapper customerDTOMapper;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final MessageSource messageSource;

    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(customerDTOMapper)
                .collect(Collectors.toList());
    }

    public CustomerDTO getCustomer(Integer customerId) {
        return customerRepository.findById(customerId)
                .map(customerDTOMapper)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage(
                        "exception.customer.notFound", null, Locale.getDefault())));
    }

    public void createCustomer(CustomerRegistrationRequest customerRegistrationRequest) {
        if (customerRepository.existsCustomerByEmail(customerRegistrationRequest.email())) {
            throw new DuplicateResourceException(messageSource.getMessage(
                    "exception.authentication.emailAlreadyExists", null, Locale.getDefault()));
        }

        Customer customer = new Customer(
                customerRegistrationRequest.name(),
                customerRegistrationRequest.email(),
                passwordEncoder.encode(customerRegistrationRequest.password()),
                customerRegistrationRequest.age(),
                customerRegistrationRequest.gender()
        );

        customerRepository.save(customer);
    }

    public void updateCustomer(Integer customerId, CustomerUpdateRequest customerUpdateRequest) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage(
                        "exception.customer.notFound", null, Locale.getDefault())));

        boolean changes = false;

        if (customerUpdateRequest.name() != null && !customerUpdateRequest.name().equals(customer.getName())) {
            customer.setName(customerUpdateRequest.name());
            changes = true;
        }
        if (customerUpdateRequest.email() != null && !customerUpdateRequest.email().equals(customer.getEmail())) {
            if (customerRepository.existsCustomerByEmail(customerUpdateRequest.email())) {
                throw new DuplicateResourceException(messageSource.getMessage(
                        "exception.authentication.emailAlreadyExists", null, Locale.getDefault()));
            }
            customer.setEmail(customerUpdateRequest.email());
            changes = true;
        }
        if (customerUpdateRequest.age() != null && !customerUpdateRequest.age().equals(customer.getAge())) {
            customer.setAge(customerUpdateRequest.age());
            changes = true;
        }
        if (customerUpdateRequest.gender() != null && !customerUpdateRequest.gender().equals(customer.getGender())) {
            customer.setGender(customerUpdateRequest.gender());
            changes = true;
        }

        if (!changes) {
            throw new RequestValidationException(messageSource.getMessage(
                    "exception.customer.noChanges", null, Locale.getDefault()));
        }

        customerRepository.save(customer);
    }

    public void deleteCustomer(Integer customerId) {
        checkIfCustomerExistsOrThrow(customerId);
        customerRepository.deleteById(customerId);
    }

    private void checkIfCustomerExistsOrThrow(Integer customerId) {
        if (!customerRepository.existsCustomerById(customerId)) {
            throw new ResourceNotFoundException(messageSource.getMessage(
                    "exception.customer.notFound", null, Locale.getDefault()));
        }
    }

    public byte[] getCustomerProfileImage(Integer customerId) {
        CustomerDTO customerDTO = getCustomer(customerId);

        if (StringUtils.isBlank(customerDTO.profileImage())) {
            throw new ResourceNotFoundException(messageSource.getMessage(
                    "exception.customer.profileImage.notFound", null, Locale.getDefault()));
        }

        return fileStorageService.getProfileImage(customerDTO.profileImage());
    }

    public void updateCustomerProfileImage(Integer customerId, MultipartFile file) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage(
                        "exception.customer.notFound", null, Locale.getDefault())));

        try {
            deleteCustomerProfileImage(customer);
            String profileImage = fileStorageService.putProfileImage(customerId, file.getBytes(), file.getOriginalFilename());
            customer.setProfileImage(profileImage);
        } catch (IOException e) {
            throw new RuntimeException(messageSource.getMessage(
                    "exception.customer.profileImage.notUploaded", null, Locale.getDefault()), e);
        }

        customerRepository.save(customer);
    }

    void deleteCustomerProfileImage(Customer customer) {
        if (customer.getProfileImage() != null) {
            try {
                Path oldCustomerProfileImage = Path.of(customer.getProfileImage());
                Files.delete(oldCustomerProfileImage);
                customer.setProfileImage(null);
            } catch (IOException e) {
                throw new RuntimeException("File not found");
            }
        }
    }
}
