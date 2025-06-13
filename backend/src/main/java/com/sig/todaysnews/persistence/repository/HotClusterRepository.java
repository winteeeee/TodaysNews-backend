package com.sig.todaysnews.persistence.repository;

import com.sig.todaysnews.persistence.entity.Cluster;
import com.sig.todaysnews.persistence.entity.HotCluster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface HotClusterRepository extends JpaRepository<HotCluster, Cluster> {
    @Query("SELECT c FROM HotCluster c JOIN FETCH c.cluster WHERE c.regdate = :date ORDER BY c.size DESC")
    List<HotCluster> findHotClustersByDate(@Param("date") LocalDate date);
}
