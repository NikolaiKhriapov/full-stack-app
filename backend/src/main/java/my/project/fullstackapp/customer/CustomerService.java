package my.project.fullstackapp.customer;

import lombok.RequiredArgsConstructor;
import my.project.fullstackapp.exception.DuplicateResourceException;
import my.project.fullstackapp.exception.RequestValidationException;
import my.project.fullstackapp.exception.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerDTOMapper customerDTOMapper;
    private final PasswordEncoder passwordEncoder;

    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(customerDTOMapper)
                .collect(Collectors.toList());
    }

    public void createCustomer(CustomerRegistrationRequest customerRegistrationRequest) {
        if (customerRepository.existsCustomerByEmail(customerRegistrationRequest.email())) {
            throw new DuplicateResourceException("Email already taken");
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

    public CustomerDTO getCustomer(Integer customerId) {
        return customerRepository.findById(customerId)
                .map(customerDTOMapper)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer with id [%s] not found".formatted(customerId)
                ));
    }

    public void updateCustomer(Integer customerId, CustomerUpdateRequest customerUpdateRequest) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer with id [%s] not found".formatted(customerId)
                ));

        boolean changes = false;

        if (customerUpdateRequest.name() != null && !customerUpdateRequest.name().equals(customer.getName())) {
            customer.setName(customerUpdateRequest.name());
            changes = true;
        }
        if (customerUpdateRequest.email() != null && !customerUpdateRequest.email().equals(customer.getEmail())) {
            if (customerRepository.existsCustomerByEmail(customerUpdateRequest.email())) {
                throw new DuplicateResourceException("Email already taken");
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
            throw new RequestValidationException("No data changes found");
        }

        customerRepository.save(customer);
    }

    public void deleteCustomer(Integer customerId) {
        if (!customerRepository.existsCustomerById(customerId)) {
            throw new ResourceNotFoundException("Customer with id [%s] not found".formatted(customerId));
        }
        customerRepository.deleteById(customerId);
    }
}
