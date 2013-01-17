/*
 * This class creates a GUI for showing some information about JStickies.  
 */

package com.jstickies.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Image;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.jstickies.JStickies;

public class About extends JDialog {

	private static final long serialVersionUID = 1L;
	private ArrayList<Image> imageIcons;
	private ImageIcon displayIcon;
	
	public About() {
		new SwingWorker<Object,Object>() {
			protected Object doInBackground() throws Exception {
				imageIcons = new ArrayList<Image>();
				imageIcons.add(JStickies.loadImage(JStickies.MEDIA_FOLDER + "/" + "stickies-16x16.png").getImage());
				imageIcons.add(JStickies.loadImage(JStickies.MEDIA_FOLDER + "/" + "stickies-24x24.png").getImage());
				imageIcons.add(JStickies.loadImage(JStickies.MEDIA_FOLDER + "/" + "stickies-32x32.png").getImage());
				displayIcon = JStickies.loadImage(JStickies.MEDIA_FOLDER + "/" + "stickies-48x48.png");
				imageIcons.add(displayIcon.getImage());
				imageIcons.add(JStickies.loadImage(JStickies.MEDIA_FOLDER + "/" + "stickies-128x128.png").getImage());
				
				return null;
			}
			
			protected void done() {
				initUI();
			}
			
		}.execute();
	}
	
	protected void initUI() {
		setLayout(new BorderLayout());
		setTitle("About JStickies");
		setResizable(false);
		setIconImages(imageIcons);
		
		JLabel icon = new JLabel(displayIcon);
		icon.setVerticalAlignment(SwingConstants.TOP);
		icon.setAlignmentY(TOP_ALIGNMENT);
		icon.setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 0));
		add(icon, BorderLayout.WEST);
		
		JLabel appDescription = new JLabel("<html>JStickies<br>" +
				"Version : 1.0<br><br>" + 
				"A simple cross-platform Java based <br>" + 
				"sticky notes application with synchronization<br>" + 
				"capabilities. JStickies lets you access your<br>" +
				"notes across multiple computers and operating<br>" +
				"systems.<br><br>" + 
				"\u00A9 Copyright 2013 - Karthik Prabhu<br>" +
				"k.karthik.prabhu@gmail.com</html>");
		appDescription.setAlignmentY(TOP_ALIGNMENT);
		appDescription.setBorder(BorderFactory.createEmptyBorder(15, 10, 20, 10));
		add(appDescription, BorderLayout.CENTER);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 20, 10));
		add(bottomPanel, BorderLayout.SOUTH);
		
		bottomPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
		
		JLabel credits = new JLabel("<html><u>Credits</u></html>");
		credits.setHorizontalAlignment(SwingConstants.CENTER);
		credits.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		bottomPanel.add(credits);
		
		JEditorPane creditText = null;
		creditText = new JEditorPane("text/html", "<html><p style='text-align:center'>" + 
				"<a href='http://tips4java.wordpress.com/'>Rob Camick</a><br>" +
				"<a href='http://www.dlanham.com/'>David Lanham</a><br>" +
				"<a href='http://www.icons-land.com/'>Icons Land</a><br>" + 
				"<a href='http://www.oxygen-icons.org/'>Oxygen Team</a><br>" +
				"<a href='http://www.everaldo.com/'>Everaldo Coelho</a><br>" +
				"<a href='http://www.visualpharm.com/'>Visual Pharm</a><br>" + 
				"Rodolphe" +
				"</p></html>");
		creditText.setEditable(false);
		creditText.setFocusable(false);
		creditText.setDragEnabled(false);
		creditText.setBackground(new Color(0,0,0,0));
		creditText.setAlignmentX(SwingConstants.CENTER);
		creditText.setBorder(null);
		creditText.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
					try {
						Desktop.getDesktop().browse(e.getURL().toURI());
					} 
					catch (IOException e1) {} 
					catch (URISyntaxException e1) {}
			}
		});
		bottomPanel.add(creditText);
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
}
