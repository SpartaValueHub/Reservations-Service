package com.sparta.reservations_service.application.port.in.dto;

import com.sparta.reservations_service.domain.model.ReservationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

// 예약 상세 응답용 결과
@Getter
@Builder
public class ReservationDetailResultDto {

	private final String reservationId;
	private final String chatRoomId;
	private final String productPostUuid;
	private final String buyerUuid;
	private final String sellerUuid;
	private final Instant scheduledAt;
	private final String placeName;
	private final String address;
	private final Double latitude;
	private final Double longitude;
	private final ReservationStatus status;
	private final String createdBy;
	private final String canceledBy;
	private final Instant canceledAt;
	private final Instant createdAt;
	private final Instant updatedAt;
}
