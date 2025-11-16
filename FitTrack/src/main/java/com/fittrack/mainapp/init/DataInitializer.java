package com.fittrack.mainapp.init;

import com.fittrack.mainapp.model.entity.Role;
import com.fittrack.mainapp.model.enums.RoleEnum;
import com.fittrack.mainapp.model.entity.User;
import com.fittrack.mainapp.repository.RoleRepository;
import com.fittrack.mainapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            Role userRole = new Role();
            userRole.setName(RoleEnum.USER);
            roleRepository.save(userRole);

            Role adminRole = new Role();
            adminRole.setName(RoleEnum.ADMIN);
            roleRepository.save(adminRole);
        }

        if (userRepository.count() == 0) {
            Role adminRole = roleRepository.findByName(RoleEnum.ADMIN).orElseThrow();
            Role userRole = roleRepository.findByName(RoleEnum.USER).orElseThrow();

            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@fittrack.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRoles(Set.of(adminRole, userRole));
            userRepository.save(admin);
        }
    }
}