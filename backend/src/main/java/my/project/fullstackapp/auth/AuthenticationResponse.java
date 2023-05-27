package my.project.fullstackapp.auth;

import my.project.fullstackapp.customer.CustomerDTO;

public record AuthenticationResponse (
        String token,
        CustomerDTO customerDTO
) {
}
