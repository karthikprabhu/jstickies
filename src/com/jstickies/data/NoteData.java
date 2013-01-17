/*
 * This class represents the data contained in a Note. Each Note object is associated with a NoteData object. NoteData contains the following 
 * information about a Note :
 * 	- Name (Title of the note)
 * 	- Color
 *	- Text (Text entered by the user)
 *	- Location (x,y coordinates on the screen)
 *	- Size (Dimensions of the note)
 *	- File Name
 * 
 * A Note is saved to file with only the above information and can also be retrieved using the same.
 */

package com.jstickies.data;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jstickies.JStickies;

public class NoteData implements Serializable {
	
	private static final long serialVersionUID = 1L;
	static File NOTE_FOLDER = JStickies.NOTE_FOLDER;
	
	public String noteName, noteText;
	public Color noteColor;
	public Point noteLocation;
	public Dimension noteSize;
	public String fileName;
	
	private static final Logger logger = LoggerFactory.getLogger(NoteData.class); 
	
	/*
	 * Creates a NoteData object.
	 */
	public NoteData(String name, Color color, String text, Point location, Dimension size) {
		noteName = name;
		noteColor = color;
		noteText = text;
		noteLocation = location;
		noteSize = size;
		fileName = getRandomName();
	}
	
	/*
	 * Saves the NoteData to file. The file name is randomly generated during object creation.
	 */
	public void saveData() {
		logger.info("Saving note {} to file {}", noteName, fileName);
		File saveLocation = new File(NOTE_FOLDER + "/" + fileName);
		JStickies.saveFile(saveLocation, this);
		
		if(JStickies.METADATA != null)
			JStickies.METADATA.updateNote(fileName);
	}
	
	/*
	 * Returns a NoteData array of all the stored notes.
	 */
	public static NoteData[] getSavedData() {
		logger.info("Loading saved notes from disk");
		File[] dataFiles = NOTE_FOLDER.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if(name.endsWith(".note"))
					return true;
				return false;
			}
		});
		logger.info("{} saved notes found", dataFiles.length);
		NoteData[] noteData = (dataFiles.length > 0)? new NoteData[dataFiles.length] : null;
		
		for(int i=0; i<dataFiles.length; i++)
			noteData[i] = NoteData.getNoteData(dataFiles[i]);
		
		return noteData;
	}
	
	/*
	 * Gets a NoteData object from file. Used for de-serializing.
	 */
	public static NoteData getNoteData(File f) {
		logger.info("Loading NoteData from file : {}", f.getName());
		return (NoteData) JStickies.loadFile(f);
	}
	
	/*
	 * Deletes the stored NoteData file.
	 */
	public void deleteData() {
		logger.info("Deleting NoteData file : {}", fileName);
		new File(NOTE_FOLDER + "/" + fileName).delete();
		
		if(JStickies.METADATA != null)
			JStickies.METADATA.deleteNote(fileName);
	}
	
	/*
	 * Generates a unique random name to store the NoteData. A .note extension is used.
	 */
	private static String getRandomName() {
		String text;
		
		do
			text = new Random().nextInt() + ".note";
		while(new File(NOTE_FOLDER + "/" + text).exists()); //If such file exists, generate random name again
		
		return text; 
	}
}
