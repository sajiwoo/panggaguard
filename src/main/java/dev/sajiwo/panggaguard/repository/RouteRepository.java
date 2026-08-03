package dev.sajiwo.panggaguard.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dev.sajiwo.panggaguard.entity.Route;

@Repository
public interface RouteRepository extends JpaRepository<Route, UUID> {

  @Query("SELECT r FROM Route r WHERE LOWER(r.uriType) = 'user_authenticated_url_redirection' AND r.domain = :domain AND r.domainRole = :domainRole")
  Optional<Route> findUserAuthUrlRedirection(@Param("domain") String domain, @Param("domainRole") String role);

  @Query("SELECT r FROM Route r WHERE LOWER(r.uriType) = 'api'")
  List<Route> fetchAllApi();

}
