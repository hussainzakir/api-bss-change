package com.trinet.ambis.service.model.notification;


/**
 * Delivery Channel could be either email or SMS; The vendor is either AWS_SES or Twillio. 
 * @author bfu
 *
 */
@lombok.Generated
public class DeliveryChannel {

	private String channel;
	private String vendor;
	
	public DeliveryChannel() {}
	public DeliveryChannel(String channel) {
		setChannel(channel);
	}
	
	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}
}