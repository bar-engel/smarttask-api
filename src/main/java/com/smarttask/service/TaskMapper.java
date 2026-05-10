package com.smarttask.service;

import com.smarttask.model.dto.response.TaskResponse;
import com.smarttask.model.entity.Task;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskMapper {

    public TaskResponse toResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    public List<TaskResponse> toResponseList(List<Task> tasks) {
        return tasks.stream().map(this::toResponse).toList();
    }
}
