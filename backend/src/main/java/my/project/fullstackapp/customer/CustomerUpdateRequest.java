package my.project.fullstackapp.customer;

record CustomerUpdateRequest(
        String name,
        String email,
        String password,
        Integer age,
        Gender gender
) {
}
