package com.sparta.reservations_service.adaptor.out.mysql.entity;

import com.sparta.reservations_service.domain.model.ReservationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

// reservations 테이블 매핑
@Entity
@Table(
		name = "reservations",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_reservations_reservation_uuid", columnNames = "reservation_uuid")
		},
		indexes = {
				@Index(name = "ix_reservations_chat_room_status", columnList = "chat_room_id, status")
		}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "reservation_id")
	private Long reservationId;

	@Column(name = "reservation_uuid", nullable = false, length = 36)
	private String reservationUuid;

	@Column(name = "product_post_uuid", nullable = false, length = 36)
	private String productPostUuid;

	@Column(name = "chat_room_id", nullable = false, length = 24)
	private String chatRoomId;

	@Column(name = "buyer_uuid", nullable = false, length = 36)
	private String buyerUuid;

	@Column(name = "seller_uuid", nullable = false, length = 36)
	private String sellerUuid;

	@Column(name = "scheduled_at", nullable = false)
	private Instant scheduledAt;

	@Column(name = "place_name", nullable = false, length = 100)
	private String placeName;

	@Column(name = "address", length = 255)
	private String address;

	@Column(name = "latitude", precision = 10, scale = 7)
	private BigDecimal latitude;

	@Column(name = "longitude", precision = 10, scale = 7)
	private BigDecimal longitude;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private ReservationStatus status;

	@Column(name = "created_by", nullable = false, length = 36)
	private String createdBy;

	@Column(name = "canceled_by", length = 36)
	private String canceledBy;

	@Column(name = "canceled_at")
	private Instant canceledAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Builder
	private ReservationEntity(
			String reservationUuid,
			String productPostUuid,
			String chatRoomId,
			String buyerUuid,
			String sellerUuid,
			Instant scheduledAt,
			String placeName,
			String address,
			BigDecimal latitude,
			BigDecimal longitude,
			ReservationStatus status,
			String createdBy,
			String canceledBy,
			Instant canceledAt,
			Instant createdAt,
			Instant updatedAt
	) {
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
}
