package my.project.fullstackapp.customer;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerDAO {

    List<Customer> getAllCustomers();

    Optional<Customer> getCustomerById(Integer id);
}
