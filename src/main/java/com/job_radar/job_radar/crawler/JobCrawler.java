package com.job_radar.job_radar.crawler;

import com.job_radar.job_radar.crawler.dto.JobPosting;
import java.util.List;

/**
 * 채용 공고 크롤러 인터페이스
 * 새로운 플랫폼 추가 시:
 * 1. 이 인터페이스를 구현한 크롤러 클래스 작성
 * 2. @Component 어노테이션 추가
 * 3. Service는 자동으로 감지하여 사용
 */
public interface JobCrawler {

    /**
     * 해당 플랫폼의 채용 공고를 크롤링
     * @param keyword 검색 키워드 ("백엔드", "Spring")
     * @param maxPages 크롤링할 최대 페이지 수
     * @return 크롤링된 채용 공고 리스트
     */
    List<JobPosting> crawl(String  keyword, int maxPages);

    /**
     * 플랫폼 이름 반환
     * @return 플랫폼 이름
     */
    String getPlatformName();

    /**
     * 크롤러 초기화 (WebDriver 세팅 등)
     * Service에서 크롤링 전에 호출
     */
    void initialize();

    /**
     * 리소스 정리 (Webdriver 종료)
     * Service에서 크롤링 후에 호출
     */
    void cleanup();
}
