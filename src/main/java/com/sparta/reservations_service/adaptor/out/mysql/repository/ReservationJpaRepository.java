package com.sparta.reservations_service.adaptor.out.mysql.repository;

import com.sparta.reservations_service.adaptor.out.mysql.entity.ReservationEntity;
import com.sparta.reservations_service.domain.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

// reservations JPA Repository
public interface ReservationJpaRepository extends JpaRepository<ReservationEntity, Long> {

	boolean existsByChatRoomIdAndStatus(String chatRoomId, ReservationStatus status);
}
