package com.trinet.ambis.service.model.notification;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.trinet.common.notification.model.MessageHeader;

@lombok.Generated
public class NotificationRequestParam {
	
	@NotNull
	@Size(min = 1) // At least one notification message in the list
	@Valid	// See additional validations in the NotficationMessage class. 
	private List<NotificationMessage> notificationMessages = new ArrayList<>();
	@NotNull
	private List<MessageHeader> bodyHeaders = new ArrayList<>();
	@NotNull
	private List<ArrayList<MessageHeader>>body = new ArrayList<>();
	private List<MessageHeader> attachmentHeaders = new ArrayList<>();
	@NotNull
	@Valid	// See additional validations in the DeliveryChannel class.
	private DeliveryChannel deliveryChannel;
	private String priority;
	private String sendType; // synch or asynch (default synch)
	private Date messageDeliveryTime;	// schedule time to send notification.
	private String defaultBody; 	
	
	public List<NotificationMessage> getNotificationMessages() {
		return notificationMessages;
	}

	public void setNotificationMessages(List<NotificationMessage> notificationMessages) {
		this.notificationMessages = notificationMessages;
	}

	public List<MessageHeader> getBodyHeaders() {
		return bodyHeaders;
	}

	public void setBodyHeaders(List<MessageHeader> headers) {
		this.bodyHeaders = headers;
	}

	public List<MessageHeader> getAttachmentHeaders() {
		return attachmentHeaders;
	}

	public void setAttachmentHeaders(List<MessageHeader> attachmentHeaders) {
		this.attachmentHeaders = attachmentHeaders;
	}

	public DeliveryChannel getDeliveryChannel() {
		return deliveryChannel;
	}

	public void setDeliveryChannel(DeliveryChannel deliveryChannel) {
		this.deliveryChannel = deliveryChannel;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getSendType() {
		return sendType;
	}

	public void setSendType(String sendType) {
		this.sendType = sendType;
	}

	public Date getMessageDeliveryTime() {
		return messageDeliveryTime;
	}

	public void setMessageDeliveryTime(Date messageDeliveryTime) {
		this.messageDeliveryTime = messageDeliveryTime;
	}

	public String getDefaultBody() {
		return defaultBody;
	}

	public void setDefaultBody(String defaultBody) {
		this.defaultBody = defaultBody;
	}

	public List<ArrayList<MessageHeader>> getBody() {
		return body;
	}

	public void setBody(List<ArrayList<MessageHeader>> body) {
		this.body = body;
	}

	@Override
	public String toString() {
		return "NotificationRequestParam [notificationMessages=" + notificationMessages + ", bodyHeaders=" + bodyHeaders
				+ ", body=" + body + ", attachmentHeaders=" + attachmentHeaders + ", deliveryChannel=" + deliveryChannel
				+ ", priority=" + priority + ", sendType=" + sendType + ", messageDeliveryTime=" + messageDeliveryTime
				+ ", defaultBody=" + defaultBody + "]";
	}
	
}