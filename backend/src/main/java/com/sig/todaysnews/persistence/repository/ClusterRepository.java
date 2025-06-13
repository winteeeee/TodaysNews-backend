package com.sig.todaysnews.persistence.repository;

import com.sig.todaysnews.persistence.entity.Cluster;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ClusterRepository extends JpaRepository<Cluster, Long> {
    @Query("SELECT c FROM Cluster c WHERE c.section.sectionId = :sid AND c.regdate = :date ORDER BY c.size DESC")
    List<Cluster> findClustersBySidAndDate(@Param("sid") Long sid, @Param("date") LocalDate date);

    @Query("SELECT c FROM Cluster c WHERE c.regdate = :date AND c.relatedCluster in :relatedClusters ORDER BY c.size DESC")
    List<Cluster> findAllWithRelatedClusters(@Param("relatedClusters") List<Cluster> relatedClusters, @Param("date") LocalDate date, Pageable pageable);

    @Query("SELECT c FROM Cluster c WHERE c.section.sectionId = :sid AND c.regdate = :date ORDER BY c.size DESC")
    List<Cluster> findOneWithRandomBySidAndDate(@Param("sid") Long sid, @Param("date") LocalDate date, Pageable pageable);

    @Query("SELECT FUNCTION('count', c) FROM Cluster c WHERE c.section.sectionId = :sid AND c.regdate = :date")
    Long countAllBySidAndDate(@Param("sid") Long sid, @Param("date") LocalDate date);
}
