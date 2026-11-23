package in.jansetu.auth.repository;

import in.jansetu.auth.entity.SupervisorJurisdiction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SupervisorJurisdictionRepository extends JpaRepository<SupervisorJurisdiction, UUID> {
    @Query("select j.centreId from SupervisorJurisdiction j where j.userId = :userId")
    List<UUID> findCentreIdsByUserId(UUID userId);

    @Modifying
    @Query("delete from SupervisorJurisdiction j where j.userId = :userId")
    void deleteByUserId(UUID userId);
}
