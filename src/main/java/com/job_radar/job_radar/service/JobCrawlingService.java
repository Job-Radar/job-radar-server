package com.job_radar.job_radar.service;

import com.job_radar.job_radar.crawler.JobCrawler;
import com.job_radar.job_radar.crawler.dto.JobPosting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 크롤링 오케스트레이션 서비스
 * 핵심 원칙:
 * - 새로운 크롤러가 추가되어도 이 service 코드는 변경 불필요
 * - Spring이 모든 JobCrawler 구현체를 자동 주입
 * - 플랫폼별 크롤링 로직은 각 구현체가 책임
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobCrawlingService {
    // spring이 JobCrawler를 구현한 모든 빈을 자동 주입

    private final List<JobCrawler> crawlers;

    /**
     * 모든 플랫폼에서 동시에 크롤링
     */
    public List<JobPosting> crawlAllPlatforms(String keyword, int maxPages) {
        log.info("전체 플랫폼 크롤링 시작 - 키워드: {}, 크롤러 수: {}", keyword, crawlers.size());

        List<JobPosting> allResults = new ArrayList<>();

        for (JobCrawler crawler : crawlers) {
            try {
                crawler.initialize();
                List<JobPosting> results = crawler.crawl(keyword, maxPages);
                allResults.addAll(results);
            } catch (Exception e) {
                log.error("[{}] 크롤링 실패", crawler.getPlatformName(), e);
            } finally {
                crawler.cleanup();
            }
        }
        log.info("전체 크롤링 완료 - 총 {}개 공고 수집", allResults.size());
        return allResults;
    }

    /**
     * 특정 플랫폼만 크롤링
     */

    /**
     * 사용 가능한 플랫폼 목록 조회
     */
    public List<String> getAvailablePlatforms() {
        return crawlers.stream()
                .map(JobCrawler::getPlatformName)
                .toList();
    }
}
