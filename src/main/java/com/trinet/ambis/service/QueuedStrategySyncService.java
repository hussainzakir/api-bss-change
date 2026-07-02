package com.trinet.ambis.service;

/**
 * This interface defines the queued strategy sync process that can be called
 * by a process scheduler.
 */
public interface QueuedStrategySyncService {

	/**
	 * Launch the next queued strategy sync process.
	 */
	void startScheduledStrategySyncProcess();

}
