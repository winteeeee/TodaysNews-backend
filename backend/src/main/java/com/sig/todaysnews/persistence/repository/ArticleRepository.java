package com.sig.todaysnews.persistence.repository;

import com.sig.todaysnews.persistence.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    @Query("SELECT a FROM Article a WHERE a.cluster.clusterId = :cid")
    List<Article> findArticlesByCid(@Param("cid") Long cid);
}
