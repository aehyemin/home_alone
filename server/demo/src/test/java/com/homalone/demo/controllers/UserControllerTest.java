package com.homalone.demo.controllers;

import com.homalone.demo.DTO.LoginRequestDTO;
import com.homalone.demo.DTO.LoginResponseDTO;
import com.homalone.demo.domain.Role;
import com.homalone.demo.domain.Users;
import com.homalone.demo.repositories.UserRepository;
import com.homalone.demo.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserControllerTest {

    @Autowired
    private UserController userController;

    @Autowired
    private UserRepository userRepository;

//    @Autowired
//    private PasswordEncoder passwordEncoder; // 보안용 클래스인가

    @Autowired
    private JwtTokenProvider jwtTokenProvider;


    @BeforeEach
    void setUp() {
        // 각 테스트 전 DB 초기화 (테스트 환경에 따라 필요)
        userRepository.deleteAll();
    }

    @Test
    void registerUser() {
        System.out.println("registerUser test start");
        Users users = getUsers("homalone");
        ResponseEntity<?> response =  userController.registerUser(users);

        // 응답 상태 코드가 성공(200 OK)인지 확인
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // 응답 본문이 Users 객체인지 확인
        assertTrue(response.getBody() instanceof Users);
        // 추가적으로, Users 객체 내 필드 값도 검증 가능
        Users newUser = (Users) response.getBody();
        assertEquals("homalone", newUser.getName());
        Optional<Users> foundUser = userRepository.findByName("homalone");

        foundUser.ifPresentOrElse(
                u -> System.out.println("✅ 찾은 유저: " + u),
                () -> System.out.println("❌ 유저를 찾을 수 없습니다.")

        );


    }

    @Test
    void loginUser() {
        System.out.println("loginUser test start");
        // 테스트를 위한 사용자 등록
        Users users = getUsers("login_correct");
        userController.registerUser(users); // DB에 사용자 정보 등록

        LoginRequestDTO loginRequestDTO = new LoginRequestDTO("login_correct", "password123");

        // 1) MockHttpServletResponse 생성
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        // 올바른 사용자명과 비밀번호로 로그인 시도
        ResponseEntity<String> result = userController.loginUser(loginRequestDTO, mockResponse);

        // 응답 상태 코드와 본문 검증
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("인증 완료", result.getBody()); // 컨트롤러에서 보내는 메시지

        // Set-Cookie 헤더에서 JWT 토큰 추출 (예: "token=XYZ; HttpOnly; Path=/; Max-Age=3600")
        String setCookieHeader = mockResponse.getHeader("Set-Cookie");
        assertNotNull(setCookieHeader);
        System.out.println("Set-Cookie: " + setCookieHeader);

        // 토큰 추출 (토큰 값은 "token=" 뒤부터 ";" 이전까지)
        int tokenStartIndex = setCookieHeader.indexOf("token=") + "token=".length();
        int tokenEndIndex = setCookieHeader.indexOf(";", tokenStartIndex);
        String token = setCookieHeader.substring(tokenStartIndex, tokenEndIndex);
        assertNotNull(token);
        System.out.println("추출된 토큰: " + token);

        // JWT 토큰에서 username 추출
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);
        assertEquals("login_correct", extractedUsername);
        System.out.println("토큰에서 추출된 사용자명: " + extractedUsername);
    }

    private static Users getUsers(String login_correct) {
        return new Users(null, login_correct, "password123", "homalone@example.com", Role.USER, null, null);
    }

    @Test
    void loginFailed() {
        System.out.println("loginFailed test start");
        // 테스트를 위한 사용자 등록
        Users users = getUsers("login_fail");
        userController.registerUser(users); // DB에 사용자 정보 등록

        LoginRequestDTO loginRequestDTO = new LoginRequestDTO("non_existent_user", "wrong_password");

        // MockHttpServletResponse 준비
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        // 로그인 실패 시 IllegalArgumentException 예외 발생 확인
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userController.loginUser(loginRequestDTO, mockResponse);
        });
        assertEquals("로그인 실패", exception.getMessage());
        System.out.println("로그인 실패 로직 정상 작동: " + exception.getMessage());
    }
}