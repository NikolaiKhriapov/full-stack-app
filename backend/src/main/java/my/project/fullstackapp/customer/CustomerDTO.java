package my.project.fullstackapp.customer;

import java.util.List;

public record CustomerDTO (
        Integer id,
        String name,
        String email,
        Integer age,
        Gender gender,
        String profileImage,
        List<String> roles,
        String username
) {

}