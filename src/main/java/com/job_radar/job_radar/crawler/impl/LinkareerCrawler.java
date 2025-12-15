package com.job_radar.job_radar.crawler.impl;

import com.job_radar.job_radar.crawler.JobCrawler;
import com.job_radar.job_radar.crawler.dto.JobPosting;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 링커리어 크롤러 구현체
 */
@Slf4j
@Component
public class LinkareerCrawler implements JobCrawler {
    private static final String PLATFORM_NAME = "Linkareer";
    private static final String BASE_URL = "https://www.linkareer.com/list/recruit";

    private WebDriver driver;
    private WebDriverWait wait;

    @Override
    public void initialize(){
        log.info("[{}] 크롤러 초기화 시작", PLATFORM_NAME);

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        options.addArguments("--start-maximized");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

        // 헤드리스 모드 (운영 환경에서 사용)
        // options.addArguments("--headless");

        this.driver = new ChromeDriver(options);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        log.info("[{}] 크롤러 초기화 완료", PLATFORM_NAME);
    }

    @Override
    public List<JobPosting> crawl(String keyword, int maxPages) {
        List<JobPosting> results = new ArrayList<>();
        log.info("[Linkareer] 크롤링 시작 - 키워드 {}, 페이지: {}개", keyword, maxPages);

        try {
            // 기본 페이지 접속
            driver.get("https://linkareer.com/list/recruit");
            Thread.sleep(3000);

            // 검색창에 키워드 입력
            WebElement searchBox = wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector("#organizationName")
                    )
            );

            searchBox.clear();
            searchBox.sendKeys(keyword);

            log.info("[Linkareer] 검색어 '{}' 입력", keyword);

            // Enter 키 입력 또는 검색 버튼 클릭
            searchBox.sendKeys(Keys.ENTER);

            // 검색 결과 로딩 대기
            Thread.sleep(4000);

            log.info("[Linkareer] 검색 실행 완료, 크롤링 시작");

            // 페이지별 크롤링
            for (int page = 1; page <= maxPages; page++) {
                log.info("[Linkareer] 페이지 {} 크롤링 중", page);

                if (page > 1) {
                    clickPageNumber(page);
                    Thread.sleep(3000);
                }

                // tbody 대기
                WebElement tbody = wait.until(driver -> {
                    try {
                        WebElement table = driver.findElement(
                                By.cssSelector("table.recruit-list-table tbody")
                        );
                        List<WebElement> rows = table.findElements(
                                By.cssSelector("tr.activity-table-row")
                        );
                        return !rows.isEmpty() ? table : null;
                    } catch (Exception e) {
                        return null;
                    }
                });

                if (tbody == null) {
                    log.warn("[Linkareer] 페이지 {}번 테이블 없음", page);
                    continue;
                }

                List<WebElement> rows = tbody.findElements(
                        By.cssSelector("tr.activity-table-row")
                );
                log.info("[Linkareer] {} 페이지에서 {}개 공고 발견", page, rows.size());

                for (WebElement row : rows) {
                    try {
                        JobPosting job = parseJobPosting(row);
                        if (job != null && job.isValid()) {
                            results.add(job);
                        }
                    } catch (Exception e) {
                        log.warn("[Linkareer] 공고 파싱 실패: {}", e.getMessage());
                    }
                }
            }

            log.info("[Linkareer] 크롤링 완료 - 총 {}개 공고 수집", results.size());

        } catch (Exception e) {
            log.error("[Linkareer] 크롤링 중 오류", e);
        }

        return results;
    }

    private void clickPageNumber(int page) {
        try {
            String xpath = String.format(
                    "//button[contains(@class, 'button-page-number') and text()='%d']",
                    page
            );
            WebElement pageBtn = wait.until(
                    ExpectedConditions.elementToBeClickable(By.xpath(xpath))
            );
            pageBtn.click();
            log.info("[Linkareer] {}페이지 버튼 클릭 완료", page);
        } catch (Exception e) {
            log.warn("[Linkareer] {}페이지 클릭 실패", page);
        }
    }

    private JobPosting parseJobPosting(WebElement row) {
        try {
            // 플랫폼 ID 추출
            String platformId = extractPlatformId(row);

            // 제목 & URL
            WebElement titleElement = row.findElement(By.cssSelector(".item-recruit-name .recruit-name"));
            String title = titleElement.getText().trim();

            WebElement linkElement = row.findElement(By.cssSelector(".item-recruit-name a.recruit-link"));
            String url = linkElement.getAttribute("href");

            // 회사명
            String companyName = row.findElement(By.cssSelector(".item-recruit-company .company-name"))
                    .getText().trim();

            // 지역
            String location = row.findElement(By.cssSelector(".item-location .short-info-typo"))
                    .getText().trim();

            // 고용 형태
            String employmentType = row.findElement(By.cssSelector(".item-recruit-type .short-info-typo"))
                    .getText().trim();

            // 마감일
            String deadline = row.findElement(By.cssSelector(".item-recruit-close .short-info-typo"))
                    .getText().trim();

            return JobPosting.builder()
                    .platform("LINKAREER")
                    .platformId(platformId)
                    .title(title)
                    .companyName(companyName)
                    .url(url)
                    .location(location)
                    .employmentType(employmentType)
                    .deadline(deadline)
                    .build();

        } catch (Exception e) {
            log.warn("[Linkareer] 공고 파싱 실패", e);
            return null;
        }
    }

    private String buildUrl(String keyword, int page) {
        log.info("[Linkareer] buildUrl 호출 - keyword: '{}', page: {}", keyword, page);

        try {
            // 키워드 URL 인코딩 (UTF-8)
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8.toString());

            String url = String.format(
                    "%s?filterBy_activityTypeID=5&filterBy_q=%s&filterBy_status=OPEN&orderBy_direction=DESC&orderBy_field=RECENT&page=%d",
                    BASE_URL,
                    encodedKeyword,
                    page
            );

            return url;

        } catch (UnsupportedEncodingException e) {
            log.error("[Linkareer] URL 인코딩 실패", e);
            // fallback: 인코딩 없이 반환
            return String.format(
                    "%s?filterBy_activityTypeID=5&filterBy_q=%s&filterBy_status=OPEN&orderBy_direction=DESC&orderBy_field=RECENT&page=%d",
                    BASE_URL,
                    keyword,
                    page
            );
        }
    }

    @Override
    public String getPlatformName(){
        return PLATFORM_NAME;
    }

    @Override
    public void cleanup(){
        if (driver != null){
            driver.quit();
            log.info("[{}] 크롤러 리소스 정리 완료", PLATFORM_NAME);
        }
    }

    private JobPosting extractJobPosting(WebElement row) {
        // Platform ID 추출 (data-activityid 속성)
        String platformId = extractPlatformId(row);

        // 회사명
        String companyName = row.findElement(
                By.cssSelector(".item-recruit-company .company-name")
        ).getText().trim();

        // 공고명 & URL
        WebElement linkElement = row.findElement(
                By.cssSelector(".item-recruit-name a.recruit-link")
        );
        String title = linkElement.findElement(By.cssSelector(".recruit-name"))
                .getText()
                .trim();
        String relativeUrl = linkElement.getAttribute("href");
        String url = relativeUrl.startsWith("http")
                ? relativeUrl
                : "https://linkareer.com" + relativeUrl;

        // 직무 카테고리
        String category = "";
        try {
            category = row.findElement(By.cssSelector(".recruit-category"))
                    .getText()
                    .trim();
        } catch (Exception e) {
            // 카테고리 없는 경우
        }

        // 채용형태
        String employmentType = row.findElement(
                By.cssSelector(".item-recruit-type .short-info-typo")
        ).getText().trim();

        // 근무지역
        String location = row.findElement(
                By.cssSelector(".item-location .short-info-typo")
        ).getText().trim();

        // 마감일
        String deadline = row.findElement(
                By.cssSelector(".item-recruit-close .short-info-typo")
        ).getText().trim();

        return JobPosting.builder()
                .platform(PLATFORM_NAME)
                .platformId(platformId)
                .title(title)
                .companyName(companyName)
                .url(url)
                .location(location)
                .experience(category)  // 카테고리를 경력으로 매핑
                .education("")         // 학력 정보 없음
                .employmentType(employmentType)
                .deadline(deadline)
                .salary("")            // 연봉 정보 없음
                .build();
    }

    private String extractPlatformId(WebElement row) {
        try {
            String activityId = row.getAttribute("data-activityid");
            if (activityId != null && !activityId.isEmpty()) {
                return activityId;
            }
        } catch (Exception e) {
            log.warn("[{}] Platform ID 추출 실패", PLATFORM_NAME);
        }

        // fallback: URL에서 추출 시도
        try {
            String href = row.findElement(By.cssSelector("a.recruit-link"))
                    .getAttribute("href");
            if (href.contains("/activity/")) {
                return href.substring(href.lastIndexOf("/") + 1);
            }
        } catch (Exception e) {
            log.warn("[{}] URL에서 ID 추출 실패", PLATFORM_NAME);
        }

        return String.valueOf(System.currentTimeMillis()); // 최후의 fallback
    }
}
