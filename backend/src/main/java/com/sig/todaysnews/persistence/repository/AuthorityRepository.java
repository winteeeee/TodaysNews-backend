package com.sig.todaysnews.persistence.repository;

import com.sig.todaysnews.persistence.entity.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepository extends JpaRepository<Authority, String> {
}
