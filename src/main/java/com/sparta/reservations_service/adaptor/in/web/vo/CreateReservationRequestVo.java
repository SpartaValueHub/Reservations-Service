package com.sparta.reservations_service.adaptor.in.web.vo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

// 거래 예약 등록 요청
@Getter
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CreateReservationRequestVo {

	// Chat 방 Mongo ObjectId
	private String chatRoomId;
	// 상품 게시글 UUID
	private String productPostUuid;
	// 구매자 UUID
	private String buyerUuid;
	// 판매자 UUID
	private String sellerUuid;
	// 거래 예정 일시
	private Instant scheduledAt;
	// 장소명
	private String placeName;
	// 주소
	private String address;
	// 위도
	private Double latitude;
	// 경도
	private Double longitude;
}
