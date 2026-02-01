package com.dicomviewer.repository;

import com.dicomviewer.model.PacsConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for PACS configurations.
 */
@Repository
public interface PacsConfigurationRepository extends JpaRepository<PacsConfiguration, UUID> {

    List<PacsConfiguration> findByIsActiveTrue();

    List<PacsConfiguration> findByPacsType(PacsConfiguration.PacsType pacsType);

    List<PacsConfiguration> findByPacsTypeAndIsActiveTrue(PacsConfiguration.PacsType pacsType);
}
