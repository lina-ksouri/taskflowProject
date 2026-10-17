package com.taskflow.controller;


import com.taskflow.dto.*;
import com.taskflow.entity.Priority;
import com.taskflow.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    // ===== Espace utilisateur (rôle user) =====

    @GetMapping("/tasks")
    @Tag(name = "Mes tâches")
    @Operation(summary = "Liste paginée de mes tâches (filtres statut/priorité, tri échéance/création)")
    public PageResponse<TaskResponse> list(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "true = terminées, false = non terminées") @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String direction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.listMine(AuthenticatedUser.from(jwt), completed, priority, sortBy, direction, page, size);
    }

    @GetMapping("/tasks/stats")
    @Tag(name = "Mes tâches")
    @Operation(summary = "Mes statistiques : terminées, en cours, en retard")
    public TaskStatsResponse stats(@AuthenticationPrincipal Jwt jwt) {
        return service.myStats(AuthenticatedUser.from(jwt));
    }

    @GetMapping("/tasks/{id}")
    @Tag(name = "Mes tâches")
    @Operation(summary = "Détail d'une de mes tâches")
    public TaskResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return service.getMine(AuthenticatedUser.from(jwt), id);
    }

    @PostMapping("/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    @Tag(name = "Mes tâches")
    @Operation(summary = "Créer une tâche (le propriétaire est l'utilisateur du jeton)")
    public TaskResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TaskRequest request) {
        return service.create(AuthenticatedUser.from(jwt), request);
    }

    @PutMapping("/tasks/{id}")
    @Tag(name = "Mes tâches")
    @Operation(summary = "Modifier une de mes tâches")
    public TaskResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                               @Valid @RequestBody TaskRequest request) {
        return service.update(AuthenticatedUser.from(jwt), id, request);
    }

    @PatchMapping("/tasks/{id}/toggle")
    @Tag(name = "Mes tâches")
    @Operation(summary = "Basculer le statut terminé / non terminé")
    public TaskResponse toggle(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return service.toggle(AuthenticatedUser.from(jwt), id);
    }

    @DeleteMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Tag(name = "Mes tâches")
    @Operation(summary = "Supprimer une de mes tâches")
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        service.delete(AuthenticatedUser.from(jwt), id);
    }

    // ===== Espace administrateur (rôle admin, imposé par SecurityConfig sur /api/admin/**) =====

    @GetMapping("/admin/tasks")
    @Tag(name = "Administration")
    @Operation(summary = "Toutes les tâches, paginées, filtrables par propriétaire")
    public PageResponse<TaskResponse> adminList(
            @Parameter(description = "Identifiant (sub) du propriétaire") @RequestParam(required = false) String ownerId,
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String direction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.listAll(ownerId, completed, priority, sortBy, direction, page, size);
    }

    @DeleteMapping("/admin/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Tag(name = "Administration")
    @Operation(summary = "Supprimer n'importe quelle tâche")
    public void adminDelete(@PathVariable UUID id) {
        service.deleteAny(id);
    }

    @GetMapping("/admin/stats")
    @Tag(name = "Administration")
    @Operation(summary = "Nombre de tâches par utilisateur et par statut")
    public List<UserTaskStats> adminStats() {
        return service.statsByUser();
    }
}
