/*
 * This class represents a context menu that provides the following options : 
 * 	- Cut
 *	- Copy
 * 	- Paste
 * 	- Select all
 * 	- Color
 * 
 * Each Note that is created is associated with a ContextMenu. The options Cut, Copy, Paste, Select all is applicable to the JTextArea of the 
 * Note, whereas the option Color is applicable to the entire Note.
 */

package com.jstickies.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class ContextMenu extends JPopupMenu implements ActionListener, PopupMenuListener {
	
	private static final long serialVersionUID = 1L;
	private JMenu colorMenu;
	private JMenuItem colorMenuItems[], copyMenuItem, cutMenuItem, pasteMenuItem, selectMenuItem;
	private Note note;
	
	/*
	 * Creates a ContextMenu for the Note object n. 
	 */
	ContextMenu(Note n) {
		note = n;
		
		colorMenu = new JMenu("Color");
		
		//Create menu items for each supported color
		colorMenuItems = new JMenuItem[5];
		for(int i=0; i<5; i++) {
			colorMenuItems[i] = new JMenuItem(Note.colorNames[i]);
			colorMenuItems[i].setIcon(Note.colorIcons[i]);
			colorMenuItems[i].addActionListener(this);
			
			//Make current note color as default icon for the menu
			if(Note.colors[i].equals(note.noteData.noteColor))
				colorMenu.setIcon(Note.colorIcons[i]);
			
			colorMenu.add(colorMenuItems[i]);
		}
		
		//Cut, Copy, Paste
		copyMenuItem = new JMenuItem("Copy");
		copyMenuItem.setEnabled(false);
		copyMenuItem.addActionListener(this);
		
		cutMenuItem = new JMenuItem("Cut");
		cutMenuItem.setEnabled(false);
		cutMenuItem.addActionListener(this);
		
		pasteMenuItem = new JMenuItem("Paste");
		pasteMenuItem.setEnabled(false);
		pasteMenuItem.addActionListener(this);
		
		//Select All
		selectMenuItem = new JMenuItem("Select All");
		selectMenuItem.addActionListener(this);
		
		add(cutMenuItem);
		add(copyMenuItem);
		add(pasteMenuItem);
		addSeparator();
		add(selectMenuItem);
		addSeparator();
		add(colorMenu);
		
		addPopupMenuListener(this);
	}
	
	/*
	 * Implements ActionListener#actionPerformed(ActionEvent). Checks which item triggered the ActionEvent and generates the appropriate action.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem) e.getSource();
		
		if(source.equals(selectMenuItem))
			note.noteTextArea.selectAll();
		else if(source.equals(cutMenuItem))
			note.noteTextArea.cut();
		else if(source.equals(copyMenuItem))
			note.noteTextArea.copy();
		else if(source.equals(pasteMenuItem))
			note.noteTextArea.paste();
		else {
			for(int i=0; i<colorMenuItems.length; i++)
				if(source.equals(colorMenuItems[i])) {
					note.setNoteColor(Note.colors[i]);
					colorMenu.setIcon(Note.colorIcons[i]);
				}
		}
	}

	@Override
	public void popupMenuCanceled(PopupMenuEvent e) {}

	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
	
	/*
	 * Implements PopupMenuListener#popupMenuWillBecomeVisible(PopupMenuEvent). This method is called every time the context menu is displayed.
	 * The enabling and disabling of cut, copy, paste is implemented in this method.
	 */
	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		cutMenuItem.setEnabled(false);
		copyMenuItem.setEnabled(false);
		pasteMenuItem.setEnabled(false);
		
		//Enable paste only if there is a string to be pasted in the system clipboard
		if(Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(DataFlavor.stringFlavor))
			pasteMenuItem.setEnabled(true);
		
		//Enable cut, copy only if text has been selected
		if(note.noteTextArea.getSelectedText() != null) {
			cutMenuItem.setEnabled(true);
			copyMenuItem.setEnabled(true);
		}
	}

}
