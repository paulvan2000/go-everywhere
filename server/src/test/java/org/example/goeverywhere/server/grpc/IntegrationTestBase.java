package org.example.goeverywhere.server.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.example.goeverywhere.protocol.grpc.LoginRequest;
import org.example.goeverywhere.protocol.grpc.LoginResponse;
import org.example.goeverywhere.protocol.grpc.SignUpRequest;
import org.example.goeverywhere.protocol.grpc.UserServiceGrpc;
import org.example.goeverywhere.protocol.grpc.UserType;
import org.example.goeverywhere.server.data.repository.UserRepository;
import org.example.goeverywhere.server.service.SessionStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class IntegrationTestBase {

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            File file = new File("./test_mydatabase.db");
            file.deleteOnExit();
        }));
    }


    @Value("${server.host}")
    private String serverHost;
    @Value("${server.port}")
    private int serverPort;

    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected SessionStore sessionStore;

    protected SignUpRequest signUpRequestRider;
    protected SignUpRequest signUpRequestDriver;

    protected String riderLogin;
    protected String driverLogin;
    protected String password;
    protected String riderSessionId;
    protected String driverSessionId;


    protected UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub;
    protected ManagedChannel channel;

    @BeforeEach
    void setUp() {
        riderLogin = "rider";
        driverLogin = "driver";
        password = "crackme";
        signUpRequestRider = SignUpRequest.newBuilder().setUserType(UserType.RIDER).setEmail(riderLogin).setName("Alice").setPassword(password).build();
        signUpRequestDriver = SignUpRequest.newBuilder().setUserType(UserType.DRIVER).setEmail(driverLogin).setName("Bob").setPassword(password).build();
        channel = ManagedChannelBuilder.forAddress(serverHost, serverPort)
                .usePlaintext()
                .build();
        userServiceBlockingStub = UserServiceGrpc.newBlockingStub(channel);
    }

    protected void signUpRider() {
        if(riderSessionId == null) {
            userServiceBlockingStub.signUp(signUpRequestRider);
        }
    }

    protected void signUpDriver() {
        if(driverSessionId == null) {
            userServiceBlockingStub.signUp(signUpRequestDriver);
        }
    }

    protected LoginResponse riderLogin(){
        var pair = login(riderLogin);
        riderSessionId = pair.getFirst();
        return pair.getSecond();
    }

    protected LoginResponse sellerLogin() {
        var pair = login(driverLogin);
        driverSessionId = pair.getFirst();
        return pair.getSecond();
    }

    private Pair<String, LoginResponse> login(String login) {
        var loginRequest = LoginRequest.newBuilder().setEmail(login).setPassword(password).build();
        var response = userServiceBlockingStub.login(loginRequest);
        var sessionId = response.getSessionId();
        return Pair.of(sessionId, response);
    }

    @AfterEach
    public void cleanUp() throws InterruptedException {
        userRepository.deleteAll();
    }

}
