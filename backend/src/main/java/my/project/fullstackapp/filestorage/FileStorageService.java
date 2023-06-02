package my.project.fullstackapp.filestorage;

import lombok.RequiredArgsConstructor;
import my.project.fullstackapp.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FileStorageProperties fileStorageProperties;

    public byte[] getProfileImage(String profileImageDirectoryAndName) {
        try {
            return Files.readAllBytes(Path.of(profileImageDirectoryAndName));
        } catch (IOException e) {
            throw new RuntimeException("Cannot read bytes");
        }
    }

    public String putProfileImage(Integer customerId, byte[] fileBytes, String originalFileName) {

        String profileImageDirectory = fileStorageProperties.getProfileImageDirectory().formatted(customerId);
        String profileImageName = fileStorageProperties.getProfileImageName().formatted(customerId, getFileExtension(originalFileName));

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
