/*
 * This class is responsible for automatically saving the notes. It saves the notes based on 2 approaches :
 * 	- Every few seconds
 * 	- Every time a note goes out of focus
 * 
 * This class extends TimerTask and is invoked by a Timer every few seconds. The interval at which AutoSaver must be invoked is configured in 
 * JStickies, which is also responsible for scheduling the Timer.
 */

package com.jstickies.data;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.TimerTask;

import javax.swing.JTextArea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jstickies.gui.Note;

public class AutoSaver extends TimerTask implements FocusListener {

	private final Logger logger = LoggerFactory.getLogger(AutoSaver.class);
	
	/*
	 * Saves all the notes to disk whenever invoked by the Timer.
	 */
	@Override
	public void run() {
		logger.info("Saving all notes to disk");
		Note.saveAll();
	}
 
	/*
	 * Saves the note which goes out of focus. 
	 * NOTE: All the notes must add an AutoSaver object as its focus listener.
	 */
	@Override
	public void focusLost(FocusEvent arg0) {
		Note n = (Note)((JTextArea)arg0.getSource()).getRootPane().getParent();
		logger.info("Saving Note ({}) to disk", n.noteData.noteName);
		n.saveData();
	}
	
	/*
	 * Not required!
	 */
	@Override
	public void focusGained(FocusEvent arg0) {}
	
}
