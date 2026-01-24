package com.example.RestaurantApplication.seed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.RestaurantApplication.module.user.model.User;
import com.example.RestaurantApplication.module.user.model.enums.Role;
import com.example.RestaurantApplication.module.user.repository.UserRepository;

@Component
public class DataSeeder implements CommandLineRunner {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Override
    public void run(String... args) throws Exception {
        // Check if admin user already exists
        if (userRepository.existsByUserName("admin")) {
            System.out.println("Admin user already exists");
            return;
        }
        // Create default admin user
        User adminUser = new User();
        
        adminUser.setUserName("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("admin12345@"));
        adminUser.setRole(Role.ROLE_ADMIN);
        userRepository.save(adminUser);

        System.out.println("Default admin user created");
    }
}
