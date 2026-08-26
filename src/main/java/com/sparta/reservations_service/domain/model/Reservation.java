package com.sparta.reservations_service.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

// 거래 예약 도메인
@Getter
public class Reservation {

	// 내부 PK. 저장 전에는 null
	private final Long reservationId;
	// API 식별자
	private final String reservationUuid;
	// 상품 게시글 UUID
	private final String productPostUuid;
	// Chat 방 Mongo ObjectId
	private final String chatRoomId;
	// 구매자
	private final String buyerUuid;
	// 판매자
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
	// 예약 상태
	private final ReservationStatus status;
	// 등록한 회원
	private final String createdBy;
	// 취소한 회원
	private final String canceledBy;
	// 취소 시각
	private final Instant canceledAt;
	// 생성 시각
	private final Instant createdAt;
	// 수정 시각
	private final Instant updatedAt;

	@Builder
	private Reservation(
			Long reservationId,
			String reservationUuid,
			String productPostUuid,
			String chatRoomId,
			String buyerUuid,
			String sellerUuid,
			Instant scheduledAt,
			String placeName,
			String address,
			Double latitude,
			Double longitude,
			ReservationStatus status,
			String createdBy,
			String canceledBy,
			Instant canceledAt,
			Instant createdAt,
			Instant updatedAt
	) {
		this.reservationId = reservationId;
		this.reservationUuid = reservationUuid;
		this.productPostUuid = productPostUuid;
		this.chatRoomId = chatRoomId;
		this.buyerUuid = buyerUuid;
		this.sellerUuid = sellerUuid;
		this.scheduledAt = scheduledAt;
		this.placeName = placeName;
		this.address = address;
		this.latitude = latitude;
		this.longitude = longitude;
		this.status = status;
		this.createdBy = createdBy;
		this.canceledBy = canceledBy;
		this.canceledAt = canceledAt;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static Reservation create(
			String productPostUuid,
			String chatRoomId,
			String buyerUuid,
			String sellerUuid,
			Instant scheduledAt,
			String placeName,
			String address,
			Double latitude,
			Double longitude,
			String createdBy
	) {
		Instant now = Instant.now();
		return Reservation.builder()
				.reservationUuid(UUID.randomUUID().toString())
				.productPostUuid(productPostUuid)
				.chatRoomId(chatRoomId)
				.buyerUuid(buyerUuid)
				.sellerUuid(sellerUuid)
				.scheduledAt(scheduledAt)
				.placeName(placeName)
				.address(address)
				.latitude(latitude)
				.longitude(longitude)
				.status(ReservationStatus.CONFIRMED)
				.createdBy(createdBy)
				.createdAt(now)
				.updatedAt(now)
				.build();
	}

	public static Reservation restore(
			Long reservationId,
			String reservationUuid,
			String productPostUuid,
			String chatRoomId,
			String buyerUuid,
			String sellerUuid,
			Instant scheduledAt,
			String placeName,
			String address,
			Double latitude,
			Double longitude,
			ReservationStatus status,
			String createdBy,
			String canceledBy,
			Instant canceledAt,
			Instant createdAt,
			Instant updatedAt
	) {
		return Reservation.builder()
				.reservationId(reservationId)
				.reservationUuid(reservationUuid)
				.productPostUuid(productPostUuid)
				.chatRoomId(chatRoomId)
				.buyerUuid(buyerUuid)
				.sellerUuid(sellerUuid)
				.scheduledAt(scheduledAt)
				.placeName(placeName)
				.address(address)
				.latitude(latitude)
				.longitude(longitude)
				.status(status)
				.createdBy(createdBy)
				.canceledBy(canceledBy)
				.canceledAt(canceledAt)
				.createdAt(createdAt)
				.updatedAt(updatedAt)
				.build();
	}

	public boolean isParty(String memberUuid) {
		return buyerUuid.equals(memberUuid) || sellerUuid.equals(memberUuid);
	}

	public boolean isSeller(String memberUuid) {
		return sellerUuid.equals(memberUuid);
	}

	public boolean isConfirmed() {
		return status == ReservationStatus.CONFIRMED;
	}

	public Reservation changeSchedule(
			Instant scheduledAt,
			String placeName,
			String address,
			Double latitude,
			Double longitude
	) {
		return Reservation.builder()
				.reservationId(this.reservationId)
				.reservationUuid(this.reservationUuid)
				.productPostUuid(this.productPostUuid)
				.chatRoomId(this.chatRoomId)
				.buyerUuid(this.buyerUuid)
				.sellerUuid(this.sellerUuid)
				.scheduledAt(scheduledAt)
				.placeName(placeName)
				.address(address)
				.latitude(latitude)
				.longitude(longitude)
				.status(this.status)
				.createdBy(this.createdBy)
				.canceledBy(this.canceledBy)
				.canceledAt(this.canceledAt)
				.createdAt(this.createdAt)
				.updatedAt(Instant.now())
				.build();
	}

	public Reservation cancel(String canceledBy) {
		Instant now = Instant.now();
		return Reservation.builder()
				.reservationId(this.reservationId)
				.reservationUuid(this.reservationUuid)
				.productPostUuid(this.productPostUuid)
				.chatRoomId(this.chatRoomId)
				.buyerUuid(this.buyerUuid)
				.sellerUuid(this.sellerUuid)
				.scheduledAt(this.scheduledAt)
				.placeName(this.placeName)
				.address(this.address)
				.latitude(this.latitude)
				.longitude(this.longitude)
				.status(ReservationStatus.CANCELED)
				.createdBy(this.createdBy)
				.canceledBy(canceledBy)
				.canceledAt(now)
				.createdAt(this.createdAt)
				.updatedAt(now)
				.build();
	}
}
