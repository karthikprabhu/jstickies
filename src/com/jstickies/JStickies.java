/*
 * Main class for loading the entire JStickies application. This class has the following responsibilities : 
 * 	- Load the system tray application
 * 	- Create the AutoSaver thread
 * 	- Load any notes that were saved
 * 	- If synchronization has been set up, then create the Synchronizer thread
 *	
 */
package com.jstickies;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Timer;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jstickies.data.AutoSaver;
import com.jstickies.data.sync.MetaData;
import com.jstickies.data.sync.SyncSettings;
import com.jstickies.data.sync.Synchronizer;
import com.jstickies.gui.Note;
import com.jstickies.gui.TrayApplication;

public class JStickies {
	
	public static String DELETE_ICON = "media/delete.png",
			TRAY_ICON = "media/stickies-24x24.png",
			NEW_ICON = "media/new.png",
			EXIT_ICON = "media/exit.png",
			ABOUT_ICON = "media/about.png",
			SYNC_ICON = "media/synchronize.png",
			SETTINGS_ICON = "media/settings.png";
	public static File NOTE_FOLDER = new File(".jstickies"), 
			MEDIA_FOLDER = new File("media");
	
	public static SyncSettings SYNC_SETTINGS = null;
	public static Synchronizer SYNCHRONIZER = null;
	public static AutoSaver AUTO_SAVER = null;
	public static MetaData METADATA = null;
	public static JFrame JFRAME = null;
	
	private long autoSaveInterval = 30000; //set the auto save interval to 30s
	private static final Logger logger = LoggerFactory.getLogger(JStickies.class);
	
	/*
	 * Launches the JStickies application by loading all the necessary classes in the right order. 
	 */
	JStickies() {
		if(!NOTE_FOLDER.exists()) {//Create the folder for loading notes, if it doesn't already exist
			NOTE_FOLDER.mkdir();
			logger.info("Created the JStickies note directory.");
		}

		JFRAME = new JFrame("JStickies");
		JFRAME.setIconImage(loadImage(TRAY_ICON).getImage());
		JFRAME.setUndecorated(true);
		JFRAME.setResizable(false);
		JFRAME.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		/*JFRAME.addWindowListener(new WindowAdapter() {
			public void windowIconified(WindowEvent e) {
				((Frame)e.getWindow()).setExtendedState(e.getOldState());
			}
		});*/
		JFRAME.setExtendedState(JFrame.NORMAL);
		JFRAME.setVisible(true);
		
		logger.info("Launching System Tray application");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new TrayApplication(loadImage(TRAY_ICON).getImage(), "JStickies");
			}
		});
		
		logger.info("Launching AutoSaver thread");
		AUTO_SAVER = new AutoSaver();
		new Timer().scheduleAtFixedRate(AUTO_SAVER, autoSaveInterval, autoSaveInterval); //Schedule the AutoSaver every autoSaveInterval milliseconds
		
		logger.info("Loading saved notes");
		Note.loadSavedNotes();
		
		if(SyncSettings.exists()) { 
			logger.info("Loading Synchronizer thread");
			startSynchronization();
		}
	}
	
	/*
	 * Main method for starting up the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getInstalledLookAndFeels()[1].getClassName()); //Set the LookAndFeel to Nimbus
		} 
		catch(ClassNotFoundException e) {
			logger.warn("Unable to set LookAndFeel to Nimbus : {}", e.getMessage());
		} 
		catch(InstantiationException e) {
			logger.warn("Unable to set LookAndFeel to Nimbus : {}", e.getMessage());
		} 
		catch(IllegalAccessException e) {
			logger.warn("Unable to set LookAndFeel to Nimbus : {}", e.getMessage());
		} 
		catch(UnsupportedLookAndFeelException e) {
			logger.warn("Unable to set LookAndFeel to Nimbus : {}", e.getMessage());
		}
		new JStickies();
	}
	
	/*
	 * Static method for loading image resources as an ImageIcon. All classes must use this method to load any images.
	 */
	public static ImageIcon loadImage(String location) {
		return new ImageIcon(JStickies.class.getResource(location));
	}	
	
	/*
	 * Loads a serialized object from file. This method is used by all classes for de-serializing any object from file. 
	 */
	public static Object loadFile(File file) {
		Object obj = null;
		ObjectInputStream ois = null;
		
		try {
			FileInputStream fis = new FileInputStream(file);
			ois = new ObjectInputStream(fis);
			
			obj = ois.readObject();
		} 
		catch(FileNotFoundException e) {
			logger.error("Error loading file {} : {}", file.getName(), e.getMessage());
		} 
		catch(ClassNotFoundException e) {
			logger.error("Error loading file {} : {}", file.getName(), e.getMessage());
		} 
		catch(IOException e) {
			logger.error("Error loading file {} : {}", file.getName(), e.getMessage());
		}
		finally {
			if(ois != null)
				try {
					ois.close();
				}
				catch(IOException e) {
					logger.warn("Unable to close ObjectInputStream while reading file {} : {}", file.getName(), e.getMessage());
				}
		}
		
		return obj;
	}
	
	/*
	 * Serializes an object to file. It is used by all classes for serializing an object to file. 
	 */
	public static void saveFile(File file, Object obj) {
		ObjectOutputStream oos = null;
		
		try {
			FileOutputStream fos = new FileOutputStream(file, false);
			oos = new ObjectOutputStream(fos);
			
			oos.writeObject(obj);
		}
		catch(FileNotFoundException e) {
			logger.error("Error while saving object to file {} : {}", file.getName(), e.getMessage());
		}
		catch(IOException e) {
			logger.error("Error while saving object to file {} : {}", file.getName(), e.getMessage());
		}
		finally {
			if(oos != null)
				try {
					oos.close();
				} catch (IOException e) {
					logger.warn("Unable to close ObjectInputStream while reading file {} : {}", file.getName(), e.getMessage());
				}
		}
	}
	
	/*
	 * Starts the Synchronizer
	 */
	public static void startSynchronization() {
		SYNC_SETTINGS = (SYNC_SETTINGS == null)? SyncSettings.getSettings() : SYNC_SETTINGS;
		SYNCHRONIZER = new Synchronizer();
	}
	
	/*
	 * Stops the Synchronizer
	 */
	public static void stopSynchronization() {
		logger.info("Stopping Synchronizer Thread");
		SYNCHRONIZER.t.interrupt();
		SYNCHRONIZER = null;
	}
}