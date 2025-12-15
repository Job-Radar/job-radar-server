package com.job_radar.job_radar.crawler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 모든 플랫폼에서 공통으로 사용하는 채용 공고 DTO
 * 각 크롤러는 자신의 사이트 구조에 맞게 DTO로 변환
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPosting {

    private String platform;
    private String platformId;

    private String title;
    private String companyName;
    private String url;

    private String location;
    private String experience;
    private String education;
    private String employmentType;
    private String deadline;
    private String salary;

    /**
     * 필수 정보 검증
     */
    public boolean isValid() {
        return title != null && !title.isEmpty()
                && companyName != null && !companyName.isEmpty()
                && url != null && !url.isEmpty();
    }
}
