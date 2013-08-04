/*****************************************************************************
** ANGRYBIRDS AI AGENT FRAMEWORK
** Copyright (c) 2013,XiaoYu (Gary) Ge, Stephen Gould,Jochen Renz
**  Sahan Abeyasinghe, Jim Keys, Kar-Wai Lim, Zain Mubashir,  Andrew Wang, Peng Zhang
** All rights reserved.
**This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
**To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
*or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
*****************************************************************************/

package ab.vision;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.awt.Polygon;

import javax.imageio.ImageIO;

import Jama.Matrix;
import ab.demo.other.ActionRobot;
import ab.server.Proxy;
import ab.server.proxy.message.ProxyScreenshotMessage;
import ab.utils.ShowDebuggingImage;
import ab.vision.MeanShiftFilter;

/* TestVision ------------------------------------------------------------- */

public class TestVisionColor implements Runnable {

	static public Proxy getGameConnection(int port) {
		Proxy proxy = null;
		try {
			proxy = new Proxy(port) {
				@Override
				public void onOpen() {
					System.out.println("...connected to game proxy");
				}

				@Override
				public void onClose() {
					System.out.println("...disconnected from game proxy");
				}
			};
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		proxy.start();
		System.out.println("Waiting for proxy to connect...");
		proxy.waitForClients(1);

		return proxy;
	}

	static public int[][] computeMetaInformation(BufferedImage screenshot) {
		// image size
		final int nHeight = screenshot.getHeight();
		final int nWidth = screenshot.getWidth();

		// meta debugging information
		int[][] meta = new int[nHeight][nWidth];
		for (int y = 0; y < nHeight; y++) {
			for (int x = 0; x < nWidth; x++) {
				final int colour = screenshot.getRGB(x, y);
				meta[y][x] = ((colour & 0x00e00000) >> 15)
						| ((colour & 0x0000e000) >> 10)
						| ((colour & 0x000000e0) >> 5);
			}
		}

		return meta;
	}

	static public BufferedImage analyseScreenShot(BufferedImage screenshot) {


		// get game state
		GameStateExtractor game = new GameStateExtractor();
		GameStateExtractor.GameState state = game.getGameState(screenshot);
	//	System.out.println(state.toString());

		if (state != GameStateExtractor.GameState.PLAYING) {
			System.out.println("End game score : " + game.getScoreEndGame(screenshot));
			//screenshot = VisionUtils.convert2grey(screenshot);
			return screenshot;
		}

		System.out.println("In game score : " + game.getScoreInGame(screenshot));
		// process image
		/**
		 * 图像处理主要在这边完成
		 */
		VisionColor visioncolor = new VisionColor(screenshot);
		BufferedImage img= new BufferedImage(screenshot.getWidth(),screenshot.getHeight(),BufferedImage.TYPE_INT_RGB);
		MeanShiftFilter mean = new MeanShiftFilter(2,60);
		System.out.println("MeanShift");
		img = mean.filter(screenshot,img);
		VisionColor visioncolormean = new VisionColor(img);
		return img;
	}

	static public void main(String[] args) {

		ShowDebuggingImage frame = null;
		BufferedImage screenshot = null;

		// check command line arguments
		if (args.length > 1) {
			System.err.println("  USAGE: java TestVisionColor [(<directory> | <image>)]");
			System.exit(1);
		}

		// connect to game proxy if no arguments given
		if (args.length == 0) {

			Proxy game = getGameConnection(9000);

			while (true) {
				// capture an image
				byte[] imageBytes = game.send(new ProxyScreenshotMessage());
				try {
					screenshot = ImageIO.read(new ByteArrayInputStream(
							imageBytes));
				} catch (IOException e) {
					e.printStackTrace();
				}

				// analyse and show image
				int[][] meta = computeMetaInformation(screenshot);
				screenshot = analyseScreenShot(screenshot);
				if (frame == null) {
					frame = new ShowDebuggingImage("TestVisionColor", screenshot,
							meta);
				} else {
					frame.refresh(screenshot, meta);
				}

				// sleep for 100ms
				try {
					Thread.sleep(25);
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		}

		// get list of images to process
		File[] images = null;

		// check if argument is a directory or an image
		if ((new File(args[0])).isDirectory()) {
			images = new File(args[0]).listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File directory, String fileName) {
					return fileName.endsWith(".png");
				}
			});
		} else {
			images = new File[1];
			images[0] = new File(args[0]);
		}

		// iterate through the images
		Arrays.sort(images);
		for (File filename : images) {
			if (filename.isDirectory()) {
				continue;
			}

			// load the screenshot
			try {
				screenshot = ImageIO.read(filename);
			} catch (IOException e) {
				System.err.println("ERROR: could not load image " + filename);
				System.exit(1);
			}

			// analyse and show image
			int[][] meta = computeMetaInformation(screenshot);
			screenshot = analyseScreenShot(screenshot);
			if (frame == null) {
				frame = new ShowDebuggingImage("TestVisionColor", screenshot, meta);
			} else {
				frame.refresh(screenshot, meta);
			}
			frame.waitForKeyPress();
		}

		frame.close();
	}

	@Override
	public void run() {
		ShowDebuggingImage frame = null,framecolor = null;
		BufferedImage screenshot = null,screenshotcolor = null;
		while (true) {
			// capture an image
		    screenshot = ActionRobot.doScreenShot();
		    screenshotcolor = ActionRobot.doScreenShot();
			// analyse and show image
			int[][] meta = computeMetaInformation(screenshot);
			screenshot = analyseScreenShot(screenshot);
			if (frame == null) {

				frame = new ShowDebuggingImage("TestVisionColor", screenshot,
				meta);
				
			} else {
				frame.refresh(screenshot, meta);
				
			};
			
			if (framecolor == null)
			{
				framecolor = new ShowDebuggingImage("TestVisionColor", screenshotcolor,
						meta);
			}
			else
			{
				framecolor.refresh(screenshotcolor, meta);
			};

			// sleep for 100ms
			try {
				Thread.sleep(25);
			} catch (InterruptedException e) {
				// do nothing
			}
		}
	}
	
}
