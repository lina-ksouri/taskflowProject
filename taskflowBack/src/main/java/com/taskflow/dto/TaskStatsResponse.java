package com.taskflow.dto;

/** Statistiques personnelles : les trois compteurs sont disjoints. */
public record TaskStatsResponse(long completed, long inProgress, long overdue) {
}
