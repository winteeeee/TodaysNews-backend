package com.sig.todaysnews;

import com.sig.todaysnews.persistence.entity.*;
import com.sig.todaysnews.persistence.repository.*;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Locale;

@SpringBootTest
public class DummyDataInsertTest {
    private static final Logger log = LoggerFactory.getLogger(DummyDataInsertTest.class);
    @Autowired
    SectionRepository sectionRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    AuthorityRepository authorityRepository;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void sectionInsertTest() {
        ArrayList<Section> sections = new ArrayList<>();
        sections.add(new Section(1L, "정치"));
        sections.add(new Section(2L, "경제"));
        sections.add(new Section(3L, "사회"));
        sections.add(new Section(4L, "생활"));
        sections.add(new Section(5L, "IT"));
        sections.add(new Section(6L, "세계"));
        sections.add(new Section(7L, "오피니언"));
        sectionRepository.saveAll(sections);
    }

    @Test
    void authorityInsertTest() {
        ArrayList<Authority> authorities = new ArrayList<>();
        authorities.add(new Authority("USER"));
        authorities.add(new Authority("ADMIN"));
        authorityRepository.saveAll(authorities);
    }

    @Test
    void userInsertTest() {
        //Faker 객체 생성, 언어는 한국어로 설정
        Faker faker = new Faker(new Locale("ko"));
        ArrayList<User> users = new ArrayList<>();
        for (int i = 0; i < 1_000_000; i++) {
            //아이디는 무작위로 생성, 중복 방지를 위해 인덱스를 추가
            //비밀번호는 무작위 단어
            users.add(new User((long)(i + 1), faker.name().fullName() + i, faker.word().noun(), true));
        }
        userRepository.saveAll(users);
    }

    @Test
    void articleInsertTest() {
        //삽입할 데이터 크기
        int dataSize = 1_000_000;
        //배치 크기
        int batchSize = 100_000;
        Faker faker = new Faker(new Locale("ko"));
        //PreparedStatement 준비
        String sql = """
                insert into article (article_id, img_url, url, press, title, content, writer, section_id, cluster_id)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        //먼저 더미 데이터를 생성
        ArrayList<Article> articles = new ArrayList<>();
        for (int i = 1; i <= dataSize; i++) {
            articles.add(new Article(
                    (long) i,
                    null,
                    String.valueOf(i),
                    String.valueOf(i),
                    faker.address().cityName(),
                    faker.word().noun(),
                    faker.word().noun(),
                    faker.name().fullName(),
                    null,
                    null
            ));

            //JDBC Template의 batchUpdate를 이용하여 배치 사이즈 단위로 bulk Insert
            if (i % batchSize == 0) {
                jdbcTemplate.batchUpdate(sql, articles, batchSize,
                        (PreparedStatement ps, Article a) -> {
                            ps.setLong(1, a.getArticleId());
                            ps.setString(2, a.getImgUrl());
                            ps.setString(3, a.getUrl());
                            ps.setString(4, a.getPress());
                            ps.setString(5, a.getTitle());
                            ps.setString(6, a.getContent());
                            ps.setString(7, a.getWriter());
                            ps.setLong(8, faker.number().numberBetween(1L, 7L));
                            ps.setLong(9, a.getArticleId());
                        });
                articles.clear();
            }
        }
    }

    @Test
    void clusterInsertTest() {
        //삽입할 데이터 크기
        int dataSize = 1_000_000;
        //배치 크기
        int batchSize = 100_000;
        Faker faker = new Faker(new Locale("ko"));
        //PreparedStatement 준비
        String sql = """
                insert into cluster (cluster_id, regdate, img_url, size, title, words, summary, section_id, centroid_id, related_cluster_id)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        //먼저 더미 데이터를 생성
        ArrayList<Cluster> clusters = new ArrayList<>();
        for (int i = 1; i <= dataSize; i++) {
            clusters.add(new Cluster(
                    (long) i,
                    null,
                    faker.address().cityName(),
                    faker.word().noun(),
                    faker.word().noun(),
                    faker.number().numberBetween(1, 100),
                    faker.word().noun(),
                    null,
                    null,
                    null,
                    null
            ));
            ;

            //JDBC Template의 batchUpdate를 이용하여 배치 사이즈 단위로 bulk Insert
            if (i % batchSize == 0) {
                //cluster_id, regdate, img_url, size, title, words, summary, section_id, centroid_id, related_cluster_id
                jdbcTemplate.batchUpdate(sql, clusters, batchSize,
                        (PreparedStatement ps, Cluster c) -> {
                            ps.setLong(1, c.getClusterId());
                            ps.setString(2,
                                    LocalDate.of(2025,
                                            faker.number().numberBetween(1, 12),
                                            faker.number().numberBetween(1, 28)).toString());
                            ps.setString(3, c.getImgUrl());
                            ps.setLong(4, c.getSize());
                            ps.setString(5, c.getTitle());
                            ps.setString(6, c.getWords());
                            ps.setString(7, c.getSummary());
                            ps.setLong(8, faker.number().numberBetween(1L, 7L));
                            ps.setLong(9, faker.number().numberBetween(1L, 1000000L));
                            ps.setLong(10, faker.number().numberBetween(1L, 1000000L));
                        });
                clusters.clear();
            }
        }
    }

    @Test
    void hotClusterInsertTest() {
        //삽입할 데이터 크기
        int dataSize = 1_000_000;
        //배치 크기
        int batchSize = 100_000;
        Faker faker = new Faker(new Locale("ko"));
        //PreparedStatement 준비
        String sql = """
                insert into hot_cluster (cluster_id, regdate, size, room_name)
                values (?, ?, ?, ?)
                """;

        //먼저 더미 데이터를 생성
        ArrayList<HotCluster> hotClusters = new ArrayList<>();
        for (int i = 1; i <= dataSize; i++) {
            hotClusters.add(new HotCluster(
                    (long) i,
                    null,
                    null,
                    1,
                    faker.word().noun()
            ));


            //JDBC Template의 batchUpdate를 이용하여 배치 사이즈 단위로 bulk Insert
            if (i % batchSize == 0) {
                jdbcTemplate.batchUpdate(sql, hotClusters, batchSize,
                        (PreparedStatement ps, HotCluster c) -> {
                            ps.setLong(1, c.getCluster_id());
                            ps.setString(2,
                                    LocalDate.of(2025,
                                            faker.number().numberBetween(1, 12),
                                            faker.number().numberBetween(1, 28)).toString());
                            ps.setLong(3, faker.number().numberBetween(1, 100));
                            ps.setString(4, c.getRoomName());
                        });
                hotClusters.clear();
            }
        }
    }

    @Test
    void viewHistoryInsertTest() {
        //삽입할 데이터 크기
        int dataSize = 1_000_000;
        //배치 크기
        int batchSize = 100_000;
        Faker faker = new Faker(new Locale("ko"));
        //PreparedStatement 준비
        String sql = """
                insert into view_history (view_history_id, user_id, centroid_id)
                values (?, ?, ?)
                """;

        //먼저 더미 데이터를 생성
        ArrayList<ViewHistory> viewHistories = new ArrayList<>();
        for (int i = 1; i <= dataSize; i++) {
            viewHistories.add(new ViewHistory(
                    (long) i,
                    null,
                    null,
                    null
            ));


            //JDBC Template의 batchUpdate를 이용하여 배치 사이즈 단위로 bulk Insert
            if (i % batchSize == 0) {
                System.out.println(i);
                jdbcTemplate.batchUpdate(sql, viewHistories, batchSize,
                        (PreparedStatement ps, ViewHistory v) -> {
                            ps.setLong(1, v.getViewHistoryId());
                            ps.setLong(2, faker.number().numberBetween(1, 1_000_000));
                            ps.setLong(3, faker.number().numberBetween(1, 1_000_000));
                        });
                viewHistories.clear();
            }
        }
    }
}
