package com.sig.todaysnews;

import com.sig.todaysnews.annotation.TimeCheck;
import com.sig.todaysnews.persistence.entity.User;
import com.sig.todaysnews.persistence.repository.ViewHistoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

@SpringBootTest
@Import(ViewHistoryRepositoryTest.ViewHistoryRepositoryWrapper.class)
public class ViewHistoryRepositoryTest {
    public static class ViewHistoryRepositoryWrapper {
        @Autowired
        ViewHistoryRepository viewHistoryRepository;

        @TimeCheck
        void findAllWithUser() {
            User user = new User(1L, null, null, true);
            viewHistoryRepository.findAllWithUser(user, LocalDateTime.of(2025, 5, 2, 0, 0, 0), Pageable.ofSize(1000000));
        }
    }

    @Autowired
    ViewHistoryRepositoryWrapper viewHistoryRepositoryWrapper;

    @Test
    void findAllWithUserTest() {
        viewHistoryRepositoryWrapper.findAllWithUser();
    }
}
