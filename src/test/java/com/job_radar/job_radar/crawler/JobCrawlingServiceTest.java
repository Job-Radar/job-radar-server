package com.job_radar.job_radar.crawler;

import com.job_radar.job_radar.crawler.impl.LinkareerCrawler;
import com.job_radar.job_radar.service.JobCrawlingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.job_radar.job_radar.crawler.dto.JobPosting;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class JobCrawlingServiceTest {

    @Autowired
    private JobCrawlingService crawlingService;

    @Test
    @DisplayName("사용 가능한 플랫폼 목록 조회")
    void getAvailablePlatformsTest(){
        //when
        List<String> platforms = crawlingService.getAvailablePlatforms();

        //then
        assertThat(platforms).contains("Linkareer");
        System.out.println("사용 가능한 플랫폼: " + platforms);
    }

    @Test
    @DisplayName("링커리어 크롤링 테스트")
    void linkareerCrawlTest() {
        // given
        String keyword = "백엔드";
        int maxPages = 2;

        // when
        LinkareerCrawler crawler = new LinkareerCrawler();
        crawler.initialize();
        List<JobPosting> results = crawler.crawl(keyword, maxPages);
        crawler.cleanup();

        // then
        assertThat(results).isNotEmpty();

        results.stream().limit(5).forEach(job -> {
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("플랫폼: " + job.getPlatform());
            System.out.println("ID: " + job.getPlatformId());
            System.out.println("제목: " + job.getTitle());
            System.out.println("회사: " + job.getCompanyName());
            System.out.println("지역: " + job.getLocation());
            System.out.println("형태: " + job.getEmploymentType());
            System.out.println("마감: " + job.getDeadline());
            System.out.println("URL: " + job.getUrl());
            System.out.println();
        });
    }

    @Test
    @DisplayName("전체 플랫폼 크롤링")
    void crawlAllPlatformsTest() {
        //given
        String keyword = "Spring";
        int maxPages = 1;

        //when
        List<JobPosting> results = crawlingService.crawlAllPlatforms(keyword, maxPages);

        // then
        assertThat(results).isNotEmpty();

        // 플랫폼별 개수 출력
        results.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        JobPosting::getPlatform,
                        java.util.stream.Collectors.counting()
                ))
                .forEach((platform, count) ->
                        System.out.printf("[%s] %d개 공고 수집%n", platform, count)
                );
    }

}
