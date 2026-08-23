package com.sparta.reservations_service.adaptor.out.mysql.repository;

import com.sparta.reservations_service.adaptor.out.mysql.entity.ReservationEntity;
import com.sparta.reservations_service.domain.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// reservations JPA Repository
public interface ReservationJpaRepository extends JpaRepository<ReservationEntity, Long> {

	boolean existsByChatRoomIdAndStatus(String chatRoomId, ReservationStatus status);

	Optional<ReservationEntity> findByChatRoomIdAndStatus(String chatRoomId, ReservationStatus status);

	@Query("""
			SELECT reservation FROM ReservationEntity reservation
			WHERE reservation.buyerUuid = :memberUuid OR reservation.sellerUuid = :memberUuid
			ORDER BY reservation.updatedAt DESC
			""")
	List<ReservationEntity> findByPartyMemberUuid(@Param("memberUuid") String memberUuid);

	@Query("""
			SELECT reservation FROM ReservationEntity reservation
			WHERE (reservation.buyerUuid = :memberUuid OR reservation.sellerUuid = :memberUuid)
			AND reservation.status = :status
			ORDER BY reservation.updatedAt DESC
			""")
	List<ReservationEntity> findByPartyMemberUuidAndStatus(
			@Param("memberUuid") String memberUuid,
			@Param("status") ReservationStatus status
	);
}
