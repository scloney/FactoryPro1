package com.plywood.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import com.plywood.model.User;
import com.plywood.repository.UserRepository;

@Controller
public class HomeController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.registration.password:admin123}")
    private String adminRegistrationPassword;


    public HomeController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @GetMapping("/")
    public String home() {
        return "index";
    }
    @GetMapping("/customers")
    public String customers() {
        return "customers";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(@RequestParam String username, @RequestParam String password, @RequestParam String adminPassword) {
        if (!adminRegistrationPassword.equals(adminPassword)) {
            return "redirect:/register?error=invalid_admin";
        }
        if (userRepository.findByUsername(username).isPresent()) {
            return "redirect:/register?error=exists";
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER");
        userRepository.save(user);
        return "redirect:/login?registered=true";
    }
}
