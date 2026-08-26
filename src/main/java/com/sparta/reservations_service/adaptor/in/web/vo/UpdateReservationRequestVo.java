package com.sparta.reservations_service.adaptor.in.web.vo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

// 거래 예약 수정 요청. 보낸 필드만 변경
@Getter
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class UpdateReservationRequestVo {

	private Instant scheduledAt;
	private String placeName;
	private Double latitude;
	private Double longitude;

	@JsonIgnore
	private String address;

	@JsonIgnore
	private boolean addressSpecified;

	@JsonProperty("address")
	public void setAddress(String address) {
		this.address = address;
		this.addressSpecified = true;
	}
}
