package com.homalone.demo.services;

import com.homalone.demo.DTO.LoginResponseDTO;
import com.homalone.demo.domain.Role;
import com.homalone.demo.domain.Users;
import com.homalone.demo.repositories.UserRepository;
import com.homalone.demo.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // 회원가입 성공 케이스
    @Test
    public void testRegisterUser_Success() {
        // given
        String username = "testUser";
        String rawPassword = "password";
        String encodedPassword = "encodedPassword";
        String email = "test@example.com";

        Users user = new Users(null, username, rawPassword, email, Role.USER, null, null);
        Users savedUser = new Users(1L, username, encodedPassword, email, Role.USER, null, null);

        when(userRepository.findByName(username)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(Users.class))).thenReturn(savedUser);

        // when
        Users result = userService.registerUser(user);

        // then
        assertNotNull(result);
        assertEquals(encodedPassword, result.getPassword());
        verify(userRepository, times(1)).save(any(Users.class));
    }

    // 이미 존재하는 사용자명으로 회원가입 시도 시 예외 발생
    @Test
    public void testRegisterUser_UsernameExists() {
        // given
        String username = "existingUser";
        String rawPassword = "password";
        String email = "existing@example.com";
        Users user = new Users(null, username, rawPassword, email, Role.USER, null, null);

        when(userRepository.findByName(username)).thenReturn(Optional.of(user));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(user));
        verify(userRepository, never()).save(any(Users.class));
    }

    // 로그인 성공 케이스
    @Test
    public void testLoginUser_Success() {
        // given
        String username = "testUser";
        String rawPassword = "password";
        String encodedPassword = "encodedPassword";
        String token = "jwtToken";

        Users user = new Users(1L, username, encodedPassword, "test@example.com", Role.USER, null, null);
        when(userRepository.findByName(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
        when(jwtTokenProvider.generateToken(username)).thenReturn(token);

        // when
        Optional<LoginResponseDTO> loginResponse = userService.loginUser(username, rawPassword);

        // then
        assertTrue(loginResponse.isPresent());
        assertEquals(token, loginResponse.get().getToken());
    }

    // 로그인 실패 케이스 : 비밀번호 불일치
    @Test
    public void testLoginUser_Failure_IncorrectPassword() {
        // given
        String username = "testUser";
        String rawPassword = "wrongPassword";
        String encodedPassword = "encodedPassword";

        Users user = new Users(1L, username, encodedPassword, "test@example.com", Role.USER, null, null);
        when(userRepository.findByName(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        // when
        Optional<LoginResponseDTO> loginResponse = userService.loginUser(username, rawPassword);

        // then
        assertFalse(loginResponse.isPresent());
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    // 로그인 실패 케이스 : 사용자 없음
    @Test
    public void testLoginUser_Failure_UserNotFound() {
        // given
        String username = "nonExistentUser";
        when(userRepository.findByName(username)).thenReturn(Optional.empty());

        // when
        Optional<LoginResponseDTO> loginResponse = userService.loginUser(username, "anyPassword");

        // then
        assertFalse(loginResponse.isPresent());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    // isUserExist 테스트 : 사용자 존재 시 true 반환
    @Test
    public void testIsUserExist_UserFound() {
        // given
        String username = "testUser";
        Users user = new Users(1L, username, "password", "test@example.com", Role.USER, null, null);
        when(userRepository.findByName(username)).thenReturn(Optional.of(user));

        // when
        boolean exists = userService.isUserExist(username);

        // then
        assertTrue(exists);
        verify(userRepository, times(1)).findByName(username);
    }

    // isUserExist 테스트 : 사용자 미존재 시 false 반환
    @Test
    public void testIsUserExist_UserNotFound() {
        // given
        String username = "nonExistentUser";
        when(userRepository.findByName(username)).thenReturn(Optional.empty());

        // when
        boolean exists = userService.isUserExist(username);

        // then
        assertFalse(exists);
        verify(userRepository, times(1)).findByName(username);
    }
}