package com.sparta.reservations_service.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

// 내 예약 목록 결과
@Getter
@Builder
public class MyReservationListResultDto {

	// 카드 목록. 없으면 빈 리스트
	private final List<MyReservationItemDto> reservations;
}
