package com.sig.todaysnews.persistence.repository;

import com.sig.todaysnews.persistence.entity.UserAuthority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAuthorityRepository extends JpaRepository<UserAuthority, Long> {
    List<UserAuthority> findByUserId(Long id);
}
