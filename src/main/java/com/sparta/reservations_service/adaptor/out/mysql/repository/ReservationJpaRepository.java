package com.sparta.reservations_service.adaptor.out.mysql.repository;

import com.sparta.reservations_service.adaptor.out.mysql.entity.ReservationEntity;
import com.sparta.reservations_service.domain.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// reservations JPA Repository
public interface ReservationJpaRepository extends JpaRepository<ReservationEntity, Long> {

	boolean existsByChatRoomIdAndStatus(String chatRoomId, ReservationStatus status);

	Optional<ReservationEntity> findByChatRoomIdAndStatus(String chatRoomId, ReservationStatus status);
}
