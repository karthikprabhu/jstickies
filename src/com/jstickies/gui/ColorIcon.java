/*
 * Represents a 16x16 mono-colored ImageIcon. Used for showing the color icons in the right click context menu.
 */

package com.jstickies.gui;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

public class ColorIcon extends ImageIcon {
	
	private static final long serialVersionUID = 1L;
	Color color;
	
	/*
	 * Creates a 16x16 ImageIcon with the color set to color. 
	 */
	public ColorIcon(Color color) {
		this.color = color;
		setImage(ColorIcon.createImage(color.getRGB(),16,16));
	}

	/*
	 * Creates an Image of rgb color with the specified width and height.
	 */
	private static Image createImage(int rgb, int width, int height) {
		BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_3BYTE_BGR);
		for(int i=0; i<width; i++)
			for(int j=0; j<height; j++)
				image.setRGB(i, j, rgb);
		return image;
	}
}
