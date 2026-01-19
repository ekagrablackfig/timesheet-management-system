package com.example.timesheet.config;

import com.example.timesheet.entity.Project;
import com.example.timesheet.entity.Role;
import com.example.timesheet.entity.User;
import com.example.timesheet.repository.ProjectRepository;
import com.example.timesheet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class InitialDataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            // Create Admin
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);

            // Create Manager
            User manager = new User();
            manager.setUsername("manager");
            manager.setPassword(passwordEncoder.encode("manager123"));
            manager.setRole(Role.APPROVER);
            userRepository.save(manager);

            // Create User
            User user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRole(Role.USER);
            user.setManager(manager); // Set manager
            userRepository.save(user);

            // Create Projects
            Project p1 = new Project();
            p1.setName("Internal Ops");
            p1.setDescription("General internal operations");
            projectRepository.save(p1);

            Project p2 = new Project();
            p2.setName("Client Alpha");
            p2.setDescription("Development for Client Alpha");
            projectRepository.save(p2);

            System.out.println("Default data loaded: admin/admin123, manager/manager123, user/user123");
        }
    }
}
