package my.project.fullstackapp.filesstorage;

import my.project.fullstackapp.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FilesStorageService {

    private static final String PROFILE_IMAGE_DIRECTORY = "src/main/resources/static/images/user-%s/profile-image/";
    private static final String PROFILE_IMAGE_NAME = "%s-profile-image%s";

    public byte[] getProfileImage(String profileImageDirectoryAndName) {
        try {
            return Files.readAllBytes(Path.of(profileImageDirectoryAndName));
        } catch (IOException e) {
            throw new RuntimeException("Cannot read bytes");
        }
    }

    public String putProfileImage(Integer customerId, byte[] fileBytes, String originalFileName) {

        String profileImageDirectory = PROFILE_IMAGE_DIRECTORY.formatted(customerId);
        String profileImageName = PROFILE_IMAGE_NAME.formatted(customerId, getFileExtension(originalFileName));

        try {
            Files.createDirectories(Path.of(profileImageDirectory));
            Files.write(Path.of(profileImageDirectory + profileImageName), fileBytes);
        } catch (IOException e) {
            throw new ResourceNotFoundException("Profile image not found");
        }

        return profileImageDirectory + profileImageName;
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
