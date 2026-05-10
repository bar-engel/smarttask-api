package com.smarttask.controller;

import com.smarttask.model.dto.request.CreateTaskRequest;
import com.smarttask.model.dto.request.UpdateTaskRequest;
import com.smarttask.model.dto.response.TaskResponse;
import com.smarttask.model.entity.TaskPriority;
import com.smarttask.model.entity.TaskStatus;
import com.smarttask.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Create, read, update, and delete tasks")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    @Operation(summary = "Get all tasks", description = "Returns all tasks for the current user, optionally filtered by status or priority")
    public ResponseEntity<List<TaskResponse>> getAllTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority
    ) {
        String username = currentUsername();

        if (status != null) {
            return ResponseEntity.ok(taskService.getTasksByStatus(status, username));
        }
        if (priority != null) {
            return ResponseEntity.ok(taskService.getTasksByPriority(priority, username));
        }
        return ResponseEntity.ok(taskService.getAllTasks(username));
    }

    @PostMapping
    @Operation(summary = "Create a task", description = "Creates a new task for the current user")
    public ResponseEntity<TaskResponse> createTask(@RequestBody @Valid CreateTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createTask(request, currentUsername()));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get task statistics", description = "Returns task counts grouped by status for the current user")
    public ResponseEntity<Map<String, Long>> getTaskStats() {
        return ResponseEntity.ok(taskService.getTaskStats(currentUsername()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID", description = "Returns a specific task; fails with 403 if it does not belong to the current user")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id, currentUsername()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a task", description = "Updates only the non-null fields of the task; fails with 403 if not owned by the current user")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @RequestBody UpdateTaskRequest request
    ) {
        return ResponseEntity.ok(taskService.updateTask(id, request, currentUsername()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task", description = "Permanently deletes a task; fails with 403 if not owned by the current user")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id, currentUsername());
        return ResponseEntity.noContent().build();
    }

    private String currentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
