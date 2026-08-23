package com.sparta.reservations_service.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

// 예약 수정 입력. 보낸 필드만 반영
@Getter
@Builder
public class UpdateReservationCommandDto {

	// 호출자 (X-Member-Uuid)
	private final String memberUuid;
	// API 식별자 reservation_uuid
	private final String reservationId;
	// 거래 예정 일시. null이면 유지
	private final Instant scheduledAt;
	// 장소명. null이면 유지
	private final String placeName;
	// 주소. addressSpecified가 true일 때만 반영
	private final String address;
	// 요청에 address 필드가 있었는지
	private final boolean addressSpecified;
	// 위도. null이면 유지. 보내면 longitude와 함께
	private final Double latitude;
	// 경도. null이면 유지. 보내면 latitude와 함께
	private final Double longitude;
}
