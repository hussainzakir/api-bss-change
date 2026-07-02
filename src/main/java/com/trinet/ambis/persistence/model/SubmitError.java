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
@Table(name = "xbss_submit_error")
@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class SubmitError implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 5420205000060758074L;

	@Id
	@Column(name = "SUBMIT_STATUS_ID")
	private long submitStatusId;

	@OneToOne
	@JoinColumn(name = "SUBMIT_STATUS_ID")
	@MapsId
	private SubmitStatus submitStatus;

	@Column(name = "ERROR_MSG")
	private String errorMsg;

	@Lob
	@Type(type = "text")
	@Column(name = "STACKTRACE", length = Integer.MAX_VALUE)
	private String stackTrace;

	@Column(name = "DISPLAY_MSG")
	private String displayMsg;

}
