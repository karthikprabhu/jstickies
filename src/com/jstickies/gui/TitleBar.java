/*
 * This class represents the title bar of a Note. Each Note has a TitleBar.
 * 
 * The TitleBar displays the title of a Note and enables to move as well as delete the Note.
 */

package com.jstickies.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jstickies.JStickies;

import componentadapter.*;

public class TitleBar extends Container {
	
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(TitleBar.class);
	private static String message = "Note Name";
	private Note parent;
	private JLabel noteTitle, deleteButton;
	
	/*
	 * Creates a TitleBar for frame with the title set to titleText. 
	 */
	public TitleBar(String titleText, final Note frame) {
		parent = frame;
		setLayout(new BorderLayout());
		
		//Note title
		noteTitle = new JLabel(titleText);
		noteTitle.setToolTipText("Double-click to change title or Click & drag to move");
		noteTitle.setBorder(Note.emptyBorder);
		noteTitle.setOpaque(false);
		noteTitle.setFont(noteTitle.getFont().deriveFont(Font.BOLD + Font.ITALIC).deriveFont(14f));
		noteTitle.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if(e.getClickCount() == 2) {
					String title = JOptionPane.showInputDialog(parent, message, noteTitle.getText());
					if(title != null)
						updateTitle(title);
				}
			}
		});
		add(noteTitle, BorderLayout.CENTER);
		
		//Delete Button
		deleteButton = new JLabel(JStickies.loadImage(JStickies.DELETE_ICON));
		deleteButton.setToolTipText("Delete Note");
		deleteButton.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int userResponse = JOptionPane.showConfirmDialog(parent, "Are you sure you want to delete this note?", "Delete Note", JOptionPane.OK_CANCEL_OPTION);
				if(userResponse == JOptionPane.OK_OPTION) {
					parent.delete();
				}
			}
		});
		add(deleteButton, BorderLayout.EAST);
		
		new ComponentMover(parent, noteTitle);
	}
	
	/*
	 * Updates the title of the note. 
	 */
	protected void updateTitle(String title) {
		logger.info("Changing note title : {}", title);
		parent.noteData.noteName = title;
		noteTitle.setText(title);
		parent.changed = true;
	}

}
