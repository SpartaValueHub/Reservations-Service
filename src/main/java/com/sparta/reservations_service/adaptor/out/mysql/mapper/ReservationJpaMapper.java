package com.sparta.reservations_service.adaptor.out.mysql.mapper;

import com.sparta.reservations_service.adaptor.out.mysql.entity.ReservationEntity;
import com.sparta.reservations_service.domain.model.Reservation;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

// 도메인 <-> JPA 엔티티 매핑
@Component
public class ReservationJpaMapper {

	public ReservationEntity toEntity(Reservation reservation) {
		return ReservationEntity.builder()
				.reservationUuid(reservation.getReservationUuid())
				.productPostUuid(reservation.getProductPostUuid())
				.chatRoomId(reservation.getChatRoomId())
				.buyerUuid(reservation.getBuyerUuid())
				.sellerUuid(reservation.getSellerUuid())
				.scheduledAt(reservation.getScheduledAt())
				.placeName(reservation.getPlaceName())
				.address(reservation.getAddress())
				.latitude(toDecimal(reservation.getLatitude()))
				.longitude(toDecimal(reservation.getLongitude()))
				.status(reservation.getStatus())
				.createdBy(reservation.getCreatedBy())
				.canceledBy(reservation.getCanceledBy())
				.canceledAt(reservation.getCanceledAt())
				.createdAt(reservation.getCreatedAt())
				.updatedAt(reservation.getUpdatedAt())
				.build();
	}

	public Reservation toDomain(ReservationEntity entity) {
		return Reservation.restore(
				entity.getReservationId(),
				entity.getReservationUuid(),
				entity.getProductPostUuid(),
				entity.getChatRoomId(),
				entity.getBuyerUuid(),
				entity.getSellerUuid(),
				entity.getScheduledAt(),
				entity.getPlaceName(),
				entity.getAddress(),
				toDouble(entity.getLatitude()),
				toDouble(entity.getLongitude()),
				entity.getStatus(),
				entity.getCreatedBy(),
				entity.getCanceledBy(),
				entity.getCanceledAt(),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}

	private BigDecimal toDecimal(Double value) {
		return value == null ? null : BigDecimal.valueOf(value);
	}

	private Double toDouble(BigDecimal value) {
		return value == null ? null : value.doubleValue();
	}
}
