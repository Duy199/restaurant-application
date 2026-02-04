package com.example.RestaurantApplication.seed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.RestaurantApplication.module.role.model.UserRole;
import com.example.RestaurantApplication.module.role.repository.UserRoleRepository;
import com.example.RestaurantApplication.module.user.model.User;
import com.example.RestaurantApplication.module.user.repository.UserRepository;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== Starting Data Seeder ===");

        // Seed roles first (must run before admin account)
        seedAdminRoles();

        // Seed admin account (depends on roles)
        seedAdminAccount();

        System.out.println("=== Data Seeder Completed ===");
    }

    /**
     * Seed the 3 basic roles: ROLE_ADMIN, ROLE_MANAGER, ROLE_STAFF
     * These roles will be configured by admin later via API
     */
    private void seedAdminRoles() {
        System.out.println("Seeding admin roles...");

        // Create ROLE_ADMIN if not exists
        if (!userRoleRepository.existsByName("ROLE_ADMIN")) {
            UserRole adminRole = new UserRole();
            adminRole.setName("ROLE_ADMIN");
            userRoleRepository.save(adminRole);
            System.out.println("✓ Created role: ROLE_ADMIN");
        } else {
            System.out.println("✓ Role ROLE_ADMIN already exists");
        }

        // Create ROLE_MANAGER if not exists
        if (!userRoleRepository.existsByName("ROLE_MANAGER")) {
            UserRole managerRole = new UserRole();
            managerRole.setName("ROLE_MANAGER");
            userRoleRepository.save(managerRole);
            System.out.println("✓ Created role: ROLE_MANAGER");
        } else {
            System.out.println("✓ Role ROLE_MANAGER already exists");
        }

        // Create ROLE_STAFF if not exists
        if (!userRoleRepository.existsByName("ROLE_STAFF")) {
            UserRole staffRole = new UserRole();
            staffRole.setName("ROLE_STAFF");
            userRoleRepository.save(staffRole);
            System.out.println("✓ Created role: ROLE_STAFF");
        } else {
            System.out.println("✓ Role ROLE_STAFF already exists");
        }
    }

    /**
     * Seed the default admin account
     * Username: admin, Password: admin12345@
     * Assigned to ROLE_ADMIN role
     */
    private void seedAdminAccount() {
        System.out.println("Seeding admin account...");

        // Check if admin user already exists
        if (userRepository.existsByUserName("admin")) {
            System.out.println("✓ Admin user already exists");
            return;
        }

        // Get ROLE_ADMIN role
        UserRole adminRole = userRoleRepository.findByNameActive("ROLE_ADMIN")
            .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found. Please run seedAdminRoles() first."));

        // Create default admin user
        User adminUser = new User();
        adminUser.setUserName("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("admin12345@"));
        adminUser.setUserRoleId(adminRole.getId());
        userRepository.save(adminUser);

        System.out.println("✓ Created admin user (username: admin, password: admin12345@)");
    }
}
