package com.homalone.demo.services;

import com.homalone.demo.DTO.LoginResponseDTO;
import com.homalone.demo.domain.Role;
import com.homalone.demo.domain.Users;
import com.homalone.demo.repositories.UserRepository;
import com.homalone.demo.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
//@RequiredArgsConstructor // 생성자 알아서 만들어주는 롬복 어노테이션
public class UserServiceImpl implements UserService {
    // 생성자 주입 방식

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // 보안용 클래스인가
    private final JwtTokenProvider jwtTokenProvider;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Users registerUser(Users users) {
        String username = users.getName();
        String password = users.getPassword();
        String email = users.getEmail();


        if (isUserExist(username)) { // 이미 해당 이름이 존재할 경우
            throw new IllegalArgumentException("Username is already in use");
        }

        String encodedPassword = passwordEncoder.encode(password); //비밀번호 해싱 적용
//        String encodedPassword = password; // 우선 해싱하지 않는 코드
        Users newUser = new Users(null, username, encodedPassword, email ,Role.USER, null,null);
        return userRepository.save(newUser);
    }

    @Override
    public Optional<LoginResponseDTO> loginUser(String username, String password) {
        Optional<Users> userOpt = userRepository.findByName(username);
        if (!userOpt.isPresent()) {
            return Optional.empty();
        }
        if (userOpt.isPresent()) {
            Users user = userOpt.get();
            // 비밀번호 확인
            if (passwordEncoder.matches(password, user.getPassword())) {
                // JWT 생성
                String token = jwtTokenProvider.generateToken(username);
                LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
                loginResponseDTO.setToken(token);
//                user.setToken(token);  // 엔티티에 필드 추가 or DTO 사용

                return Optional.of(loginResponseDTO);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean isUserExist(String username) { // 아이디 중복확인 메소드
        return userRepository.findByName(username).isPresent();
    }
}
