package com.sig.todaysnews;

import com.sig.todaysnews.annotation.TimeCheck;
import com.sig.todaysnews.persistence.entity.Cluster;
import com.sig.todaysnews.persistence.repository.ClusterRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Import(ClusterRepositoryTest.ClusterRepositoryWrapper.class)
public class ClusterRepositoryTest {
    public static class ClusterRepositoryWrapper {
        @Autowired
        ClusterRepository clusterRepository;

        @TimeCheck
        public void findClustersBySidAndDate() {
            clusterRepository.findClustersBySidAndDate(1L, LocalDate.of(2025, 5, 2));
        }

        @TimeCheck
        public void findAllWithRelatedClusters() {
            List<Cluster> relatedClusters = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                relatedClusters.add(Cluster.builder().clusterId((long) i).build());
            }
            clusterRepository.findAllWithRelatedClusters(relatedClusters, LocalDate.of(2025, 5, 2), Pageable.ofSize(1000000));
        }
    }

    @Autowired
    ClusterRepositoryWrapper clusterRepositoryWrapper;

    @Test
    void findClusterBySidAndDateTest() {
        clusterRepositoryWrapper.findClustersBySidAndDate();
    }

    @Test
    void findAllWithRelatedClustersTest() {
        clusterRepositoryWrapper.findAllWithRelatedClusters();
    }
}
