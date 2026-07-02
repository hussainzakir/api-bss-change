package com.trinet.ambis.service.model.notification;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import com.trinet.ambis.common.BSSApplicationConstants;
	
@lombok.Generated
public class NotificationMessage {
	
	private String companyId;	
	private String employeeId;	
	private String messageId;	 
	private String textMsg;		  
	private String htmlMsg;		
	private String subject;		
	private Boolean createAttachment = false;
	private String messageType;	
	private Boolean transformRequired = true;
	private List<Recipient> recipients = new ArrayList<>(); 
	
	private Recipient from = new Recipient();	

	public NotificationMessage() {}
	public NotificationMessage(String id, Recipient recipient) {
		Assert.notNull(recipient, BSSApplicationConstants.RECIPIENT_ERROR);
		List<Recipient> recipients = new ArrayList<>();
		recipients.add(recipient);
		this.messageId = id;
		this.setRecipients(recipients);
	}
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	public Boolean getCreateAttachment() {
		return createAttachment;
	}

	public void setCreateAttachment(Boolean createAttachment) {
		this.createAttachment = createAttachment;
	}
	public String getMessageId() {
		return StringUtils.isBlank(messageId) 
				? java.util.UUID.randomUUID().toString()
				: messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
		
	public String getTextMsg() {
		return textMsg;
	}
	public void setTextMsg(String textMsg) {
		this.textMsg = textMsg;
	}
	public String getHtmlMsg() {
		return htmlMsg;
	}
	public void setHtmlMsg(String htmlMsg) {
		this.htmlMsg = htmlMsg;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}

	
	public List<Recipient> getRecipients() {
		return recipients;
	}
	public void setRecipients(List<Recipient> recipients) {
		this.recipients = recipients;
	}
	public Recipient getFrom() {
		return from;
	}
	public void setFrom(Recipient from) {
		this.from = from;
	}
	public String getMessageType() {
		return messageType;
	}

	public String getCompanyId() {
		return companyId;
	}
	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}
	public String getEmployeeId() {
		return employeeId;
	}
	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}
	
	public Boolean getTransformRequired() {
		return transformRequired;
	}
	public void setTransformRequired(Boolean transformRequired) {
		this.transformRequired = transformRequired;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("NotificationMessage");
		builder.append(" [");
		builder.append(String.format("messageId = %s", messageId));
		builder.append(String.format(", companyId = %s", companyId));
		builder.append(String.format(", employeeId = %s", employeeId));
		builder.append(String.format(", textMsg = %s", textMsg));
		builder.append(String.format(", htmlMsg = %s", htmlMsg));
		builder.append(String.format(", subject = %s", subject));
		builder.append(String.format(", messageType = %s", messageType));
		builder.append(String.format(", recipients = %s", recipients));
		builder.append(String.format(", from = %s", from));
		builder.append(" ]");
		return builder.toString();
	}
}