package com.taskflow.dto;

import com.taskflow.entity.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Corps des requêtes POST et PUT : ce que le client a le droit d'envoyer. */
public record TaskRequest(
        @NotBlank(message = "Le titre est obligatoire")
        @Size(max = 100, message = "Le titre ne doit pas dépasser 100 caractères")
        String titre,

        @Size(max = 2000, message = "La description ne doit pas dépasser 2000 caractères")
        String description,

        //@Schema(description = "Priorité ; NORMAL par défaut", example = "NORMAL")
        Priority priorite,

        //@Schema(description = "Date d'échéance (AAAA-MM-JJ), facultative", example = "2026-11-01")
        LocalDate dateEcheance
) {
}
