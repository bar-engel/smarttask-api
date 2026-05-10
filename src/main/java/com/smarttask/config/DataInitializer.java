package com.smarttask.config;

import com.smarttask.model.entity.*;
import com.smarttask.repository.TaskRepository;
import com.smarttask.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded — skipping.");
            return;
        }

        userRepository.save(User.builder()
                .username("admin")
                .email("admin@smarttask.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build());

        User john = userRepository.save(User.builder()
                .username("johndoe")
                .email("john@smarttask.com")
                .password(passwordEncoder.encode("user1234"))
                .role(Role.USER)
                .build());

        taskRepository.saveAll(List.of(
                Task.builder()
                        .title("Set up project structure")
                        .description("Initialize Spring Boot project with Maven dependencies and package layout")
                        .status(TaskStatus.DONE)
                        .priority(TaskPriority.HIGH)
                        .dueDate(LocalDate.now().minusDays(7))
                        .user(john)
                        .build(),
                Task.builder()
                        .title("Implement JWT authentication")
                        .description("Add JWT-based security using Spring Security 6 and jjwt 0.12")
                        .status(TaskStatus.DONE)
                        .priority(TaskPriority.HIGH)
                        .dueDate(LocalDate.now().minusDays(3))
                        .user(john)
                        .build(),
                Task.builder()
                        .title("Build task CRUD endpoints")
                        .description("Create REST controllers and service layer for full task lifecycle")
                        .status(TaskStatus.IN_PROGRESS)
                        .priority(TaskPriority.MEDIUM)
                        .dueDate(LocalDate.now().plusDays(2))
                        .user(john)
                        .build(),
                Task.builder()
                        .title("Write unit and integration tests")
                        .description("Cover service and controller layers with JUnit 5 and MockMvc")
                        .status(TaskStatus.IN_PROGRESS)
                        .priority(TaskPriority.MEDIUM)
                        .dueDate(LocalDate.now().plusDays(5))
                        .user(john)
                        .build(),
                Task.builder()
                        .title("Deploy to production")
                        .description("Containerise with Docker and deploy to cloud infrastructure")
                        .status(TaskStatus.TODO)
                        .priority(TaskPriority.LOW)
                        .dueDate(LocalDate.now().plusDays(14))
                        .user(john)
                        .build()
        ));

        log.info("Seeded: 2 users (admin / johndoe), 5 tasks");
        log.info("Credentials — admin: admin/admin123 | user: johndoe/user1234");
    }
}
