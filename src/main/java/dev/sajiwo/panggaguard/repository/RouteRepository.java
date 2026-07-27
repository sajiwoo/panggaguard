package dev.sajiwo.panggaguard.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.sajiwo.panggaguard.entity.Route;

@Repository
public interface RouteRepository extends JpaRepository<Route, UUID> {
}
