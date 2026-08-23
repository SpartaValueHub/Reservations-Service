package com.sparta.reservations_service.application.port.out;

import com.sparta.reservations_service.domain.model.Reservation;
import com.sparta.reservations_service.domain.model.ReservationStatus;

import java.util.List;
import java.util.Optional;

// 예약 조회
public interface LoadReservationPort {

	boolean existsConfirmedByChatRoomId(String chatRoomId);

	Optional<Reservation> findConfirmedByChatRoomId(String chatRoomId);

	// 당사자(구매자 또는 판매자) 예약. updatedAt desc
	List<Reservation> findByPartyMemberUuid(String memberUuid);

	// 당사자 예약 중 지정 상태만. updatedAt desc
	List<Reservation> findByPartyMemberUuidAndStatus(String memberUuid, ReservationStatus status);
}
