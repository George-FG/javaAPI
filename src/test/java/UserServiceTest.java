
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import main.user.User;
import main.user.UserRepository;
import main.user.UserService;

import org.mockito.Mockito;

import java.util.Optional;

public class UserServiceTest {
    static UserRepository mockRepo;
    static PasswordEncoder mockEncoder;
    static UserService userService;

    @BeforeAll
    static void setup() {
        mockRepo = Mockito.mock(UserRepository.class);
        mockEncoder = Mockito.mock(PasswordEncoder.class);
        userService = new UserService(mockRepo, mockEncoder);
    }

    @Test
    void testRegisterUserSuccess() {
        Mockito.when(mockRepo.existsByUsername("alice")).thenReturn(false);
        Mockito.when(mockEncoder.encode("Password1!")).thenReturn("hashed");
        Mockito.when(mockRepo.save(Mockito.any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(mockRepo.findByUsername("alice")).thenReturn(Optional.of(new User("alice", "hashed")));
        Mockito.when(mockEncoder.matches("Password1!", "hashed")).thenReturn(true);
        userService.registerUser("alice", "Password1!");
        Assertions.assertTrue(userService.loginUser("alice", "Password1!"));
    }

    @Test
    void testRegisterDuplicateUserThrows() {
        Mockito.when(mockRepo.existsByUsername("bob")).thenReturn(false).thenReturn(true);
        Mockito.when(mockEncoder.encode("Password1!")).thenReturn("hashed");
        Mockito.when(mockRepo.save(Mockito.any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        userService.registerUser("bob", "Password1!");
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser("bob", "Password1!");
        });
    }

    @Test
    void testRegisterUserWeakPasswordThrows() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser("charlie", "weak");
        });
    }

    @Test
    void testLoginWithCorrectCredentials() {
        Mockito.when(mockRepo.findByUsername("dave")).thenReturn(Optional.of(new User("dave", "hashed")));
        Mockito.when(mockEncoder.matches("Password1!", "hashed")).thenReturn(true);
        Assertions.assertTrue(userService.loginUser("dave", "Password1!"));
    }

    @Test
    void testLoginWithIncorrectCredentials() {
        Mockito.when(mockRepo.findByUsername("eve")).thenReturn(Optional.of(new User("eve", "hashed")));
        Mockito.when(mockEncoder.matches("WrongPass1!", "hashed")).thenReturn(false);
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            userService.loginUser("eve", "WrongPass1!");
        });
    }
}
