package com.dicomviewer.repository;

import com.dicomviewer.model.entity.ApplicationEntity;
import com.dicomviewer.model.entity.ApplicationEntity.AEType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationEntityRepository extends JpaRepository<ApplicationEntity, Long> {

    Optional<ApplicationEntity> findByAeTitle(String aeTitle);

    List<ApplicationEntity> findByAeType(AEType aeType);

    List<ApplicationEntity> findByEnabledTrue();

    @Query("SELECT ae FROM ApplicationEntity ae WHERE ae.aeType = 'LOCAL' AND ae.enabled = true")
    Optional<ApplicationEntity> findLocalAE();

    @Query("SELECT ae FROM ApplicationEntity ae WHERE ae.defaultAE = true AND ae.enabled = true")
    Optional<ApplicationEntity> findDefaultAE();

    List<ApplicationEntity> findByAeTypeAndEnabledTrue(AEType aeType);

    boolean existsByAeTitle(String aeTitle);
}
