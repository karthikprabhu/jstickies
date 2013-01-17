/*
 * Abstract class representing a synchronization provider. The methods specified in this class is used by the Synchronizer to synchronize
 * notes to a server. All providers must implement all the abstract methods.
 */

package com.jstickies.data.sync.provider;

import java.io.File;
import java.io.FilenameFilter;
import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jstickies.JStickies;
import com.jstickies.data.sync.SyncSettings;

public abstract class Provider {
	private static final Logger logger = LoggerFactory.getLogger(Provider.class);
	
	/*
	 * Gets the display name of the Provider.
	 */
	public abstract String getDisplayName();
	
	/*
	 * Gets the display icon/logo of the Provider.
	 */
	public abstract ImageIcon getIcon();
	
	/*
	 * Returns the username of the Provider account that is linked with JStickies .
	 */
	public abstract String getUsername();
	
	/*
	 * Authorizes the user's account with JStickies. This method should take care of the entire flow of authorization and also save any 
	 * authorization information that will be used later using Provider#saveAuthInfo(Object obj).
	 */
	public abstract void authorize();
	
	/*
	 * Unauthorizes or unlinks the user's account with JStickies
	 */
	public abstract void unAuthorize();
	
	/*
	 * Returns the list of files on the server as a String array.
	 */
	public abstract String[] getFiles();
	
	/*
	 * Uploads the file diskFile from the local disk to the server with the file name serverFile.
	 */
	public abstract void uploadFile(File diskFile, String serverFile, boolean overwrite);
	
	/*
	 * Downloads the server file with the file name serverFile into the local disk file diskFile.
	 */
	public abstract void downloadFile(String serverFile, File diskFile);
	
	/*
	 * Checks if there is a file on the server with the file name serverFile. If so, then returns true, else false.
	 */
	public abstract boolean fileExists(String serverFile);
	
	/*
	 * Returns the Provider's server address
	 */
	public abstract String getServerAddress();
	
	/*
	 * Creates a SyncSettings file with the default settings and saves the authorization information. Eg: OAuth Access Tokens can be saved 
	 * after the authorization process using this method and retrieved later to access the API using Provider#getAuthInfo(). 
	 */
	@SuppressWarnings("unchecked")
	void saveAuthInfo(Object obj){
		SyncSettings settings = new SyncSettings((Class<Provider>) this.getClass());
		settings.setAuthInfo(obj);
		settings.saveSettings();
		JStickies.SYNC_SETTINGS = settings;
	}
	
	/*
	 * Returns the authorization information that was saved into the SyncSettings file.
	 */
	Object getAuthInfo(){
		return JStickies.SYNC_SETTINGS.getAuthInfo();
	}
	
	/*
	 * Returns all the available providers.
	 */
	@SuppressWarnings("unchecked")
	public static Class<Provider>[] getAvailableProviders() {
		logger.info("Finding all available Providers");
		Class<Provider>[] providers = null;
		String packagePath = Provider.class.getPackage().getName().replaceAll("\\.", "/"); //Get package name and convert it to a relative path
		File packageFile = new File(ClassLoader.getSystemClassLoader().getResource(packagePath).getFile()); //Create a File object for the package
		
		String[] classFiles = packageFile.list(new FilenameFilter() {
			@Override
			public boolean accept(File arg0, String arg1) {
				if(arg1.endsWith(".class") && !arg1.startsWith("Provider") && !arg1.contains("$"))
					return true;
				return false;
			}
		});
		providers = new Class[classFiles.length];
		logger.info("{} Provider(s) found", classFiles.length);
		
		for(int i=0; i<classFiles.length; i++) {
			String classPath = packagePath + "/" + classFiles[i].replace(".class", ""); //Relative path of the class
			String className = classPath.replaceAll("/", "\\.");
			try {
				providers[i] = (Class<Provider>) Class.forName(className);	
			} 
			catch (ClassNotFoundException e) { 
				logger.error("Error finding Provider class : {}", e.getMessage());
			}
		}
		
		return providers;
	}
}
