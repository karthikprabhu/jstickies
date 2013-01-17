/*
 * This class creates a system tray icon for managing the application. The system tray icon is the main UI for managing JStickies.
 * 
 * The following options are available in the tray icon : 
 * 	- New Note
 * 	- Show/Hide Notes
 * 	- Sync Now
 * 	- Sync Settings
 * 	- About
 * 	- Exit
 * 
 * TODO Popupmenu does not disappear when clicked outside
 */ 

package com.jstickies.gui;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jstickies.JStickies;
import com.jstickies.data.sync.SyncSettings;

public class TrayApplication implements ActionListener, PopupMenuListener {

	private JPopupMenu popupMenu;
	private JMenuItem menuItems[];
	private SystemTray systemTray;
	private TrayIcon trayIcon;
	private boolean visible;
	private static String[] menuNames = { "New Note", "Exit", "About" , "Hide Notes", "Show Notes", "Sync Now", "Sync Settings"};
	private static final Logger logger = LoggerFactory.getLogger(TrayApplication.class);
	
	/*
	 * Creates a new system tray icon with the specified image and tooltip. Since this is the main UI for managing JStickies, the application 
	 * exits if system tray is not supported by the system or there is an error while adding the tray icon to the system tray.
	 */
	public TrayApplication(Image image, String tooltip) {
		
		//Exit, if system tray is not supported by the system
		if(!SystemTray.isSupported()) {
			logger.error("System tray not supported in the system. Exiting!");
			JOptionPane.showMessageDialog(null, "This application requires system tray application to work", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		
		initPopupMenu(); //Initialize the popupmenu
		popupMenu.addPopupMenuListener(this);
		
		trayIcon = new TrayIcon(image,tooltip,null);
		trayIcon.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if(!visible) {
					popupMenu.setLocation(e.getX(), e.getY()-popupMenu.getHeight());
					popupMenu.setInvoker(popupMenu);
					popupMenu.setVisible(true);
					visible = true;
				}
				else {
					popupMenu.setVisible(false);
					visible = false;
				}
			}
		});
		visible = false; //Since popupMenu is not visible yet!
		systemTray = SystemTray.getSystemTray();
		
		try {
			systemTray.add(trayIcon);
		} 
		catch (AWTException e) { 
			logger.error("Error adding TrayIcon to the system tray! Exiting application.");
			System.exit(0);
		}
	}
	
	/*
	 * Initializes the JPopupMenu that will be displayed when clicking on the system tray icon.
	 */
	private void initPopupMenu() {
		popupMenu = new JPopupMenu();
		menuItems = new JMenuItem[6];
		
		menuItems[0] = new JMenuItem(menuNames[0]);
		menuItems[0].setIcon(JStickies.loadImage(JStickies.NEW_ICON));
		menuItems[0].addActionListener(this);
		
		menuItems[1] = new JMenuItem(menuNames[1]);
		menuItems[1].setIcon(JStickies.loadImage(JStickies.EXIT_ICON));
		menuItems[1].addActionListener(this);
		
		menuItems[2] = new JMenuItem(menuNames[2]);
		menuItems[2].setIcon(JStickies.loadImage(JStickies.ABOUT_ICON));
		menuItems[2].addActionListener(this);
		
		menuItems[3] = new JMenuItem(menuNames[3]);
		menuItems[3].addActionListener(this);
		
		menuItems[4] = new JMenuItem(menuNames[5]);
		menuItems[4].setIcon(JStickies.loadImage(JStickies.SYNC_ICON));
		menuItems[4].addActionListener(this);
		menuItems[4].setEnabled(SyncSettings.exists());
		
		menuItems[5] = new JMenuItem(menuNames[6]);
		menuItems[5].setIcon(JStickies.loadImage(JStickies.SETTINGS_ICON));
		menuItems[5].addActionListener(this);
		
		popupMenu.add(menuItems[0]);
		popupMenu.add(menuItems[3]);
		popupMenu.addSeparator();
		popupMenu.add(menuItems[4]);
		popupMenu.add(menuItems[5]);
		popupMenu.addSeparator();
		popupMenu.add(menuItems[2]);
		popupMenu.addSeparator();
		popupMenu.add(menuItems[1]);
		
		//To enable computation of height
		popupMenu.setVisible(true);
		popupMenu.setVisible(false);
	}

	/*
	 * This method performs the necessary action when a particular menu item is clicked.
	 * 
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem) e.getSource();
		
		int index = 100; //To break from the switch case, in case there are no matches
		for(int i=0; i<menuItems.length; i++)
			if(menuItems[i].equals(source))
				index = i;
		
		switch(index) {
			case 0 :
				new Note("Untitled Note").setVisible(true);
				break;
			case 1 :
				//Save all notes before exit.
				Note.saveAll();
				System.exit(0);
				break;
			case 2 :
				new About();
				break;
			case 3 :
				boolean temp = menuItems[3].getText().equals(menuNames[3]);
				displayNotes(!temp);
				menuItems[3].setText(menuNames[(temp)? 4 : 3]);
				break;
			case 4 :
				JStickies.SYNCHRONIZER.syncNow = true;
				JStickies.SYNCHRONIZER.t.interrupt();
				break;
			case 5 :
				new SyncSettingsGUI();
				break;
			default:
				break;
		}
		visible = false; //On clicking any item in the menu, the popup menu disappears.
	}
	
	/*
	 * Displays/Hides all notes depending on the value of b
	 */
	private void displayNotes(boolean b) {
		Iterator<Note> iterator = Note.notes.iterator();
		while(iterator.hasNext())
			iterator.next().setVisible(b);
	}

	@Override
	public void popupMenuCanceled(PopupMenuEvent arg0) {}

	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {}

	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
		menuItems[4].setEnabled(JStickies.SYNCHRONIZER != null);
	}
	
}
