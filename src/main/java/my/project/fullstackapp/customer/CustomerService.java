package my.project.fullstackapp.customer;

import lombok.RequiredArgsConstructor;
import my.project.fullstackapp.exception.DuplicateResourceException;
import my.project.fullstackapp.exception.RequestValidationException;
import my.project.fullstackapp.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    public final CustomerRepository customerRepository;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public void createCustomer(CustomerRegistrationRequest customerRegistrationRequest) {
        if (customerRepository.existsCustomerByEmail(customerRegistrationRequest.email())) {
            throw new DuplicateResourceException("Email already taken");
        }

        Customer customer = new Customer(
                customerRegistrationRequest.name(),
                customerRegistrationRequest.email(),
                customerRegistrationRequest.age()
        );

        customerRepository.save(customer);
    }

    public Customer getCustomer(Integer customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer with id [%s] not found".formatted(customerId)
                ));
    }

    public void updateCustomer(Integer customerId, CustomerUpdateRequest customerUpdateRequest) {
        Customer customer = getCustomer(customerId);

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

        if (!changes) {
            throw new RequestValidationException("No data changes found");
        }

        customerRepository.save(customer);
    }

    public void deleteCustomer(Integer customerId) {
        customerRepository.deleteById(customerId);
    }
}
