package com.telecom.repository;

import com.telecom.entity.DatasetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DatasetRepository extends JpaRepository<DatasetEntity, Long> {
    Optional<DatasetEntity> findByName(String name);
    List<DatasetEntity> findByOrderByUploadedAtDesc();
}
