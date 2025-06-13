package com.sig.todaysnews.sevice;

import com.sig.todaysnews.dto.ClusterDto;
import com.sig.todaysnews.persistence.entity.*;
import com.sig.todaysnews.persistence.repository.*;
import com.sig.todaysnews.security.util.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Component
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {
    private final UserRepository userRepository;
    private final ViewHistoryRepository viewHistoryRepository;
    private final ClusterRepository clusterRepository;
    private final HotClusterRepository hotClusterRepository;
    private final ArticleRepository articleRepository;
    private final SectionRepository sectionRepository;

    @Transactional
    public List<ClusterDto> getProposal(LocalDate date) {
        String username = AuthenticationUtil.getCurrentUsername().get();
        List<Cluster> clusters;

        if (!username.equals("anonymousUser")) {
            User user = userRepository.findOneWithAuthoritiesByUsername(username).get();
            LocalDateTime dateTime = LocalDateTime.of(date.plusDays(1), LocalTime.of(0, 0, 0));
            List<ViewHistory> viewHistories = viewHistoryRepository.findAllWithUser(user, dateTime, PageRequest.of(0, 40));

            List<Cluster> relatedClusters = new ArrayList<>();
            for (ViewHistory vh : viewHistories) {
                Cluster cluster = vh.getCentroid().getCluster();
                if (cluster != null) relatedClusters.add(vh.getCentroid().getCluster());
            }

            clusters = clusterRepository.findAllWithRelatedClusters(relatedClusters, date, PageRequest.of(0, 6));
        }
        else {
            clusters = new ArrayList<>();
        }

        List<Section> sections = sectionRepository.findAll();
        int remains = 6 - clusters.size();
        for (int i=0; i<remains; i++) {
            Long maxSize = clusterRepository.countAllBySidAndDate(sections.get(i).getSectionId(), date);
            int idx = (int)(Math.random() * maxSize);
            if (0 < maxSize) clusters.add(clusterRepository.findOneWithRandomBySidAndDate(sections.get(i).getSectionId(), date, PageRequest.of(idx, 1)).get(0));
        }

        List<ClusterDto> clusterDtoList = clusters.stream()
                .map(cluster -> makeClusterDto(
                        cluster,
                        null,
                        null
                ))
                .collect(Collectors.toList());
        return clusterDtoList;
    }

    public List<ClusterDto> getSection(Long sid, LocalDate date) {
        List<Cluster> clusters = clusterRepository.findClustersBySidAndDate(sid, date);

        List<ClusterDto> clusterDtoList = clusters.stream()
                .map(cluster -> makeClusterDto(
                        cluster,
                        null,
                        null
                ))
                .collect(Collectors.toList());
        return clusterDtoList;
    }

    @Transactional
    public List<ClusterDto> getHotClusters(LocalDate date) {
        List<HotCluster> hotClusters = hotClusterRepository.findHotClustersByDate(date);
        List<ClusterDto> clusterDtoList = hotClusters.stream()
                .map(hotCluster -> makeHotClusterDto(hotCluster))
                .collect(Collectors.toList());
        return clusterDtoList;
    }

    @Transactional
    public ClusterDto getCluster(Long cid) {
        Cluster cluster = clusterRepository.findById(cid).orElse(null);

        ClusterDto clusterDto = null;
        if (cluster != null) {
            clusterDto = makeClusterDto(
                    cluster,
                    makeArticleDtoList(articleRepository.findArticlesByCid(cluster.getClusterId())),
                    makeRelatedClusters(cluster, 3)
            );
            String username = AuthenticationUtil.getCurrentUsername().get();
            if (!username.equals("anonymousUser")) {
                viewHistoryRepository.save(
                        ViewHistory.builder()
                                .regdate(LocalDateTime.now())
                                .centroid(cluster.getCentroid())
                                .user(userRepository.findOneWithAuthoritiesByUsername(username).get()).build()
                );
            }
        }

        return clusterDto;
    }

    public void deleteCluster(Long cid) {
        clusterRepository.deleteById(cid);
    }

    public void deleteArticle(Long aid) {
        Article article = articleRepository.findById(aid).get();
        article.setCluster(null);
        articleRepository.save(article);
    }
}

