package com.sparta.reservations_service.adaptor.out.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.reservations_service.domain.model.Reservation;
import com.sparta.reservations_service.domain.model.ReservationStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ReservationEventPayloadTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Test
	void created_mapsEntityFieldsToKafkaJsonKeys() throws Exception {
		Reservation reservation = Reservation.restore(
				1L,
				"44444444-4444-4444-8444-444444444444",
				"11111111-1111-4111-8111-111111111111",
				"67a1c2d3e4f5a6b7c8d9e0f1",
				"22222222-2222-4222-8222-222222222222",
				"33333333-3333-4333-8333-333333333333",
				Instant.parse("2026-08-26T03:00:00Z"),
				"해동병원 앞",
				"부산시",
				35.115,
				129.042,
				ReservationStatus.CONFIRMED,
				"33333333-3333-4333-8333-333333333333",
				null,
				null,
				Instant.parse("2026-08-26T01:00:00Z"),
				Instant.parse("2026-08-26T01:00:00Z")
		);

		ReservationEventPayload payload = ReservationEventPayload.created(reservation);
		JsonNode json = OBJECT_MAPPER.readTree(OBJECT_MAPPER.writeValueAsString(payload));

		assertEquals("CREATED", json.get("eventType").asText());
		assertEquals("11111111-1111-4111-8111-111111111111", json.get("productPostUuid").asText());
		assertEquals("44444444-4444-4444-8444-444444444444", json.get("reservationUuid").asText());
		assertEquals("67a1c2d3e4f5a6b7c8d9e0f1", json.get("chatRoomUuid").asText());
		assertEquals("2026-08-26T12:00:00+09:00", json.get("meetAt").asText());
		assertEquals("해동병원 앞", json.get("placeName").asText());
		assertEquals("33333333-3333-4333-8333-333333333333", json.get("sellerUuid").asText());
		assertEquals("22222222-2222-4222-8222-222222222222", json.get("buyerUuid").asText());
		assertEquals("2026-08-26T01:00:00Z", json.get("updatedAt").asText());

		assertFalse(json.has("chatRoomId"));
		assertFalse(json.has("scheduledAt"));
		assertFalse(json.has("status"));
		assertFalse(json.has("address"));
		assertFalse(json.has("latitude"));
		assertFalse(json.has("longitude"));
		assertEquals(9, countFields(json));
	}

	private int countFields(JsonNode json) {
		int count = 0;
		Iterator<String> names = json.fieldNames();
		while (names.hasNext()) {
			names.next();
			count++;
		}
		return count;
	}
}
