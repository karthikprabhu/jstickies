/*
 * SyncSettings is used to represent the synchronization settings. It contains the following synchronization information :
 *	- Interval - the intervals at which to run the Synchronizer thread (default: 5min)
 *	- Provider - the authorized Provider 
 * 	- Authorization Information - authorization info that is required to access the server. (Eg: OAuth Access Tokens) 
 * 
 * All this information will be saved to the synchronization settings file once synchronization has been configured by the user. 
 */

package com.jstickies.data.sync;

import java.io.File;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jstickies.JStickies;
import com.jstickies.data.sync.provider.Provider;

public class SyncSettings implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private static File SYNC_FILE = new File(JStickies.NOTE_FOLDER + "/sync");
	private static final Logger logger = LoggerFactory.getLogger(SyncSettings.class);
	
	private int syncInterval; //Synchronization interval in minutes
	private Class<Provider> provider;
	private Object authInfo;
	
	/*
	 * Creates SyncSettings with default settings and p as the Provider.
	 */
	public SyncSettings(Class<Provider> p) {
		provider = p;
		syncInterval = 5;
		authInfo = null;
	}
	
	/*
	 * Sets the authorization information.
	 */
	public void setAuthInfo(Object obj) {
		authInfo = obj;
		logger.debug("Authorization info changed");
	}
	
	/*
	 * Returns the authorization information.
	 */
	public Object getAuthInfo() {
		return authInfo;
	}
	
	/*
	 * Sets the synchronization interval. The interval must be provided in minutes. 
	 */
	public void setInterval(int interval) {
		syncInterval = interval;
		logger.debug("Synchronization interval changed : {}", interval);
	}
	
	/*
	 * Returns the synchronization interval.
	 */
	public int getInterval() {
		return syncInterval;
	}
	
	/*
	 * Sets the Provider.
	 */
	public void setProvider(Class<Provider> p) {
		provider = p;
		logger.debug("Synchronization provider changed!");
	}
	
	/*
	 * Returns the Provider.
	 */
	public Class<Provider> getProvider() {
		return provider;
	}
	
	/*
	 * Checks whether the synchronization settings file exists.
	 */
	public static boolean exists() {
		return SYNC_FILE.exists();
	}
	
	/*
	 * De-serializes the stored object from file.  
	 */
	public static SyncSettings getSettings() {
		logger.info("Loading SyncSettings from file");
		SyncSettings syncSettings = (SyncSettings) JStickies.loadFile(SYNC_FILE);
		return syncSettings;
	}
	
	/*
	 * Saves the settings to file. 
	 */
	public void saveSettings() {
		logger.info("Saving SyncSettings file");
		JStickies.saveFile(SYNC_FILE, this);
	}
	
	/*
	 * Deletes the settings file
	 */
	public void deleteSettings() {
		logger.info("Deleting SyncSettings file");
		SYNC_FILE.delete();
	}
}
