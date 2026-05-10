package com.smarttask.repository;

import com.smarttask.model.entity.Task;
import com.smarttask.model.entity.TaskPriority;
import com.smarttask.model.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUserId(Long userId);

    List<Task> findByUserIdAndStatus(Long userId, TaskStatus status);

    List<Task> findByUserIdAndPriority(Long userId, TaskPriority priority);

    List<Task> findByUserIdOrderByDueDateAsc(Long userId);

    List<Task> findByUserIdAndDueDateBefore(Long userId, LocalDate date);

    long countByUserIdAndStatus(Long userId, TaskStatus status);
}
