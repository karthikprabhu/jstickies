/*
 * This class creates a new Thread that performs the job of synchronization. It synchronizes the notes between the cloud server and the 
 * computer. Its main tasks include :
 * 	- Getting files (or metadata) from the server and comparing with local files
 * 	- Synchronizing the files
 * 	- Instantiating any newly downloaded notes
 */

package com.jstickies.data.sync;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jstickies.JStickies;
import com.jstickies.data.NoteData;
import com.jstickies.data.sync.provider.Provider;
import com.jstickies.gui.Note;

public class Synchronizer implements Runnable, FilenameFilter {
	
	private Provider provider;
	private int tries;
	private static final Logger logger = LoggerFactory.getLogger(Synchronizer.class);
	private static final int MAXTRIES = 3;
	
	public volatile long interval;
	public volatile boolean syncNow;
	public Thread t;
	
	/*
	 * Creates and starts a new Thread.
	 */
	public Synchronizer() {
		Class<Provider> providerClass = JStickies.SYNC_SETTINGS.getProvider();
		try {
			provider = providerClass.newInstance();
		} 
		catch (InstantiationException e) {
			logger.error("Error while instantiating Provider : {}", e.getMessage());
			return; //TODO Inform the user and re authorize!
		} 
		catch (IllegalAccessException e) {
			logger.error("Error while instantiating Provider : {}", e.getMessage());
			return;
		}
		
		interval = JStickies.SYNC_SETTINGS.getInterval() * 60 * 1000; //in milliseconds
		syncNow = false;
		tries = 0;
		
		JStickies.METADATA = (MetaData.metaDataFile.exists())? MetaData.getMetaData(MetaData.metaDataFile) : new MetaData();
		
		t = new Thread(this);
		logger.info("Starting new Thread : {}", t);
		t.start();
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				if(!syncNow)
					Thread.sleep(interval);
				else
					syncNow = false;
				
				if(tries == MAXTRIES) {
					logger.error("Maximum number of tries reached! Stopping Synchronizer");
					JStickies.SYNCHRONIZER = null;
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							JOptionPane.showMessageDialog(null, "<html>Synchronizer has been stopped due to network connectivity problems. Please <br>restart the application to start synchronization</html>", "Network Connection Error", JOptionPane.ERROR_MESSAGE);
						}
					});
					break;
				}
				
				InetAddress address = InetAddress.getByName(provider.getServerAddress());
				if(address.isReachable(1000))
					compare();
				else
					tries++;
			} catch (InterruptedException e) {
				logger.info("Thread interrupted. SyncNow : {}", syncNow);
				if(!syncNow)
					break;
			} catch (UnknownHostException e) {
				logger.error("Error connecting to host : " + e);
				tries++;
			} catch (IOException e) {
				logger.error("Error connecting to host : " + e);
				tries++;
			}
		}
	}
	
	/*
	 * Compares the local and server files and performs synchronization. 
	 */
	public void compare() {
		tries = 0; //reset no of tries
		logger.info("Synchronization started..");
		if(!provider.fileExists(MetaData.fileName)) { //First synchronization
			JStickies.METADATA.saveMetaData();
			upload(MetaData.metaDataFile, false);
			
			logger.info("Uploading all local files to server");
			Iterator<String> iterator = JStickies.METADATA.getFilenameIterator();
			while(iterator.hasNext())
				upload(new File(JStickies.NOTE_FOLDER + "/" + iterator.next()), false);
		}
		else {
			File tempFile = new File(JStickies.NOTE_FOLDER + "/" + "temp");
			logger.info("Downloading MetaData from server to file : {}", tempFile.getName());
			provider.downloadFile(MetaData.fileName, tempFile);
			MetaData serverMetaData = MetaData.getMetaData(tempFile);
			
			//Check all server files
			logger.info("Comparing server files..");
			Iterator<String> iterator = serverMetaData.getFilenameIterator();
			while(iterator.hasNext()) {
				String s = iterator.next();
				long u = serverMetaData.getUpdateCount(s), localu = JStickies.METADATA.getUpdateCount(s);
				logger.info("Comparing file : {}; Server Update Count : {}; Local Update Count : {}", s, u, localu);
				if(localu == -1) {//If does not exist on desktop, then download
					if(u != 0) {
						download(s);
						JStickies.METADATA.addNote(s, u);
					}
				}
				else if(u > localu) { //Server contains latest copy
					download(s);
					JStickies.METADATA.addNote(s, u);
				}
				else if(u < localu) { //Desktop contains latest copy
					upload(new File(JStickies.NOTE_FOLDER + "/" + s), true);
					serverMetaData.addNote(s, localu);
				}
				logger.info("File {} is synchronized!", s);
			}
			
			//Check the local files
			logger.info("Comparing local files..");
			iterator = JStickies.METADATA.getFilenameIterator();
			while(iterator.hasNext()) {
				String s = iterator.next();
				if(serverMetaData.getUpdateCount(s) == -1) { //The file is only on the desktop
					logger.info("File {} was newly created. Uploading to server..", s);
					upload(new File(JStickies.NOTE_FOLDER + "/" + s), false);
					serverMetaData.addNote(s, JStickies.METADATA.getUpdateCount(s));
				}
			}
			
			//Save the metadata and upload the server metadata
			JStickies.METADATA.saveMetaData();
			serverMetaData.saveMetaData();
			provider.uploadFile(tempFile, MetaData.fileName, true);
		}
		logger.info("Synchronization complete!");
	}
	
	/*
	 * Downloads a file from the server to disk. 
	 */
	private void download(String fileName) {
		logger.info("Downloading file {} from server", fileName);
		File diskFile = new File(JStickies.NOTE_FOLDER + "/" + fileName);
		provider.downloadFile(fileName, diskFile);
		
		//If a new copy of the file was downloaded, then delete it and initialize a new copy
		Iterator<Note> notes = Note.notes.iterator();
		while(notes.hasNext()) {
			Note n = notes.next();
			if(n.noteData.fileName == fileName) {
				n.dispose();
				Note.notes.remove(n);
			}
		}
		
		new Note(NoteData.getNoteData(diskFile)).setVisible(true);
	}
	
	/*
	 * Uploads a file from the disk to the server.
	 */
	private void upload(File diskFile, boolean overwrite) {
		logger.info("Uploading file {} to server", diskFile.getName());
		provider.uploadFile(diskFile, diskFile.getName(), overwrite);
	}
	
	@Override
	public boolean accept(File arg0, String arg1) {
		if(arg1.endsWith(".note"))
			return true;
		return false;
	}

}
