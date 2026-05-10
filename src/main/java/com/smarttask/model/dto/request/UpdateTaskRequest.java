package com.smarttask.model.dto.request;

import com.smarttask.model.entity.TaskPriority;
import com.smarttask.model.entity.TaskStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskRequest {

    @Size(max = 100)
    private String title;

    @Size(max = 500)
    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    private LocalDate dueDate;
}
