package com.homalone.demo.repositories;

import com.homalone.demo.domain.Role;
import com.homalone.demo.domain.Users;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

@DataJpaTest
class UsersRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByName() {
        System.out.println("UserRepositoryTest Start");
        Users users = new Users(null, "homalone", "password123", "homalone@example.com", Role.USER, null, null);
        userRepository.save(users);
        // When: 저장한 User 조회
        Optional<Users> foundUser = userRepository.findByName("homalone");


        // Then: 조회된 유저 정보 출력A
        foundUser.ifPresentOrElse(
                u -> System.out.println("✅ 찾은 유저: " + u),
                () -> System.out.println("❌ 유저를 찾을 수 없습니다.")

        );

    }
}