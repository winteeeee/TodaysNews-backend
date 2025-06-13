package com.sig.todaysnews.persistence.repository;

import com.sig.todaysnews.persistence.entity.User;
import com.sig.todaysnews.persistence.entity.ViewHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ViewHistoryRepository extends JpaRepository<ViewHistory, Long> {
    @Query("SELECT v FROM ViewHistory v JOIN FETCH v.centroid c JOIN FETCH c.cluster WHERE v.regdate < :date AND v.user = :user ORDER BY v.regdate DESC")
    List<ViewHistory> findAllWithUser(@Param("user") User user, @Param("date") LocalDateTime date, Pageable pageable);
}
