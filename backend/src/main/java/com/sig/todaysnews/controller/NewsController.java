package com.sig.todaysnews.controller;

import com.sig.todaysnews.dto.ClusterDto;
import com.sig.todaysnews.sevice.NewsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
public class NewsController {
    private final NewsServiceImpl newsService;

    @GetMapping("/proposal")
    public ResponseEntity<List<ClusterDto>> getProposal(
            LocalDate date
    ) {
        if (date == null) date = LocalDate.now();
        List<ClusterDto> res = newsService.getProposal(date);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/section")
    public ResponseEntity<List<ClusterDto>> getSection(
            Long sid,
            LocalDate date
    ) {
        if (date == null) date = LocalDate.now();
        List<ClusterDto> res = newsService.getSection(sid, date);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/hottopic")
    public ResponseEntity<List<ClusterDto>> getHotClusters(
            LocalDate date
    ) {
        if (date == null) date = LocalDate.now();
        List<ClusterDto> res = newsService.getHotClusters(date);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/cluster")
    public ResponseEntity<ClusterDto> getCluster(
            Long cid
    ) {
        ClusterDto res = newsService.getCluster(cid);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/cluster")
    public ResponseEntity<Void> deleteCluster(
            Long cid
    ) {
        newsService.deleteCluster(cid);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/article")
    public ResponseEntity<Void> deleteArticle(
            Long aid
    ) {
        newsService.deleteArticle(aid);
        return ResponseEntity.ok().build();
    }
}
