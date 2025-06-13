package com.sig.todaysnews;

import com.sig.todaysnews.annotation.TimeCheck;
import com.sig.todaysnews.persistence.repository.ArticleRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
// 래퍼 클래스를 빈으로 등록하기 위한 Import 어노테이션
@Import(ArticleRepositoryTest.ArticleRepositoryWrapper.class)
public class ArticleRepositoryTest {
    //성능 측정은 여러 번 수행할 것이므로 래퍼 클래스를 생성하고 클래스 내부 메소드에 TimeCheck 어노테이션 작성
    public static class ArticleRepositoryWrapper {
        @Autowired
        ArticleRepository articleRepository;
        Faker faker = new Faker();

        @TimeCheck
        public void findArticleByCid(int iter) {
            for (int i = 0; i < iter; i++) {
                articleRepository.findArticlesByCid(faker.number().numberBetween(1L, 1_000_000L));
            }
        }
    }

    @Autowired
    ArticleRepositoryWrapper articleRepositoryWrapper;

    @Test
    void findArticleByCidTest() {
        articleRepositoryWrapper.findArticleByCid(100_000);
    }
}
