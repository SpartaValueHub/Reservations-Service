package com.sparta.reservations_service.adaptor.in.web.vo;

import com.sparta.reservations_service.domain.model.ReservationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

// 내 예약 목록 카드 응답
@Getter
@Builder
public class MyReservationItemVo {

	// API 식별자
	private final String reservationId;
	// Chat 방 Mongo ObjectId
	private final String chatRoomId;
	// 상품 게시글 UUID
	private final String productPostUuid;
	// 거래 예정 일시
	private final Instant scheduledAt;
	// 장소명
	private final String placeName;
	// 예약 상태
	private final ReservationStatus status;
}
