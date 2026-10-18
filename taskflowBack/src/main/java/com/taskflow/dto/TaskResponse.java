package com.taskflow.dto;


import com.taskflow.entity.Priority;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** Ce que l'API renvoie : l'entité n'est jamais exposée directement. */
public record TaskResponse(
        UUID identifiant,
        String titre,
        String description,
        Priority priorite,
        LocalDate dateEcheance,
        boolean estTermine,
        boolean overdue,       // calculé : non terminée et échéance dépassée
        String ownerId,
        String proprietaire,
        LocalDateTime dateCreation,
        LocalDateTime dateMiseAJour
) {
}
