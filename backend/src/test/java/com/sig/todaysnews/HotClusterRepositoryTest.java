package com.sig.todaysnews;

import com.sig.todaysnews.annotation.TimeCheck;
import com.sig.todaysnews.persistence.repository.HotClusterRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;

@SpringBootTest
@Import(HotClusterRepositoryTest.HotClusterRepositoryWrapper.class)
public class HotClusterRepositoryTest {
    public static class HotClusterRepositoryWrapper {
        @Autowired
        HotClusterRepository hotClusterRepository;

        @TimeCheck
        void findHotClustersByDate() {
            hotClusterRepository.findHotClustersByDate(LocalDate.of(2025, 5, 1));
        }
    }

    @Autowired
    HotClusterRepositoryWrapper hotClusterRepositoryWrapper;

    @Test
    void findHotClustersByDateTest() {
        hotClusterRepositoryWrapper.findHotClustersByDate();
    }
}
