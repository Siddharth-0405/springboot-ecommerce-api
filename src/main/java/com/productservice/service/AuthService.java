package com.productservice.service;

import com.productservice.dto.AuthResponse;
import com.productservice.dto.LoginRequest;
import com.productservice.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
