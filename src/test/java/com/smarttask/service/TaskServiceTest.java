package com.smarttask.service;

import com.smarttask.exception.ResourceNotFoundException;
import com.smarttask.exception.UnauthorizedException;
import com.smarttask.model.dto.request.CreateTaskRequest;
import com.smarttask.model.dto.request.UpdateTaskRequest;
import com.smarttask.model.dto.response.TaskResponse;
import com.smarttask.model.entity.*;
import com.smarttask.repository.TaskRepository;
import com.smarttask.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private UserRepository userRepository;
    @Mock private TaskMapper taskMapper;

    @InjectMocks private TaskService taskService;

    private User user;
    private Task task;
    private TaskResponse taskResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .password("encoded")
                .role(Role.USER)
                .build();

        task = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(7))
                .user(user)
                .build();

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
    }

    @Test
    void createTask_Success() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(7))
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        TaskResponse result = taskService.createTask(request, "testuser");

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Task");
        assertThat(result.getStatus()).isEqualTo(TaskStatus.TODO);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_UserNotFound_ThrowsException() {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Test Task")
                .build();

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(request, "unknown"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void getTaskById_Success() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        TaskResponse result = taskService.getTaskById(1L, "testuser");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Task");
    }

    @Test
    void getTaskById_WrongUser_ThrowsUnauthorizedException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.getTaskById(1L, "otheruser"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void updateTask_Success() {
        UpdateTaskRequest request = UpdateTaskRequest.builder()
                .title("Updated Title")
                .status(TaskStatus.IN_PROGRESS)
                .build();

        Task updated = Task.builder()
                .id(1L).title("Updated Title").status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.MEDIUM).user(user).build();

        TaskResponse updatedResponse = TaskResponse.builder()
                .id(1L).title("Updated Title").status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.MEDIUM).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(updated);
        when(taskMapper.toResponse(updated)).thenReturn(updatedResponse);

        TaskResponse result = taskService.updateTask(1L, request, "testuser");

        assertThat(result.getTitle()).isEqualTo("Updated Title");
        assertThat(result.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void deleteTask_Success() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskService.deleteTask(1L, "testuser");

        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_NotFound_ThrowsException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.deleteTask(99L, "testuser"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task");
        verify(taskRepository, never()).delete(any());
    }
}
