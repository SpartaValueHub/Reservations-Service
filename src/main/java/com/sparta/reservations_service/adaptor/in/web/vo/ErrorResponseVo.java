package com.sparta.reservations_service.adaptor.in.web.vo;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

// 공통 에러 응답
@Getter
@Builder
public class ErrorResponseVo {

	// ISO-8601 시각
	private final Instant timestamp;
	// HTTP 상태 코드
	private final int status;
	// 안정적 에러 코드
	private final String code;
	// 사용자 메시지
	private final String message;
	// 요청 경로
	private final String path;
}
