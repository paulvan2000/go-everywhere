package org.example.goeverywhere.server.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import jakarta.transaction.Transactional;
import org.example.goeverywhere.protocol.grpc.LoginRequest;
import org.example.goeverywhere.protocol.grpc.LoginResponse;
import org.example.goeverywhere.protocol.grpc.UserType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserServiceIntegrationTest extends IntegrationTestBase {

    @Test
    public void testSuccessfulSignUp_driver() {
        signUpDriver();
        var user = userRepository.findByEmail(driverLogin).get();
        assertEquals(driverLogin, user.getEmail());
        assertEquals(UserType.DRIVER, user.getUserType());
        //password is encrypted
        assertNotEquals(password, user.getPassword());
    }

    @Test
    public void testDuplicateLogin() {
        testSuccessfulSignUp_driver();
        assertThrows(StatusRuntimeException.class, () -> userServiceBlockingStub.signUp(signUpRequestDriver));
    }

    @Test
    @Transactional
    public void testSuccessfulSignUp_rider() {
        signUpRider1();
        var user = userRepository.findByEmail(riderLogin1).get();
        assertEquals(riderLogin1, user.getEmail());
        assertEquals(UserType.RIDER, user.getUserType());
        assertNotEquals(password, user.getPassword());
    }

    @Test
    public void testRiderLogin() {
        signUpRider1();
        LoginResponse response = riderLogin1();
        assertNotNull(response.getSessionId());
        var userFromDB = userRepository.findByEmail(riderLogin1).get();
        var userFromSessionStore = sessionStore.getUser(response.getSessionId());
        assertThat(userFromDB)
                .usingRecursiveComparison()
                .isEqualTo(userFromSessionStore);
    }

    @Test
    public void testRiderLoginIncorrectPassword() {
        userRepository.deleteAll();
        signUpRider1();
        var loginRequest = LoginRequest.newBuilder().setEmail(riderLogin1).setPassword("incorrect").build();
        StatusRuntimeException statusRuntimeException = assertThrows(StatusRuntimeException.class, () -> userServiceBlockingStub.login(loginRequest));
        assertEquals(Status.UNAUTHENTICATED.getCode(), statusRuntimeException.getStatus().getCode());
        assertEquals("Invalid credentials", statusRuntimeException.getStatus().getDescription());
    }
}
