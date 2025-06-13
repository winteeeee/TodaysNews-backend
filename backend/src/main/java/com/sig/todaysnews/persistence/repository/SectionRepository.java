package com.sig.todaysnews.persistence.repository;

import com.sig.todaysnews.persistence.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectionRepository extends JpaRepository<Section, Long> {
}
