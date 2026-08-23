package com.sparta.reservations_service.adaptor.in.web.vo;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

// 내 예약 목록 응답
@Getter
@Builder
public class MyReservationListResponseVo {

	// 카드 목록. 없으면 빈 배열
	private final List<MyReservationItemVo> reservations;
}
