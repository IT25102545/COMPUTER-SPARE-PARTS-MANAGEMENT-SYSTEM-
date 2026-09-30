package com.lankatech.spareparts.config;

import com.lankatech.spareparts.auth.entity.Role;
import com.lankatech.spareparts.auth.entity.User;
import com.lankatech.spareparts.auth.repository.RoleRepository;
import com.lankatech.spareparts.auth.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class AuthDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthDataInitializer(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        createRolesIfMissing();

        String demoPassword = System.getenv("DEMO_USER_PASSWORD");

        if (demoPassword == null || demoPassword.isBlank()) {
            System.out.println(
                    "DEMO_USER_PASSWORD is not set. Demo user creation skipped."
            );
            return;
        }

        createUserIfMissing(
                "Branch Supervisor",
                "test.supervisor@lankatech.local",
                "BRANCH_SUPERVISOR",
                demoPassword
        );

        createUserIfMissing(
                "Inventory Supervisor",
                "inventory.supervisor@lankatech.local",
                "INVENTORY_SUPERVISOR",
                demoPassword
        );

        createUserIfMissing(
                "Supplier Relations Officer",
                "supplier.officer@lankatech.local",
                "SUPPLIER_RELATIONS_OFFICER",
                demoPassword
        );

        createUserIfMissing(
                "Sales Officer",
                "sales.officer@lankatech.local",
                "SALES_OFFICER",
                demoPassword
        );

        createUserIfMissing(
                "Customer Service Officer",
                "customer.service@lankatech.local",
                "CUSTOMER_SERVICE_OFFICER",
                demoPassword
        );

        createUserIfMissing(
                "Manager",
                "manager@lankatech.local",
                "MANAGER",
                demoPassword
        );

        createUserIfMissing(
                "System Administrator",
                "admin@lankatech.local",
                "SYSTEM_ADMINISTRATOR",
                demoPassword
        );

        System.out.println("Authentication demo users are ready.");
    }

    private void createRolesIfMissing() {

        Map<String, String> roles = new LinkedHashMap<>();

        roles.put(
                "BRANCH_SUPERVISOR",
                "Manages branch and stock transfer operations"
        );

        roles.put(
                "INVENTORY_SUPERVISOR",
                "Manages inventory and stock operations"
        );

        roles.put(
                "SUPPLIER_RELATIONS_OFFICER",
                "Manages suppliers and purchase operations"
        );

        roles.put(
                "SALES_OFFICER",
                "Manages sales, payments and invoicing"
        );

        roles.put(
                "CUSTOMER_SERVICE_OFFICER",
                "Manages customer service, reservations and complaints"
        );

        roles.put(
                "MANAGER",
                "Views management reports and oversees operations"
        );

        roles.put(
                "SYSTEM_ADMINISTRATOR",
                "Manages system administration activities"
        );

        roles.forEach((roleName, description) -> {

            if (roleRepository.findByRoleName(roleName).isEmpty()) {

                Role role = new Role();
                role.setRoleName(roleName);
                role.setDescription(description);

                roleRepository.save(role);
            }
        });
    }

    private void createUserIfMissing(
            String fullName,
            String email,
            String roleName,
            String rawPassword) {

        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            return;
        }

        Role role = roleRepository
                .findByRoleName(roleName)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Role not found: " + roleName
                        )
                );

        User user = new User();

        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(
                passwordEncoder.encode(rawPassword)
        );
        user.setRole(role);
        user.setActive(true);

        userRepository.save(user);

        System.out.println(
                "Created demo user: " + email
        );
    }
}