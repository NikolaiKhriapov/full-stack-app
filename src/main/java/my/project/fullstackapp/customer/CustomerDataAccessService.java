package my.project.fullstackapp.customer;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerDataAccessService implements CustomerDAO {

    private static final List<Customer> customers;

    static {
        customers = new ArrayList<>();
        customers.add(
                new Customer(
                        1,
                        "Alex",
                        "alex@gmail.com",
                        21
                )
        );
        customers.add(
                new Customer(
                        2,
                        "Jamila",
                        "jamila@gmail.com",
                        25
                )
        );
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customers;
    }

    @Override
    public Optional<Customer> getCustomerById(Integer customerId) {
        return customers.stream()
                .filter(oneCustomer -> oneCustomer.getId().equals(customerId))
                .findFirst();
    }
}
