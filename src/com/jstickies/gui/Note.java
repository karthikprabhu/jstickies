/*
 * This class represents a sticky note. It creates the entire sticky note GUI including the title area and and also the text area for the note. 
 */

package com.jstickies.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jstickies.JStickies;
import com.jstickies.data.NoteData;
import componentadapter.ComponentResizer;


public class Note extends JDialog implements Serializable {

	private static final long serialVersionUID = 4536222406431913314L;
	private static final Logger logger = LoggerFactory.getLogger(Note.class);
	
	public static EmptyBorder emptyBorder;
	public static Dimension minimumSize, defaultSize;
	public static Color[] colors = new Color[5];
	public static ColorIcon[] colorIcons = new ColorIcon[5];
	public static String[] colorNames = new String[5];
	public static ArrayList<Note> notes; //Used to keep track of all created notes.
	
	/*
	 * Static objects common to all Note objects
	 */
	static {
		emptyBorder = new EmptyBorder(5,10,5,10);
		
		minimumSize = new Dimension(50,50); 
		defaultSize = new Dimension(250,300);
		
		colors[0] = new Color(254,240,112);
		colors[1] = new Color(255,195,93);
		colors[2] = new Color(255,187,146);
		colors[3] = new Color(234,187,247);
		colors[4] = new Color(160,241,199);
		
		for(int i=0; i<5; i++)
			colorIcons[i] = new ColorIcon(colors[i]);
		
		colorNames[0] = "Yellow";
		colorNames[1] = "Orange";
		colorNames[2] = "Red";
		colorNames[3] = "Purple";
		colorNames[4] = "Green";
		
		notes = new ArrayList<Note>();
	}
	
	public NoteData noteData;
	JTextArea noteTextArea;
	TitleBar titleBar;
	Container contentPane, centerPane;
	JScrollPane pane;
	EmptyBorder frameBorder;
	boolean changed;
	
	/*
	 * Creates a new Note with the title set to noteName
	 */
	public Note(String noteName) {
		this(new NoteData(noteName, colors[0], "", null, defaultSize));
		changed = true;
	}
	
	/*
	 * Creates a new Note from a NoteData object. 
	 */
	public Note(NoteData data) {
		notes.add(this); //Add this note to the set of all notes
		
		noteData = data;
		contentPane = getContentPane();
		logger.debug("Creating Note({})", noteData.noteName);
		
		setSize(noteData.noteSize);
		setLayout(new BorderLayout());
		setUndecorated(true);
		if(noteData.noteLocation == null) {
			setLocationRelativeTo(null);
			noteData.noteLocation = getLocation(); //To avoid null pointer exception in isNoteChanged
		}
		else
			setLocation(noteData.noteLocation);
		
		titleBar = new TitleBar(noteData.noteName, this);
		contentPane.add(titleBar, BorderLayout.NORTH);
		
		centerPane = new Container();
		centerPane.setLayout(new BorderLayout());
		contentPane.add(centerPane, BorderLayout.CENTER);
		
		noteTextArea = new JTextArea(noteData.noteText);
		noteTextArea.setBorder(emptyBorder);
		noteTextArea.setLineWrap(true);
		noteTextArea.setWrapStyleWord(true);
		noteTextArea.setFont(new Font(noteTextArea.getFont().getName(), Font.PLAIN, 14));
		noteTextArea.addFocusListener(JStickies.AUTO_SAVER);
		centerPane.add(noteTextArea, BorderLayout.CENTER);
		
		pane = new JScrollPane(noteTextArea);
		pane.getVerticalScrollBar().setPreferredSize(new Dimension(12,pane.getVerticalScrollBar().getPreferredSize().height));
		pane.setBorder(null);
		centerPane.add(pane,BorderLayout.CENTER);
		
		setNoteColor(noteData.noteColor);
		
		frameBorder = new EmptyBorder(10,10,10,10);
		rootPane.setBorder(frameBorder);
		
		ComponentResizer resizer = new ComponentResizer();
		resizer.setMinimumSize(minimumSize);
		resizer.setDragInsets(new Insets(10,10,10,10));
		resizer.registerComponent(this);
		
		ContextMenu contextMenu = new ContextMenu(this);
		noteTextArea.setComponentPopupMenu(contextMenu);
		
		changed = false; //Variable for detecting changes made during runtime
	}
	
	/*
	 * Sets the color of this Note to color. 
	 */
	public void setNoteColor(Color color) {
		logger.info("Setting {} note color to {}", noteData.noteName, color);
		noteData.noteColor = color;
		noteTextArea.setBackground(color);
		contentPane.setBackground(color);
		rootPane.setBackground(color);
		changed = true;
	}
	
	/*
	 * Checks if this Note has been changed compared to when it was previously saved. 
	 */
	public boolean isNoteChanged() {
		if(changed | !noteData.noteText.equals(noteTextArea.getText()) | !noteData.noteLocation.equals(getLocation()) | !noteData.noteSize.equals(getSize()))
			return true;
		return false;
	}
	
	/*
	 * Saves this Note to disk. Instead of saving the entire Note, it saves only the necessary data i.e the NoteData associated with this 
	 * Note to disk.
	 */
	public void saveData() {
		if(isNoteChanged()) {
			noteData.noteLocation = getLocation();
			noteData.noteSize = getSize();
			noteData.noteText = noteTextArea.getText();
			
			logger.info("Note({}) has changed. Saving to disk", noteData.noteName);
			noteData.saveData();
			changed = false; //File has been updated
		}
		else
			logger.info("No changes to Note({})", noteData.noteName);
	}
	
	/*
	 * Deletes this Note. This method disposes the Note and also deletes any saved data.
	 */
	public void delete() {
		logger.info("Deleting Note({})", noteData.noteName);
		noteData.deleteData();
		noteData = null;
		dispose();
		notes.remove(this);
	}
	
	/*
	 * Loads all the saved notes from disk. 
	 */
	public static void loadSavedNotes() {
		final NoteData[] noteData = NoteData.getSavedData();
		
		logger.debug("Creating Note objects on EDT");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if(noteData != null)
					for(NoteData d : noteData)
						new Note(d).setVisible(true);
			}
			
		});
	}
	
	/*
	 * Saves all the notes to disk.
	 */
	public static void saveAll() {
		logger.debug("Saving all notes to disk");
		Iterator<Note> i = notes.iterator();
		while(i.hasNext()) {
			Note n = i.next();
			n.saveData();
		}
	}

}
