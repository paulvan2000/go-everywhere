package com.example.goeverywhere;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Empty;
import dagger.hilt.android.AndroidEntryPoint;
import io.grpc.ManagedChannel;
import org.example.goeverywhere.protocol.grpc.LoginRequest;
import org.example.goeverywhere.protocol.grpc.LoginResponse;
import org.example.goeverywhere.protocol.grpc.SignUpRequest;
import org.example.goeverywhere.protocol.grpc.UserServiceGrpc;
import org.example.goeverywhere.protocol.grpc.UserType;

import javax.inject.Inject;
import java.util.concurrent.ExecutionException;

import static org.example.goeverywhere.protocol.grpc.UserType.DRIVER;
import static org.example.goeverywhere.protocol.grpc.UserType.RIDER;

@AndroidEntryPoint
public class SignupActivity extends AppCompatActivity {

    EditText signupName, signupEmail, signupPassword;
    TextView loginRedirectText;
    Button signupButton;
    RadioGroup roleRadioGroup;

    @Inject
    ManagedChannel managedChannel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);


        signupName = findViewById(R.id.signup_name);
        signupEmail = findViewById(R.id.signup_email);
        signupPassword = findViewById(R.id.signup_password);
        loginRedirectText = findViewById(R.id.loginRedirectText);
        signupButton = findViewById(R.id.signup_button);
        roleRadioGroup = findViewById(R.id.role_radio_group);

        int selectedRoleId = roleRadioGroup.getCheckedRadioButtonId();
        UserType role = (selectedRoleId == R.id.radio_rider) ? RIDER : DRIVER;

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = signupName.getText().toString().trim();
                String email = signupEmail.getText().toString().trim();
                String password = signupPassword.getText().toString().trim();

                UserServiceGrpc.UserServiceFutureStub asyncUserService = UserServiceGrpc.newFutureStub(managedChannel);

                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(SignupActivity.this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
                    return;
                }

                ListenableFuture<Empty> asyncSignUpResult = asyncUserService.signUp(SignUpRequest.newBuilder()
                        .setUserType(role)
                        .setName(name)
                        .setEmail(email)
                        .setPassword(password)
                        .build());

                asyncSignUpResult.addListener(() -> {
                    try {
                        asyncSignUpResult.get();
                    } catch (Exception e) {
                        Toast.makeText(SignupActivity.this, "Signup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }, Runnable::run); // replace Runnable::run with a custom executor if async execution is needed
            }
        });

        //redirect with text click
        loginRedirectText.setOnClickListener(view -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}
