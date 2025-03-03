package com.homalone.demo.controllers;

import com.homalone.demo.DTO.LoginRequestDTO;
import com.homalone.demo.domain.Users;
import com.homalone.demo.services.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
//@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Users users){

        try {
            Users newUser = userService.registerUser(users);
            return ResponseEntity.ok(newUser);  // 성공 시 저장된 유저 정보 반환
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());  // 중복된 사용자명 처리
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody LoginRequestDTO loginRequestDTO,
                                          HttpServletResponse response) {
        return userService.loginUser(loginRequestDTO.getUsername(), loginRequestDTO.getPassword())
                .map(loginResponseDTO -> {
                    // 쿠키 설정
                    ResponseCookie cookie = ResponseCookie.from("token", loginResponseDTO.getToken())
                            .httpOnly(true)
                            .secure(false)
                            .sameSite("Strict")
                            .path("/")
                            .maxAge(60 * 60)
                            .build();
                    response.addHeader("Set-Cookie", cookie.toString());

                    // 바디 없이 200 OK
                    return ResponseEntity.ok("인증 완료");
                })
                .orElseThrow(() -> new IllegalArgumentException("로그인 실패"));
    }

}
