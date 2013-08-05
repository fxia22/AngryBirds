/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2013, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys, Kar-Wai Lim, Zain Mubashir, Andrew Wang, Peng Zhang
 ** All rights reserved.
 **This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
 **To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
 *or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 *****************************************************************************/
package ab.demo;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ab.demo.other.ActionRobot;
import ab.demo.other.Env;
import ab.demo.other.Shot;
import ab.demo.util.StateUtil;
import ab.planner.TrajectoryPlanner;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;

public class NaiveAgent implements Runnable {

	private int focus_x;
	private int focus_y;

	private ActionRobot ar;
	public int currentLevel = 1;
	TrajectoryPlanner tp;

	private boolean firstShot;
	private Point prevTarget;

	// a standalone implementation of the Naive Agent
	public NaiveAgent() {
		ar = new ActionRobot();
		tp = new TrajectoryPlanner();
		prevTarget = null;
		firstShot = true;
		// --- go to the Poached Eggs episode level selection page ---
		ActionRobot.GoFromMainMenuToLevelSelection();

	}

	public int getCurrent_level() {
		return currentLevel;
	}

	public void setCurrent_level(int current_level) {
		this.currentLevel = current_level;
	}

	// run the client
	public void run() {
		System.out.println("Load");
		ar.loadLevel(currentLevel);
		while (true) {
			//System.out.println("Solve");
			GameState state = solve();
			//System.out.println("SolveFinish");
			if (state == GameState.WON) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				int score = -2;
				while (score != StateUtil.checkCurrentScore(ar.proxy)) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					score = StateUtil.checkCurrentScore(ar.proxy);
				}
				System.out.println("###### The game score is " + score
						+ "########");
				ar.loadLevel(++currentLevel);
				// make a new trajectory planner whenever a new level is entered
				tp = new TrajectoryPlanner();

				// first shot on this level, try high shot first
				firstShot = true;
			} else if (state == GameState.LOST) {
				System.out.println("restart");
				ar.restartLevel();
			} else if (state == GameState.LEVEL_SELECTION) {
				System.out
						.println("unexpected level selection page, go to the lasts current level : "
								+ currentLevel);
				ar.loadLevel(currentLevel);
			} else if (state == GameState.MAIN_MENU) {
				System.out
						.println("unexpected main menu page, go to the lasts current level : "
								+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				ar.loadLevel(currentLevel);
			} else if (state == GameState.EPISODE_MENU) {
				System.out
						.println("unexpected episode menu page, go to the lasts current level : "
								+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				ar.loadLevel(currentLevel);
			}

		}

	}

	private double distance(Point p1, Point p2) {
		return Math
				.sqrt((double) ((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y)
						* (p1.y - p2.y)));
	}

	public GameState solve()

	{
		System.out.println("solve");
		int tap_time = 0;
		// capture Image
		BufferedImage screenshot = ActionRobot.doScreenShot();

		// process image
		Vision vision = new Vision(screenshot);

		Rectangle sling = vision.findSlingshot();

		while (sling == null && ar.checkState() == GameState.PLAYING) {
			System.out
					.println("no slingshot detected. Please remove pop up or zoom out");
			ar.fullyZoom();
			screenshot = ActionRobot.doScreenShot();
			vision = new Vision(screenshot);
			sling = vision.findSlingshot();
		}
		
		List<Rectangle> red_birds = vision.findRedBirds();
		List<Rectangle> blue_birds = vision.findBlueBirds();
		List<Rectangle> yellow_birds = vision.findYellowBirds();
		List<Rectangle> pigs = vision.findPigs();
		int bird_count = 0;
		bird_count = red_birds.size() + blue_birds.size() + yellow_birds.size();
		
		/*if (sling!=null)
		{
			int colour = screenshot.getRGB(sling.x+sling.width/2, sling.y+sling.height/8);
			int colourblack = ((colour & 0x00e00000) >> 15)
					| ((colour & 0x0000e000) >> 10)
					| ((colour & 0x000000e0) >> 5);
			System.out.println("Color"+colourblack);
		}*/
		/*
		int _bird_on_sling = 0;
		boolean find_bird = false;
		if (!find_bird)
			{
			for (int i = 0;i<red_birds.size();i++) 
			
			if (sling.contains(red_birds.get(i).getCenterX(), red_birds.get(i).getCenterY())) 
				{
				_bird_on_sling = 1;
				find_bird = true;
				break;
				}
			};
		
			if (!find_bird)
			{
			for (int i = 0;i<blue_birds.size();i++) 
			
			if (sling.contains(blue_birds.get(i).getCenterX(), blue_birds.get(i).getCenterY())) 
				{
				_bird_on_sling = 2;
				find_bird = true;
				break;
				}
			};
			
			if (!find_bird)
			{
			for (int i = 0;i<yellow_birds.size();i++) 
			
			if (sling.contains(yellow_birds.get(i).getCenterX(), yellow_birds.get(i).getCenterY())) 
				{
				_bird_on_sling = 3;
				find_bird = true;
				break;
				}
			};
		if ((_bird_on_sling == 0)&&(sling!=null)&&(pigs.size()>0)&&(bird_count>0))
		{
			ar.fullyZoomIn();
			//ar.fullyZoomIn();
			
			BufferedImage bscreenshot = ActionRobot.doScreenShot();
			ar.fullyZoom();
			Vision bvision = new Vision(bscreenshot);
			Rectangle bsling = bvision.findSlingshot();
			List<Rectangle> bred_birds = bvision.findRedBirds();
			List<Rectangle> bblue_birds = bvision.findBlueBirds();
			List<Rectangle> byellow_birds = bvision.findYellowBirds();
			if (bsling != null)
				{
				if (!find_bird)
				{
				for (int i = 0;i<bred_birds.size();i++) 
				
				if (bsling.contains(bred_birds.get(i).getCenterX(), bred_birds.get(i).getCenterY())) 
					{
					_bird_on_sling = 1;
					find_bird = true;
					break;
					}
				};
			
				if (!find_bird)
				{
				for (int i = 0;i<bblue_birds.size();i++) 
				
				if (bsling.contains(bblue_birds.get(i).getCenterX(), bblue_birds.get(i).getCenterY())) 
					{
					_bird_on_sling = 2;
					find_bird = true;
					break;
					}
				};
				
				if (!find_bird)
				{
				for (int i = 0;i<byellow_birds.size();i++) 
				
				if (bsling.contains(byellow_birds.get(i).getCenterX(), byellow_birds.get(i).getCenterY())) 
					{
					_bird_on_sling = 3;
					find_bird = true;
					break;
					}
				};
			};
			
		}
		System.out.println("...found " + pigs.size() + " pigs and "
				+ bird_count + " birds");
		System.out.print("bird on sling:");
		switch (_bird_on_sling)
		{
		case 1:System.out.println("red");break;
		case 2:System.out.println("blue");break;
		case 3:System.out.println("yellow");break;
		default :System.out.println("not found");
		}
		
	*/
		
		GameState state = ar.checkState();

		// if there is a sling, then play, otherwise just skip.
		if (sling != null) {
			ar.fullyZoom();
			if (!pigs.isEmpty()) {
				System.out.println("!pigs.isEmpty()");

				// Initialise a shot list
				ArrayList<Shot> shots = new ArrayList<Shot>();
				Point releasePoint;
				{
					// random pick up a pig
					Random r = new Random();
					int index = 0;
					int minx = (int)pigs.get(0).getCenterX();
					System.out.printf("minx%d",minx);
					for (int k=0;k<pigs.size();k++)
					{
						if ((int)pigs.get(k).getCenterX()<minx)
						{
							index = k;
							minx = (int)pigs.get(k).getCenterX();
						}
					}
					
					Rectangle pig = pigs.get(index);
					Point _tpt = new Point((int) pig.getCenterX(),
							(int) pig.getCenterY());

					System.out.println("the target point is " + _tpt);

					// if the target is very close to before, randomly choose a
					// point near it
					if (prevTarget != null && distance(prevTarget, _tpt) < 10) {
						double _angle = r.nextDouble() * Math.PI * 2;
						_tpt.x = _tpt.x + (int) (Math.cos(_angle) * 10);
						_tpt.y = _tpt.y + (int) (Math.sin(_angle) * 10);
						System.out.println("Randomly changing to " + _tpt);
					}

					prevTarget = new Point(_tpt.x, _tpt.y);

					// estimate the trajectory
					ArrayList<Point> pts = tp.estimateLaunchPoint(sling, _tpt);

					// do a high shot when entering a level to find an accurate
					// velocity
					
					if (firstShot&&(pts.size()>1))
						 releasePoint = pts.get(1);
					else
						releasePoint = pts.get(0);
					/*
					if (firstShot && pts.size() > 1) {
						releasePoint = pts.get(1);
					} else if (pts.size() == 1)
						releasePoint = pts.get(0);
					else {
						// System.out.println("first shot " + firstShot);
						// randomly choose between the trajectories, with a 1 in
						// 6 chance of choosing the high one
						if (r.nextInt(6) == 0)
							releasePoint = pts.get(1);
						else
							releasePoint = pts.get(0);
					}
					*/
					Point refPoint = tp.getReferencePoint(sling);
					/* Get the center of the active bird */
					focus_x = (int) ((Env.getFocuslist()
							.containsKey(currentLevel)) ? Env.getFocuslist()
							.get(currentLevel).getX() : refPoint.x);
					focus_y = (int) ((Env.getFocuslist()
							.containsKey(currentLevel)) ? Env.getFocuslist()
							.get(currentLevel).getY() : refPoint.y);
					System.out.println("the release point is: " + releasePoint);
					/*
					 * =========== Get the release point from the trajectory
					 * prediction module====
					 */
					System.out.println("Shoot!!");
					if (releasePoint != null) {
						double releaseAngle = tp.getReleaseAngle(sling,
								releasePoint);
						System.out.println(" The release angle is : "
								+ Math.toDegrees(releaseAngle));
						int base = 0;
						if (releaseAngle > Math.PI / 4)
							base = 1400;
						else
							base = 550;
						 tap_time = (int) (base + Math.random() * 1500);
						tap_time = (int)((focus_x-releasePoint.getX())/Math.cos(releaseAngle)*2.5);
						if (releaseAngle > Math.PI / 4) tap_time+=1700;
						tap_time+=Math.random() * 100;
						tap_time-=Math.random() * 100;
						tap_time+=(focus_y-releasePoint.getY())/2;
						System.out.println("taptime:"+tap_time);
						
					//	tap_time=100;
						
						shots.add(new Shot(focus_x, focus_y, (int) releasePoint
								.getX() - focus_x, (int) releasePoint.getY()
								- focus_y, 0, tap_time));
					} else
						System.err.println("Out of Knowledge");
				}

				// check whether the slingshot is changed. the change of the
				// slingshot indicates a change in the scale.
				{
					ar.fullyZoom();
					screenshot = ActionRobot.doScreenShot();
					vision = new Vision(screenshot);
					Rectangle _sling = vision.findSlingshot();
					if (sling.equals(_sling)) {
						state = ar.shootWithStateInfoReturned(shots);
						// update parameters after a shot is made
						if (state == GameState.PLAYING) {
							screenshot = ActionRobot.doScreenShot();
							vision = new Vision(screenshot);
							List<Point> traj = vision.findTrajPoints();
							tp.adjustTrajectory(traj, sling, releasePoint);
							firstShot = false;
							
						}
					} else
						System.out
								.println("scale is changed, can not execute the shot, will re-segement the image");
				}
				if ((tap_time * 2 -1000)>0)
				try {
					Thread.sleep(tap_time * 2-1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				};
				
				int pigx = 0;
				int stonex = 0;
				int _pigx = 0;
				int _stonex = 0;
				screenshot = ActionRobot.doScreenShot();
				vision = new Vision(screenshot);
				 pigs = vision.findPigs();
				List<Rectangle> stoneBlocks = vision.findStones();
				for (int i = 0;i<pigs.size();i++) pigx += pigs.get(i).getCenterX();
				for (int i = 0;i<stoneBlocks.size();i++) stonex += stoneBlocks.get(i).getCenterX();
				
				
				do
				{
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					};
					_pigx = pigx;
					_stonex = stonex;
					screenshot = ActionRobot.doScreenShot();
					vision = new Vision(screenshot);
					 pigs = vision.findPigs();
					 stoneBlocks = vision.findStones();
					 pigx = 0;
					 stonex = 0;
					for (int i = 0;i<pigs.size();i++) pigx += pigs.get(i).getCenterX();
					for (int i = 0;i<stoneBlocks.size();i++) stonex += stoneBlocks.get(i).getCenterX();
					System.out.println("Not stationery, Waiting!"+pigx+"&"+_pigx);
					
				}
				while ((Math.abs(pigx - _pigx)>0.5)||(Math.abs(stonex - _stonex)>0.5));
				
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				};
				
				if (pigs.size() == 0)
			
				try {
					Thread.sleep(6000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				else
				{
				screenshot = ActionRobot.doScreenShot();
				vision = new Vision(screenshot);
				red_birds = vision.findRedBirds();
				blue_birds = vision.findBlueBirds();
				yellow_birds = vision.findYellowBirds();
				
				if ((red_birds.size() + blue_birds.size() + yellow_birds.size()) == 0)
					
					try {
						Thread.sleep(6000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					};
				}
				System.out.println("Finish");
				/*if ((pigs.size()<3)||(bird_count<3))
					try {
						Thread.sleep(7000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				else
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				*/
					
				
			}

		}
		return state;
	}

	public static void main(String args[]) {

		NaiveAgent na = new NaiveAgent();
		if (args.length > 0)
			na.currentLevel = Integer.parseInt(args[0]);
		na.run();

	}
}
