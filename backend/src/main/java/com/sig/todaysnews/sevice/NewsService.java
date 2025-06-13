package com.sig.todaysnews.sevice;

import com.sig.todaysnews.dto.ArticleDto;
import com.sig.todaysnews.dto.ClusterDto;
import com.sig.todaysnews.persistence.entity.Article;
import com.sig.todaysnews.persistence.entity.Cluster;
import com.sig.todaysnews.persistence.entity.HotCluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface NewsService {
    default ClusterDto makeClusterDto(Cluster cluster, List<ArticleDto> articleDtoList, List<ClusterDto> relatedClusters) {
        ClusterDto clusterDto = ClusterDto.builder()
                .clusterId(cluster.getClusterId())
                .title(cluster.getTitle())
                .imgUrl(cluster.getImgUrl())
                .summary(cluster.getSummary())
                .articleList(articleDtoList)
                .regdate(cluster.getRegdate())
                .words(Arrays.stream(cluster.getWords().split(",")).toList())
                .size(cluster.getSize())
                .roomName("")
                .relatedClusters(relatedClusters)
                .build();
        return clusterDto;
    }

    default List<ClusterDto> makeRelatedClusters(Cluster cluster, int maxDepth) {
        List<ClusterDto> relatedClusters = new ArrayList<>();
        relatedClusters.add(makeClusterDto(cluster, null, null));
        for (int i=0; i<maxDepth; i++) {
            Cluster cur = cluster.getRelatedCluster();
            if (cur == null) break;
            relatedClusters.add(makeClusterDto(cur, null, null));
            cluster = cur;
        }
        return relatedClusters;
    }

    default ClusterDto makeHotClusterDto(HotCluster hotCluster) {
        ClusterDto clusterDto = ClusterDto.builder()
                .clusterId(hotCluster.getCluster().getClusterId())
                .title(hotCluster.getCluster().getTitle())
                .imgUrl(hotCluster.getCluster().getImgUrl())
                .summary(hotCluster.getCluster().getSummary())
                .words(Arrays.stream(hotCluster.getCluster().getWords().split(",")).toList())
                .size(hotCluster.getSize())
                .regdate(hotCluster.getCluster().getRegdate())
                .roomName(hotCluster.getRoomName())
                .build();
        return clusterDto;
    }

    default ArticleDto entityToDto(Article article) {
        String content = article.getContent().substring(0, Math.min(50, article.getContent().length()-1)) + "...";
        ArticleDto articleDto = ArticleDto.builder()
                .articleId(article.getArticleId())
                .title(article.getTitle())
                .imgUrl(article.getImgUrl())
                .url(article.getUrl())
                .content(content)
                .press(article.getPress())
                .regdate(article.getRegdate())
                .build();
        return articleDto;
    }

    default List<ArticleDto> makeArticleDtoList(List<Article> articleList) {
        List<ArticleDto> articleDtoList = articleList.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
        return articleDtoList;
    }
}
