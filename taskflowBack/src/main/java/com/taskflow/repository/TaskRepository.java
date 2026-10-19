package com.taskflow.repository;

import com.taskflow.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

    /** Recherche par id ET propriétaire : un utilisateur ne peut pas atteindre la tâche d'un autre. */
    Optional<Task> findByIdentifiantAndOwnerId(UUID identifiant, String ownerId);

    @Query("""
            select count(task) from Task task
            where task.ownerId = :ownerId and task.estTermine = true""")
    long nbrTachesTermineesParProprietaire(String ownerId);

    /** En cours = non terminée et pas encore en retard. */
    @Query("""
            select count(task) from Task task
            where task.ownerId = :ownerId and task.estTermine = false
              and (task.dateEcheance is null or task.dateEcheance >= :today)""")
    long nbrTachesEnCours(@Param("ownerId") String ownerId, @Param("today") LocalDate today);

    /** En retard = non terminée et échéance dépassée. */
    @Query("""
            select count(task) from Task task
            where task.ownerId = :ownerId and task.estTermine = false and task.dateEcheance < :today""")
    long nbrTachesEnRetard(@Param("ownerId") String ownerId, @Param("today") LocalDate today);

    /** Statistiques admin : une ligne par utilisateur, avec le nombre de tâches par statut. */
    @Query("""
        select task.ownerId, max(task.proprietaire),
               count(case when task.estTermine = true then 1 end),
               count(case when task.estTermine = false and (task.dateEcheance is null or task.dateEcheance >= :today) then 1 end),
               count(case when task.estTermine = false and task.dateEcheance < :today then 1 end)
        from Task task
        group by task.ownerId
        order by max(task.proprietaire)""")
    List<Object[]> statsByUser(@Param("today") LocalDate today);

    @Query("""
    SELECT DISTINCT task.ownerId
    FROM Task task
    WHERE LOWER(task.proprietaire) = LOWER(:owner)
""")
    List<String> findOwnerIdsByName(@Param("owner") String owner);

}
