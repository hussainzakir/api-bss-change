package com.trinet.ambis.persistence.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "xbss_submit_payload")
@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class SubmitPayload implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4258185876492811814L;

	@Id
	@Column(name = "SUBMIT_STATUS_ID")
	private long submitStatusId;

	@OneToOne
	@JoinColumn(name = "SUBMIT_STATUS_ID")
	@MapsId
	private SubmitStatus submitStatus;

	@Lob
	@Type(type = "text")
	@Column(name = "PAYLOAD", length = Integer.MAX_VALUE)
	private String payload;

}
