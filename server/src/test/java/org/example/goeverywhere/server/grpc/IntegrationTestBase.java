package org.example.goeverywhere.server.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.example.goeverywhere.protocol.grpc.*;
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

    protected SignUpRequest signUpRequestRider1;
    protected SignUpRequest signUpRequestRider2;
    protected SignUpRequest signUpRequestDriver;

    protected String riderLogin1;
    protected String riderLogin2;
    protected String driverLogin;
    protected String password;
    protected String riderSessionId;
    protected String driverSessionId;


    protected UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub;
    protected RiderServiceGrpc.RiderServiceStub riderServiceStub;
    protected DriverServiceGrpc.DriverServiceStub driverServiceStub;
    protected DriverServiceGrpc.DriverServiceBlockingStub driverServiceBlockingStub;

    protected ManagedChannel channel;

    @BeforeEach
    void setUp() {
        riderLogin1 = "rider1";
        riderLogin2 = "rider2";
        driverLogin = "driver";
        password = "crackme";
        signUpRequestRider1 = SignUpRequest.newBuilder().setUserType(UserType.RIDER).setEmail(riderLogin1).setName("Alice").setPassword(password).build();
        signUpRequestRider2 = SignUpRequest.newBuilder().setUserType(UserType.RIDER).setEmail(riderLogin2).setName("Carol").setPassword(password).build();
        signUpRequestDriver = SignUpRequest.newBuilder().setUserType(UserType.DRIVER).setEmail(driverLogin).setName("Bob").setPassword(password).build();
        channel = ManagedChannelBuilder.forAddress(serverHost, serverPort)
                .usePlaintext()
                .build();
        userServiceBlockingStub = UserServiceGrpc.newBlockingStub(channel);
        riderServiceStub = RiderServiceGrpc.newStub(channel);
        driverServiceStub = DriverServiceGrpc.newStub(channel);
        driverServiceBlockingStub = DriverServiceGrpc.newBlockingStub(channel);
    }

    protected void signUpRider1() {
        if(riderSessionId == null) {
            userServiceBlockingStub.signUp(signUpRequestRider1);
        }
    }

    protected void signUpRider2() {
        if(riderSessionId == null) {
            userServiceBlockingStub.signUp(signUpRequestRider2);
        }
    }

    protected void signUpDriver() {
        if(driverSessionId == null) {
            userServiceBlockingStub.signUp(signUpRequestDriver);
        }
    }

    protected LoginResponse riderLogin1(){
        var pair = login(riderLogin1);
        riderSessionId = pair.getFirst();
        return pair.getSecond();
    }

    protected LoginResponse riderLogin2(){
        var pair = login(riderLogin2);
        riderSessionId = pair.getFirst();
        return pair.getSecond();
    }

    protected LoginResponse driverLogin() {
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

    protected static void pause() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    @AfterEach
    public void cleanUp() throws InterruptedException {
        userRepository.deleteAll();
    }

}
