/*
 * This class represents a GUI that enables the manipulation of the synchronization settings. It enables the user set up a synchronization 
 * provider, change the synchronization interval, unlink the synchronization provider, etc.
 */

package com.jstickies.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jstickies.JStickies;
import com.jstickies.data.sync.SyncSettings;
import com.jstickies.data.sync.provider.Provider;

public class SyncSettingsGUI extends JFrame implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(SyncSettingsGUI.class);
	
	private JButton providerButtons[];
	private Container contentPane;
	private Provider[] providers;
	private List<Image> imageIcons;
	private ImageIcon syncSettingsIcon;
	private JTextField intervalField;
	
	/*
	 * Creates a GUI displaying the SyncSettings if it was previously configured, or displays the various synchronization providers to select 
	 * from.
	 */
	public SyncSettingsGUI() {
		new SwingWorker<Boolean, Object>() {

			@Override
			protected Boolean doInBackground() throws Exception {
				//Get all the ImageIcons for the JFrame. Done in the SwingWorker thread, to prevent any problems while loading the image
				logger.debug("Loading Image Icons");
				imageIcons = new ArrayList<Image>();
				imageIcons.add(JStickies.loadImage("media/stickies-16x16.png").getImage());
				imageIcons.add(JStickies.loadImage("media/stickies-24x24.png").getImage());
				imageIcons.add(JStickies.loadImage("media/stickies-32x32.png").getImage());
				imageIcons.add(JStickies.loadImage("media/stickies-48x48.png").getImage());
				imageIcons.add(JStickies.loadImage("media/stickies-128x128.png").getImage());
				
				syncSettingsIcon = JStickies.loadImage("media/synchronize_32.png");
				
				//If synchronization settings have been configured, then get it. Else, get all the providers.
				Boolean val = SyncSettings.exists();
				if(val) {
					providers = new Provider[1];
					providers[0] = JStickies.SYNC_SETTINGS.getProvider().newInstance();
				}
				else {
					Class<Provider>[] providerClass = Provider.getAvailableProviders();
					providers = new Provider[providerClass.length];
					for(int i=0; i<providerClass.length; i++)
						providers[i] = providerClass[i].newInstance();
				}
				
				return val;
			}
			
			@Override
			protected void done() {
				try {
					if(get())
						initSettingsGUI();
					else
						initGUI();
				} 
				catch (InterruptedException e) {
					logger.error("SwingWorker Thread(SyncSettingsGUI) was interrupted : {}", e.getMessage());
				} 
				catch (ExecutionException e) {
					logger.error("Error in SwingWorker Thread(SyncSettingsGUI) :  {}", e.getMessage());
				}
			}
			
		}.execute();
	}
	
	/*
	 * Creates a GUI displaying all the synchronization providers to select from. Selecting any of the providers, starts an authorization 
	 * process with the respective provider. 
	 */
	protected void initGUI() {
		basicGUI();
		logger.debug("Loading Provider Selection GUI");
		
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		
		JTextArea message = new JTextArea("JStickies supports the synchronization of your sticky notes across multiple computers by storing " +
				"your notes in the cloud.\n\nTo link your account with JStickies, select your cloud storage provider below : ");
		message.setLineWrap(true);
		message.setWrapStyleWord(true);
		message.setEditable(false);
		message.setFocusable(false);
		message.setBorder(new EmptyBorder(0,0,10,0));
		message.setBackground(new Color(0,0,0,1));
		message.setAlignmentX(LEFT_ALIGNMENT);
		message.setMaximumSize(new Dimension(250, 110));
		contentPane.add(message);
		
		providerButtons = new JButton[providers.length];
		for(int i=0; i<providers.length; i++) {
				providerButtons[i] = new JButton(providers[i].getDisplayName());
				providerButtons[i].setIcon(providers[i].getIcon());
				providerButtons[i].addActionListener(this);
				contentPane.add(providerButtons[i++]);
		}
		
		setVisible(true);
	}
	
	/*
	 * Creates a GUI displaying the current synchronization settings. 
	 */
	protected void initSettingsGUI() {
		basicGUI();
		logger.debug("Loading Settings GUI");
		
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		
		JLabel providerName = new JLabel(providers[0].getDisplayName());
		providerName.setIcon(providers[0].getIcon());
		providerName.setFocusable(false);
		providerName.setFont(providerName.getFont().deriveFont(20f));
		contentPane.add(providerName);
		
		contentPane.add(Box.createRigidArea(new Dimension(1,10)));
		
		final JLabel userNameLabel = new JLabel("Account : ");
		userNameLabel.setFocusable(false);
		contentPane.add(userNameLabel);
		
		contentPane.add(Box.createRigidArea(new Dimension(1,10)));
		
		Box intervalBox = Box.createHorizontalBox();
		intervalBox.setAlignmentX(LEFT_ALIGNMENT);
		intervalBox.add(new JLabel("Synchronization Interval : "));
		intervalBox.add(Box.createRigidArea(new Dimension(10,1)));
		intervalField = new JTextField(JStickies.SYNC_SETTINGS.getInterval() + "");
		intervalField.setMaximumSize(new Dimension(40,25));
		intervalBox.add(intervalField);
		intervalBox.add(Box.createRigidArea(new Dimension(5,1)));
		intervalBox.add(new JLabel("minute(s)"));
		contentPane.add(intervalBox);
		
		contentPane.add(Box.createRigidArea(new Dimension(1,80)));
		
		Box buttonBox = Box.createHorizontalBox();
		buttonBox.setAlignmentX(LEFT_ALIGNMENT);
		buttonBox.add(Box.createRigidArea(new Dimension(50,1)));
		JButton unlinkButton = new JButton("Unlink Account");
		unlinkButton.addActionListener(this);
		buttonBox.add(unlinkButton);
		buttonBox.add(Box.createRigidArea(new Dimension(10,1)));
		JButton saveButton = new JButton("Save Settings");
		saveButton.addActionListener(this);
		buttonBox.add(saveButton);
		contentPane.add(buttonBox);
		
		final JFrame parent = this;
		//Network operation! 
		new SwingWorker<String, Object>() {
			@Override
			protected String doInBackground() throws Exception {
				logger.debug("Retrieving username in new Thread");
				return providers[0].getUsername();
			}
			
			protected void done() {
				try {
					String uname = get();
					userNameLabel.setText(userNameLabel.getText() + uname);
					if(uname == null)
						JOptionPane.showMessageDialog(parent, "Error in connecting to " + providers[0].getDisplayName() + " servers.", "Connection Error", JOptionPane.ERROR_MESSAGE);
				} 
				catch (InterruptedException e) {
					logger.error("SwingWorker Thread(SyncSettingsGUI) was interrupted while fetching the username : " + e.getMessage());
				} 
				catch (ExecutionException e) {
					logger.error("Error in retrieving username from server : {}", e.getMessage());
					JOptionPane.showMessageDialog(parent, "Error in connecting to " + providers[0].getDisplayName() + " servers.", "Connection Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			
		}.execute();
		
		setVisible(true);
	}
	
	/*
	 * Sets up the basic GUI like window size, layout, etc.
	 */
	protected void basicGUI() {
		setSize(300,300);
		setLayout(new BorderLayout(0,7));
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setResizable(false);
		
		setTitle("JStickies Sync Settings");
		setIconImages(imageIcons);
		
		JLabel syncSettings = new JLabel("Sync Settings");
		syncSettings.setFont(syncSettings.getFont().deriveFont(24f));
		syncSettings.setIcon(syncSettingsIcon);
		syncSettings.setIconTextGap(10);
		syncSettings.setFocusable(false);
		getContentPane().add(syncSettings, BorderLayout.NORTH);
		
		contentPane = new Container();
		getContentPane().add(contentPane, BorderLayout.CENTER);
		
		JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
		sep.setMaximumSize(new Dimension(sep.getMaximumSize().width, 10));
		contentPane.add(sep);
		
		getRootPane().setBorder(Note.emptyBorder);
	}

	/*
	 * Implements ActionListener#actionPerformed(ActionEvent). 
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		JButton source = (JButton) arg0.getSource();
		
		if(SyncSettings.exists()) {
			if(source.getText() == "Save Settings") {
				final int interval = Integer.parseInt(intervalField.getText());
				if(interval != 0)
					if(interval != JStickies.SYNC_SETTINGS.getInterval()) {
						new SwingWorker<Object, Object>() {
							@Override
							protected Object doInBackground() throws Exception {
								logger.info("Restarting synchronization thread");
								JStickies.stopSynchronization();
								JStickies.SYNC_SETTINGS.setInterval(interval);
								JStickies.SYNC_SETTINGS.saveSettings();
								JStickies.startSynchronization();
								return null;
							}
						}.execute();
						dispose();
					}
				else
					JOptionPane.showMessageDialog(this, "Synchronization interval must be greater than 0", "Incorrect Synchronization Interval", JOptionPane.WARNING_MESSAGE);
			}
			else {
				new SwingWorker<Object, Object>() {
					@Override
					protected Object doInBackground() throws Exception {
						logger.info("Unauthorizing user");
						JStickies.stopSynchronization();
						providers[0].unAuthorize();
						JStickies.SYNC_SETTINGS.deleteSettings(); //Delete the settings file
						JStickies.SYNC_SETTINGS = null; //Remove references to SyncSettings
						return null;
					}
				}.execute();
				dispose();
			}
		}
		else
			for(int i=0; i<providerButtons.length; i++)
				if(providerButtons[i].equals(source)) {
					//Authorization session in a new thread, so as to not cause problems in the EDT
					final int index = i;
					new SwingWorker<Object, Object>() {
						@Override
						protected Object doInBackground() throws Exception {
							logger.info("Authorizig user");
							providers[index].authorize();
							return null;
						}
						
						protected void done() {
							logger.info("Starting Synchronizer Thread");
							if(SyncSettings.exists())
								JStickies.startSynchronization();
							else
								logger.error("Couldn't start Synchronizer : No SyncSettings file");
						}
						
					}.execute();
					
					dispose();
					break;
				}
	}
}
