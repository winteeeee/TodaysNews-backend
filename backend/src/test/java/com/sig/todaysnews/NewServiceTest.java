package com.sig.todaysnews;

import com.sig.todaysnews.sevice.NewsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@SpringBootTest
public class NewServiceTest {
    @Autowired
    NewsServiceImpl newsService;

    @Test
    void getProposalTest() {
        newsService.getProposal(LocalDate.of(2025, 5, 1));
    }

    @Test
    void getHotClustersTest() {
        newsService.getHotClusters(LocalDate.of(2025, 5, 1));
    }

    @Test
    void getClusterTest() {
        newsService.getCluster(1L);
    }
}
