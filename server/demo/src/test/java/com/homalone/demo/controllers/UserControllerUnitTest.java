package com.homalone.demo.controllers;

import com.homalone.demo.DTO.LoginRequestDTO;
import com.homalone.demo.DTO.LoginResponseDTO;
import com.homalone.demo.domain.Role;
import com.homalone.demo.domain.Users;
import com.homalone.demo.services.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class UserControllerUnitTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // registerUser 메소드 단위 테스트 - 성공 케이스
    @Test
    public void testRegisterUser_Success() {
        // given
        Users inputUser = new Users(null, "testUser", "password", "test@example.com", Role.USER, null, null);
        Users savedUser = new Users(1L, "testUser", "encodedPassword", "test@example.com", Role.USER, null, null);
        when(userService.registerUser(inputUser)).thenReturn(savedUser);

        // when
        ResponseEntity<?> response = userController.registerUser(inputUser);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(savedUser, response.getBody());
    }

    // registerUser 메소드 단위 테스트 - 실패 케이스 (이미 존재하는 사용자)
    @Test
    public void testRegisterUser_Failure() {
        // given
        Users inputUser = new Users(null, "existingUser", "password", "existing@example.com", Role.USER, null, null);
        String errorMessage = "Username is already in use";
        when(userService.registerUser(inputUser)).thenThrow(new IllegalArgumentException(errorMessage));

        // when
        ResponseEntity<?> response = userController.registerUser(inputUser);

        // then
        assertEquals(400, response.getStatusCodeValue());
        assertEquals(errorMessage, response.getBody());
    }

    // loginUser 메소드 단위 테스트 - 성공 케이스
    @Test
    public void testLoginUser_Success() {
        // given
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setUsername("testUser");
        loginRequestDTO.setPassword("password");

        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setToken("jwtToken");

        when(userService.loginUser("testUser", "password"))
                .thenReturn(Optional.of(loginResponseDTO));

        // HttpServletResponse는 직접 모의(mock) 처리
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        // when
        ResponseEntity<String> response = userController.loginUser(loginRequestDTO, mockResponse);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("인증 완료", response.getBody());
        // 쿠키를 설정하기 위해 "Set-Cookie" 헤더가 추가되었는지 검증 (토큰 문자열 포함)
        verify(mockResponse, times(1)).addHeader(eq("Set-Cookie"), contains("token=jwtToken"));
    }

    // loginUser 메소드 단위 테스트 - 실패 케이스 (로그인 실패)
    @Test
    public void testLoginUser_Failure() {
        // given
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setUsername("nonExistentUser");
        loginRequestDTO.setPassword("password");

        when(userService.loginUser("nonExistentUser", "password"))
                .thenReturn(Optional.empty());

        HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        // when & then: 로그인 실패 시 IllegalArgumentException("로그인 실패") 발생
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                userController.loginUser(loginRequestDTO, mockResponse)
        );
        assertEquals("로그인 실패", exception.getMessage());
    }
}