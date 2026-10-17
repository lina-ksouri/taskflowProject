package com.taskflow.dto;


import com.taskflow.entity.Priority;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** Ce que l'API renvoie : l'entité n'est jamais exposée directement. */
public record TaskResponse(
        UUID id,
        String title,
        String description,
        Priority priority,
        LocalDate dueDate,
        boolean completed,
        boolean overdue,       // calculé : non terminée et échéance dépassée
        String ownerId,
        String ownerName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
