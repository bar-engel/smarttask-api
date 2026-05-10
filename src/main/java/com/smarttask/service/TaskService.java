package com.smarttask.service;

import com.smarttask.exception.ResourceNotFoundException;
import com.smarttask.exception.UnauthorizedException;
import com.smarttask.model.dto.request.CreateTaskRequest;
import com.smarttask.model.dto.request.UpdateTaskRequest;
import com.smarttask.model.dto.response.TaskResponse;
import com.smarttask.model.entity.Task;
import com.smarttask.model.entity.TaskPriority;
import com.smarttask.model.entity.TaskStatus;
import com.smarttask.model.entity.User;
import com.smarttask.repository.TaskRepository;
import com.smarttask.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    public TaskResponse createTask(CreateTaskRequest request, String username) {
        User user = findUser(username);

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus())
                .priority(request.getPriority())
                .dueDate(request.getDueDate())
                .user(user)
                .build();

        return taskMapper.toResponse(taskRepository.save(task));
    }

    public List<TaskResponse> getAllTasks(String username) {
        User user = findUser(username);
        return taskMapper.toResponseList(taskRepository.findByUserId(user.getId()));
    }

    public TaskResponse getTaskById(Long id, String username) {
        return taskMapper.toResponse(findTaskAndVerifyOwnership(id, username));
    }

    public TaskResponse updateTask(Long id, UpdateTaskRequest request, String username) {
        Task task = findTaskAndVerifyOwnership(id, username);

        if (request.getTitle() != null)       task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getStatus() != null)      task.setStatus(request.getStatus());
        if (request.getPriority() != null)    task.setPriority(request.getPriority());
        if (request.getDueDate() != null)     task.setDueDate(request.getDueDate());

        return taskMapper.toResponse(taskRepository.save(task));
    }

    public void deleteTask(Long id, String username) {
        taskRepository.delete(findTaskAndVerifyOwnership(id, username));
    }

    public List<TaskResponse> getTasksByStatus(TaskStatus status, String username) {
        User user = findUser(username);
        return taskMapper.toResponseList(taskRepository.findByUserIdAndStatus(user.getId(), status));
    }

    public List<TaskResponse> getTasksByPriority(TaskPriority priority, String username) {
        User user = findUser(username);
        return taskMapper.toResponseList(taskRepository.findByUserIdAndPriority(user.getId(), priority));
    }

    public Map<String, Long> getTaskStats(String username) {
        User user = findUser(username);
        Long userId = user.getId();

        long todo       = taskRepository.countByUserIdAndStatus(userId, TaskStatus.TODO);
        long inProgress = taskRepository.countByUserIdAndStatus(userId, TaskStatus.IN_PROGRESS);
        long done       = taskRepository.countByUserIdAndStatus(userId, TaskStatus.DONE);

        Map<String, Long> stats = new HashMap<>();
        stats.put("TODO", todo);
        stats.put("IN_PROGRESS", inProgress);
        stats.put("DONE", done);
        stats.put("TOTAL", todo + inProgress + done);
        return stats;
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private Task findTaskAndVerifyOwnership(Long taskId, String username) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));

        if (!task.getUser().getUsername().equals(username)) {
            throw new UnauthorizedException();
        }

        return task;
    }
}
