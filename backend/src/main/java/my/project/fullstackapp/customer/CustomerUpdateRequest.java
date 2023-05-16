package my.project.fullstackapp.customer;

record CustomerUpdateRequest(
        String name,
        String email,
        Integer age
) {
}
