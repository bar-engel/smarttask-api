package com.smarttask.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttask.exception.ResourceNotFoundException;
import com.smarttask.model.dto.request.CreateTaskRequest;
import com.smarttask.model.dto.request.LoginRequest;
import com.smarttask.model.dto.request.RegisterRequest;
import com.smarttask.model.dto.request.UpdateTaskRequest;
import com.smarttask.model.dto.response.AuthResponse;
import com.smarttask.model.dto.response.TaskResponse;
import com.smarttask.model.entity.TaskPriority;
import com.smarttask.model.entity.TaskStatus;
import com.smarttask.service.AuthService;
import com.smarttask.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private TaskService taskService;
    @MockBean private AuthService authService;

    private TaskResponse taskResponse;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        taskResponse = TaskResponse.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        authResponse = AuthResponse.builder()
                .token("jwt_token")
                .username("testuser")
                .role("USER")
                .build();
    }

    // ── Task endpoints ──────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void createTask_Returns201() throws Exception {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .build();

        when(taskService.createTask(any(), any())).thenReturn(taskResponse);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    @WithMockUser
    void getAllTasks_Returns200() throws Exception {
        when(taskService.getAllTasks(any())).thenReturn(List.of(taskResponse));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Task"));
    }

    @Test
    @WithMockUser
    void getTaskById_Returns200() throws Exception {
        when(taskService.getTaskById(eq(1L), any())).thenReturn(taskResponse);

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    @WithMockUser
    void getTaskById_NotFound_Returns404() throws Exception {
        when(taskService.getTaskById(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Task", 99L));

        mockMvc.perform(get("/api/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task not found with id: 99"));
    }

    @Test
    @WithMockUser
    void updateTask_Returns200() throws Exception {
        UpdateTaskRequest request = UpdateTaskRequest.builder()
                .title("Updated Task")
                .status(TaskStatus.IN_PROGRESS)
                .build();

        TaskResponse updated = TaskResponse.builder()
                .id(1L).title("Updated Task").status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.MEDIUM)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        when(taskService.updateTask(eq(1L), any(), any())).thenReturn(updated);

        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @WithMockUser
    void deleteTask_Returns204() throws Exception {
        doNothing().when(taskService).deleteTask(eq(1L), any());

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(eq(1L), any());
    }

    // ── Auth endpoints ──────────────────────────────────────────────────────────

    @Test
    void register_Returns201() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email("newuser@test.com")
                .password("password123")
                .build();

        when(authService.register(any())).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt_token"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void login_Returns200() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        when(authService.login(any())).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt_token"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }
}
