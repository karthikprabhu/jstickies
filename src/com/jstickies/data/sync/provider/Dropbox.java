/*
 * This class represents the Dropbox Provider. It uses of the DropboxAPI to implement all the methods specified in the Provider interface.
 */

package com.jstickies.data.sync.provider;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.WebAuthSession;
import com.dropbox.client2.session.WebAuthSession.WebAuthInfo;
import com.jstickies.JStickies;

public class Dropbox extends Provider {
	
	final static private String APP_KEY = "tifrmenulsvqqqm", 
			APP_SECRET = "kkdlxtmrec09qsr";
	final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
	final static private String DISPLAY_NAME = "Dropbox";
	final static private Logger logger = LoggerFactory.getLogger(Dropbox.class);
	
	private DropboxAPI<WebAuthSession> DBAPI;
	
	public Dropbox() {}
	
	/*
	 * Returns a String containing the display name
	 */
	@Override
	public String getDisplayName() {
		return DISPLAY_NAME;
	}
	
	/*
	 * Returns an ImageIcon containing the logo/icon of Dropbox
	 */
	@Override
	public ImageIcon getIcon() {
		return JStickies.loadImage(JStickies.MEDIA_FOLDER + "/dropbox-logo.png");
	}

	/*
	 * Returns the username of the Dropbox account that is linked with JStickies
	 */
	@Override
	public String getUsername() {
		initSession();
		String name = null;
		
		try {
			name = DBAPI.accountInfo().displayName;
		} 
		catch (DropboxException e) {
			logger.error("Error getting username from Dropbox server : {}", e.getMessage());
		}
		
		return name;
	}

	/*
	 * Authorizes the user's Dropbox account with JStickies
	 */
	public void authorize() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(null, "Please wait while JStickies connects to the server..", "Connecting to server", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		
		AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        WebAuthSession session = new WebAuthSession(appKeys, ACCESS_TYPE);
        WebAuthInfo authInfo = null;
        boolean er = false;
		
        try {
        	logger.info("Getting authorization information from Dropbox");
			authInfo = session.getAuthInfo();
	        Desktop.getDesktop().browse(new URL(authInfo.url).toURI());
	        
	        JOptionPane.showMessageDialog(null, "<html>An authorization page has been opened in your default browser. Please allow JStickies<br>" +
	        		"access to your Dropbox account and press OK when done</html>", "Dropbox Authorization", JOptionPane.INFORMATION_MESSAGE);
	        
	        logger.info("Trying to retrieve access token from Dropbox");
	        session.retrieveWebAccessToken(authInfo.requestTokenPair);
		}
        catch (DropboxException e) {
        	logger.error("Error during authorization : {}", e.getMessage());
        	er = true;
        } 
        catch (MalformedURLException e) {
        	logger.error("Error during authorization : {}", e.getMessage());
        	er = true;
		} 
        catch (IOException e) {
        	logger.error("Error during authorization : {}", e.getMessage());
        	er = true;
		} 
        catch (URISyntaxException e) {
        	logger.error("Error during authorization : {}", e.getMessage());
        	er = true;
		}
        
        if(er)
        	JOptionPane.showMessageDialog(null, "Error getting authentication information from server! Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        else {
	        AccessTokenPair tokens = session.getAccessTokenPair();
	        logger.info("Access token retrieved. Key : {}, Secret : {}", tokens.key, tokens.secret);
	        saveAuthInfo(tokens);
        }
	}

	/*
	 * Unlinks user's Dropbox account with JStickies
	 */
	@Override
	public void unAuthorize() {}
	
	public String getServerAddress() {
		AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
		AccessTokenPair token = (AccessTokenPair) getAuthInfo();
		WebAuthSession session = new WebAuthSession(appKeys, ACCESS_TYPE, token);
		return session.getWebServer();
	}

	/*
	 * Retrieves all the files accessible to JStickies from Dropbox
	 */
	@Override
	public String[] getFiles() {
		ArrayList<String> fileNames = new ArrayList<String>();
		initSession();
		
		try {
			logger.info("Getting files from server");
			Entry entry = DBAPI.metadata("/", 0, null, true, null);
			
			logger.info("{} files found on the server", entry.size);
			List<Entry> l = entry.contents;
			for(int i=0; i<l.size(); i++)
				fileNames.add(l.get(i).fileName());
			
		} catch (DropboxException e) {
			logger.error("Error while getting files from Dropbox : {}", e.getMessage());
		}
		
		return fileNames.toArray(new String[fileNames.size()]);
	}

	/*
	 * Uploads the file diskFile to Dropbox with the name serverFile
	 */
	@Override
	public void uploadFile(File diskFile, String serverFile, boolean overwrite) {
		initSession();
		FileInputStream fis = null;
		Entry temp = null;
		logger.info("Uploading file {} to Dropbox", diskFile.getName());
		
		try {
			fis = new FileInputStream(diskFile);
			
			if(overwrite)
				temp = DBAPI.putFileOverwrite("/" + serverFile, fis, diskFile.length(), null);
			else
				temp = DBAPI.putFile("/" + serverFile, fis, diskFile.length(), null, null);
		} 
		catch (FileNotFoundException e) {
			logger.error("Error while uploading file {} to Dropbox : {}", diskFile.getName(), e.getMessage());
		} 
		catch (DropboxException e) {
			logger.error("Error while uploading file to Dropbox : {}", e.getMessage());
		}
		
		if(temp.bytes == diskFile.length())
			logger.info("File {} successfully uploaded", diskFile.getName());
		else
			logger.warn("File {} was not uploaded properly", diskFile.getName());
	}

	@Override
	public void downloadFile(String serverFile, File diskFile) {
		initSession();
		FileOutputStream fos = null;
		DropboxFileInfo temp = null;
		logger.info("Downloading {} from Dropbox", serverFile);
		
		try{
			fos = new FileOutputStream(diskFile);
			temp = DBAPI.getFile("/" + serverFile, null, fos, null);
		} 
		catch(FileNotFoundException e) {
			logger.error("Error while downloading file from Dropbox : {}", e.getMessage());
		} 
		catch (DropboxException e) {
			logger.error("Error while downloading file from Dropbox : {}", e.getMessage());
		}
		
		if(temp.getFileSize() == diskFile.length())
			logger.info("File {} successfully downloaded", serverFile);
		else
			logger.info("File {} was not downloaded properly", serverFile);
	}

	/*
	 * Checks whether the file ServerFile exists on Dropbox
	 */
	@Override
	public boolean fileExists(String serverFile) {
		initSession();
		
		try {
			logger.info("Searching for file {} on Dropbox", serverFile);
			List<Entry> results = DBAPI.search("/", serverFile, 0, false);
			Iterator<Entry> iterator = results.iterator();
			
			while(iterator.hasNext())
				if(iterator.next().fileName().equals(serverFile))
					return true;
		} 
		catch (DropboxException e) {
			logger.error("Error while searching for file {} on Dropbox : {}", serverFile, e.getMessage());
		}
		
		logger.info("Could not find file {}", serverFile);
		return false;
	}
	
	/*
	 * Initializes a web session with Dropbox
	 */
	private void initSession() {
		logger.info("Initializing a session with Dropbox");
		AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
		AccessTokenPair token = (AccessTokenPair) getAuthInfo();
		WebAuthSession session = new WebAuthSession(appKeys, ACCESS_TYPE, token);
		DBAPI = new DropboxAPI<WebAuthSession>(session);
	}
}
