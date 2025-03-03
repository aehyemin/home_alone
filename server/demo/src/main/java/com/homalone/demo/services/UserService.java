package com.homalone.demo.services;

import com.homalone.demo.DTO.LoginResponseDTO;
import com.homalone.demo.domain.Users;

import java.util.Optional;

public interface UserService {
    Users registerUser(Users users);
    Optional<LoginResponseDTO> loginUser(String username, String password);
    boolean isUserExist(String username);
}
