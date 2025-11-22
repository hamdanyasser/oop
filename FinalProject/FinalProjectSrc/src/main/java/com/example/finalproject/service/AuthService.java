package com.example.finalproject.service;

import com.example.finalproject.dao.UserDao;
import com.example.finalproject.model.User;
import com.example.finalproject.security.JwtService;
import com.example.finalproject.security.PasswordHasher;

import java.util.Optional;

public class AuthService {
    private final UserDao userDao = new UserDao();

    // Register new customer
    public String register(String name, String email, String password, String address) throws Exception {
        if (userDao.findByEmail(email).isPresent()) {
            throw new Exception("Email already exists!");
        }

        String hashed = PasswordHasher.hash(password);
        User user = new User(0, name, email, hashed, "CUSTOMER", address);
        userDao.save(user);

        return JwtService.issueToken(user.getId(), user.getRole(), user.getEmail());
    }

    // Login
    public String login(String email, String password) throws Exception {
        Optional<User> optUser = userDao.findByEmail(email);
        if (optUser.isEmpty()) {
            throw new Exception("User not found!");
        }

        User user = optUser.get();
        if (!PasswordHasher.verify(password, user.getPasswordHash())) {
            throw new Exception("Invalid password!");
        }

        return JwtService.issueToken(user.getId(), user.getRole());
    }
}
