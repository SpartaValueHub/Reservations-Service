package com.sparta.reservations_service.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

// 예약 등록 입력. 헤더 회원 UUID + 요청 스냅샷
@Getter
@Builder(toBuilder = true)
public class CreateReservationCommandDto {

	// 호출자 (X-Member-Uuid)
	private final String memberUuid;
	// Chat 방 Mongo ObjectId
	private final String chatRoomId;
	// 상품 게시글 UUID
	private final String productPostUuid;
	// 구매자 UUID
	private final String buyerUuid;
	// 판매자 UUID
	private final String sellerUuid;
	// 거래 예정 일시
	private final Instant scheduledAt;
	// 장소명
	private final String placeName;
	// 주소
	private final String address;
	// 위도
	private final Double latitude;
	// 경도
	private final Double longitude;
}
