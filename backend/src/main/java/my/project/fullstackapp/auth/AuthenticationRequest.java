package my.project.fullstackapp.auth;

public record AuthenticationRequest(
        String username,
        String password
) {
}
