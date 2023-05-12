package my.project.fullstackapp.customer;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    public final CustomerDAO customerDAO;

    public CustomerService(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    public List<Customer> getAllCustomers() {
        return customerDAO.getAllCustomers();
    }

    public Customer getCustomer(Integer customerId) {
        return customerDAO.getCustomerById(customerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Customer with id [%s] not found".formatted(customerId)
                ));
    }
}
