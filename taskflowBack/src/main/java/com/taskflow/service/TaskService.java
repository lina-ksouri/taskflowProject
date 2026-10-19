package com.taskflow.service;

import com.taskflow.dto.*;
import com.taskflow.entity.Priority;
import com.taskflow.entity.Task;
import com.taskflow.error.BadRequestException;
import com.taskflow.error.ResourceNotFoundException;
import com.taskflow.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Predicate;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private static final int MAX_PAGE_SIZE = 100;

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    // ---------- Espace utilisateur : toujours restreint à SES tâches ----------

    public PageResponse<TaskResponse> listMine(AuthenticatedUser user, Boolean completed, Priority priority,
                                               String sortBy, String direction, int page, int size) {
        return search(List.of(user.id()), completed, priority, sortBy, direction, page, size);
    }

    public TaskResponse getMine(AuthenticatedUser user, UUID id) {
        return getReponseTask(findOwned(user, id));
    }

    @Transactional
    public TaskResponse create(AuthenticatedUser user, TaskRequest request) {
        Task task = new Task();
        apply(request, task);
        task.setOwnerId(user.id());
        task.setProprietaire(user.username());
        return getReponseTask(repository.save(task));
    }

    @Transactional
    public TaskResponse update(AuthenticatedUser user, UUID id, TaskRequest request) {
        Task task = findOwned(user, id);
        apply(request, task);
        return getReponseTask(repository.save(task));
    }

    @Transactional
    public TaskResponse toggle(AuthenticatedUser user, UUID id) {
        Task task = findOwned(user, id);
        task.setEstTermine(!task.isEstTermine());
        return getReponseTask(repository.save(task));
    }

    @Transactional
    public void delete(AuthenticatedUser user, UUID id) {
        repository.delete(findOwned(user, id));
    }

    public TaskStatsResponse myStats(AuthenticatedUser user) {
        LocalDate today = LocalDate.now();
        return new TaskStatsResponse(
                repository.nbrTachesTermineesParProprietaire(user.id()),
                repository.nbrTachesEnCours(user.id(), today),
                repository.nbrTachesEnRetard(user.id(), today));
    }

    // ---------- Espace administrateur : toutes les tâches ----------

    public PageResponse<TaskResponse> listAll(String owner, Boolean completed, Priority priority,
                                              String sortBy, String direction, int page, int size) {
        List<String> ownerIds = new ArrayList<>();

        if (owner != null && !owner.isBlank()) {
            List<String> ids = repository.findOwnerIdsByName(owner.trim());

            if (!ids.isEmpty()) {
                ownerIds = ids;
            } else {
                // Ken owner mawjoud directement comme ID
                ownerIds.add(owner.trim());
            }
        }
        return search(ownerIds, completed, priority, sortBy, direction, page, size);
    }

    @Transactional
    public void deleteAny(UUID id) {
        Task task = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tâche " + id + " introuvable"));
        repository.delete(task);
    }

    public List<UserTaskStats> statsByUser() {
        return repository.statsByUser(LocalDate.now()).stream()
                .map(r -> new UserTaskStats(
                        (String) r[0], (String) r[1],
                        ((Long) r[2]), ((Long) r[3]), ((Long) r[4])))
                .collect(Collectors.toList());
    }
    // ---------- Méthodes internes ----------

    /** Une tâche d'un autre utilisateur est traitée comme inexistante (404) : on ne révèle pas son existence. */
    private Task findOwned(AuthenticatedUser user, UUID id) {
        return repository.findByIdentifiantAndOwnerId(id, user.id())
                .orElseThrow(() -> new ResourceNotFoundException("Tâche " + id + " introuvable"));
    }

    private PageResponse<TaskResponse> search(List<String> ownerIds, Boolean completed, Priority priority,
                                              String sortBy, String direction, int page, int size) {
        Specification<Task> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (ownerIds != null && !ownerIds.isEmpty()) predicates.add(root.get("ownerId").in(ownerIds));
            if (completed != null) predicates.add(cb.equal(root.get("estTermine"), completed));
            if (priority != null) predicates.add(cb.equal(root.get("priorite"), priority));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                buildSort(sortBy, direction));
        Page<Task> result = repository.findAll(spec, pageable);
        LocalDate today = LocalDate.now();
        return PageResponse.of(result, t -> getReponseTask(t));
    }

    /** Tri limité à une liste blanche de champs (échéance ou date de création) : pas d'injection de tri libre. */
    private Sort buildSort(String sortBy, String direction)  {
        String property = sortBy == null || sortBy.isEmpty() ? "dateCreation" : sortBy;
        if (!property.equals("dateEcheance") && !property.equals("dateCreation")) {
            throw new BadRequestException("Tri invalide : utiliser 'dateEcheance' ou 'dateCreation'");
        }
        Sort.Direction dir;
        if (direction == null || direction.isEmpty()) {
            dir = property.equals("dateEcheance") ? Sort.Direction.ASC : Sort.Direction.DESC;
        } else {
            dir = Sort.Direction.fromOptionalString(direction)
                    .orElseThrow(() -> new BadRequestException("Direction invalide : utiliser 'asc' ou 'desc'"));
        }
        // Les tâches sans échéance passent toujours en dernier ; l'id départage les égalités
        return Sort.by(new Sort.Order(dir, property), Sort.Order.asc("identifiant"));
    }


    static TaskResponse getReponseTask(Task t) {
        LocalDate today = LocalDate.now();
        boolean overdue = !t.isEstTermine() && t.getDateEcheance() != null && t.getDateEcheance().isBefore(today);
        return new TaskResponse(t.getIdentifiant(), t.getTitre(), t.getDescription(), t.getPriorite(),
                t.getDateEcheance(), t.isEstTermine(), overdue, t.getOwnerId(), t.getProprietaire(),
                t.getDateCreation(), t.getDateMiseAJour());
    }

    /** Copie les champs modifiables d'une requête vers l'entité (le propriétaire n'est jamais touché). */
    static void apply(TaskRequest req, Task t) {
        t.setTitre(req.titre().trim());
        t.setDescription(req.description());
        t.setPriorite(req.priorite() != null ? req.priorite() : Priority.NORMALE);
        t.setDateEcheance(req.dateEcheance());
    }




}
