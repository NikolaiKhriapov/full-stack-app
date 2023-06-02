package my.project.fullstackapp.filestorage;

import my.project.fullstackapp.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    private FileStorageService underTest;
    @Mock
    private FileStorageProperties fileStorageProperties;

    private final static String PROFILE_IMAGE_DIRECTORY = "src/test/resources/garbage/static/images/user-%s/profile-image/";
    private final static String PROFILE_IMAGE_NAME = "%s-profile-image%s";
    private static final Random RANDOM = new Random();

    @BeforeEach
    void setUp() {
        underTest = new FileStorageService(fileStorageProperties);
    }

    @Test
    void getProfileImage() throws IOException {
        // Given
        when(fileStorageProperties.getProfileImageDirectory()).thenReturn(PROFILE_IMAGE_DIRECTORY);
        when(fileStorageProperties.getProfileImageName()).thenReturn(PROFILE_IMAGE_NAME);

        Integer id = RANDOM.nextInt(1, 1000);
        String profileImage =
                fileStorageProperties.getProfileImageDirectory().formatted(id) +
                        fileStorageProperties.getProfileImageName().formatted(id, ".jpg");

        byte[] profileImageBytes = profileImage.getBytes();

        Path tempProfileImagePath = Paths.get(profileImage);
        Files.createDirectories(tempProfileImagePath.getParent());
        Files.write(tempProfileImagePath, profileImageBytes);

        // When
        byte[] actual = underTest.getProfileImage(profileImage);

        // Then
        assertThat(actual).isEqualTo(profileImageBytes);
    }

    @Test
    void willThrowExceptionWhenGetProfileImage() {
        // Given
        when(fileStorageProperties.getProfileImageDirectory()).thenReturn(PROFILE_IMAGE_DIRECTORY);
        when(fileStorageProperties.getProfileImageName()).thenReturn(PROFILE_IMAGE_NAME);

        Integer id = RANDOM.nextInt(1, 1000);

        String profileImage =
                fileStorageProperties.getProfileImageDirectory().formatted(id) +
                        fileStorageProperties.getProfileImageName().formatted(id, ".jpg");

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() ->
                    Files.readAllBytes(Path.of(profileImage))).thenThrow(new IOException());

            // When
            // Then
            assertThatThrownBy(() -> underTest.getProfileImage(profileImage))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Cannot read bytes");
        }
    }

    @Test
    void putProfileImage() throws IOException {
        // Given
        when(fileStorageProperties.getProfileImageDirectory()).thenReturn(PROFILE_IMAGE_DIRECTORY);
        when(fileStorageProperties.getProfileImageName()).thenReturn(PROFILE_IMAGE_NAME);

        Integer customerId = RANDOM.nextInt(1, 1000);
        byte[] fileBytes = "This is a test image".getBytes();
        String originalFileName = "test.jpg";

        // When
        String profileImagePath = underTest.putProfileImage(customerId, fileBytes, originalFileName);

        // Then
        String expected =
                fileStorageProperties.getProfileImageDirectory().formatted(customerId) +
                        fileStorageProperties.getProfileImageName().formatted(customerId, ".jpg");
        assertThat(profileImagePath).isEqualTo(expected);
        assertThat(fileBytes).isEqualTo(Files.readAllBytes(Path.of(expected)));
    }

    @Test
    void willThrowExceptionWhenPutProfileImageCreateDirectories() {
        // Given
        when(fileStorageProperties.getProfileImageDirectory()).thenReturn(PROFILE_IMAGE_DIRECTORY);
        when(fileStorageProperties.getProfileImageName()).thenReturn(PROFILE_IMAGE_NAME);

        Integer customerId = RANDOM.nextInt(1, 1000);
        byte[] fileBytes = "This is a test image".getBytes();
        String originalFileName = "test.jpg";

        String profileImageDirectory = fileStorageProperties.getProfileImageDirectory().formatted(customerId);

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() ->
                    Files.createDirectories(Path.of(profileImageDirectory))).thenThrow(new IOException());

            // When
            // Then
            assertThatThrownBy(() -> underTest.putProfileImage(customerId, fileBytes, originalFileName))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Profile image not found");
        }
    }

    @Test
    void willThrowExceptionWhenPutProfileImageWrite() {
        // Given
        when(fileStorageProperties.getProfileImageDirectory()).thenReturn(PROFILE_IMAGE_DIRECTORY);
        when(fileStorageProperties.getProfileImageName()).thenReturn(PROFILE_IMAGE_NAME);

        Integer customerId = RANDOM.nextInt(1, 1000);
        byte[] fileBytes = "This is a test image".getBytes();
        String originalFileName = "test.jpg";

        String profileImageDirectory = fileStorageProperties.getProfileImageDirectory().formatted(customerId);
        String profileImageName = fileStorageProperties.getProfileImageName().formatted(customerId, ".jpg");

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() ->
                    Files.write(Path.of(profileImageDirectory + profileImageName), fileBytes)).thenThrow(new IOException());

            // When
            // Then
            assertThatThrownBy(() -> underTest.putProfileImage(customerId, fileBytes, originalFileName))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Profile image not found");
        }
    }
}