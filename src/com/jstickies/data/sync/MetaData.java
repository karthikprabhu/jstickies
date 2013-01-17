/*
 * This class represents the metadata associated with every .note file. It is used by the Synchronizer to synchronize files between the server
 * and disk
 */

package com.jstickies.data.sync;

import java.io.File;
import java.io.Serializable;

import com.jstickies.JStickies;
import com.jstickies.data.NoteData;
import java.util.HashMap;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetaData implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private HashMap<String, Long> hashMap;
	private static final Logger logger = LoggerFactory.getLogger(MetaData.class);
	
	public static String fileName = "metadata";
	public static File metaDataFile = new File(JStickies.NOTE_FOLDER + "/" + fileName);
	
	public MetaData() {
		logger.info("Creating new MetaData");
		NoteData[] noteData = NoteData.getSavedData();
		
		hashMap = new HashMap<String, Long>();
		for(NoteData n : noteData)
			hashMap.put(n.fileName, 1L);
		
		saveMetaData();
	}
	
	/*
	 * Saves this MetaData object to disk. 
	 */
	void saveMetaData() {
		logger.info("Saving MetaData to disk");
		JStickies.saveFile(metaDataFile, this);
	}
	
	/*
	 * Gets a MetaData object from file.
	 */
	static MetaData getMetaData(File file) {
		logger.info("Reading MetaData from file : {}", file.getName());
		return (MetaData) JStickies.loadFile(file); 
	}
	
	
	/*
	 * Updates the metadata for the file fileName. Called everytime NoteData is saved. 
	 */
	public void updateNote(String fileName) {
		if(hashMap.containsKey(fileName)) //If there is already an entry for the file, then update it
			addNote(fileName, hashMap.get(fileName).longValue() + 1L);
		else //Else create a new entry
			addNote(fileName, 1L);
	}
	
	/*
	 * Adds the metadata for a new file into the HashMap. Works even if the metadata for the file already exists.
	 */
	public void addNote(String fileName, long update) {
		hashMap.put(fileName, update);
		logger.info("Metadata for file {} updated to {}", fileName, update);
	}
	
	/*
	 * Deletes the metadata for the file fileName. It basically sets the update count for the file fileName to 0.
	 */
	public void deleteNote(String fileName) {
		addNote(fileName, 0L);
	}
	
	/*
	 * Returns the update count of the file.
	 */
	public long getUpdateCount(String s) {
		if(hashMap.containsKey(s))
			return hashMap.get(s);
		return -1;
	}
	
	/*
	 * Returns an Iterator over the filenames. 
	 */
	public Iterator<String> getFilenameIterator() {
		return hashMap.keySet().iterator();
	}
}