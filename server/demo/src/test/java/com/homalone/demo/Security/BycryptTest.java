package com.homalone.demo.Security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class BycryptTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void Bcrypt () {

        String rawPassword = "1234";
        String hashed = "$2y$10$1UV0GXMh9dOXvUWVTc2kCOdQtfvZbxsWSxwXpl1.ACwXg2Vv.FgTa";

        // $2y$를 $2a$로 교체해보는 방법도 가능
        boolean matches = passwordEncoder.matches(rawPassword, hashed);
        System.out.println("Matches? " + matches);
    }
}
