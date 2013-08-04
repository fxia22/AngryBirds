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
import java.awt.Polygon;
import java.awt.image.BufferedImage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//import quicktime.qd.Polygon;

import ab.demo.other.ClientActionRobot;
import ab.demo.other.ClientActionRobotJava;
import ab.demo.other.Env;
import ab.planner.TrajectoryPlanner;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;
import ab.vision.*;

//Naive agent (server/client version)

public class ClientNaiveAgent implements Runnable {

	//focus point
	private int focus_x;
	private int focus_y;
	//Wrapper of the communicating messages
	private ClientActionRobotJava ar;
	public byte currentLevel = 1;
	TrajectoryPlanner tp;
	private int id = 18898;
	private boolean firstShot;
	private Point prevTarget;
	private List<Integer> pigcount =new ArrayList<Integer>();
	/**
	 * Constructor using the default IP
	 * */
	public ClientNaiveAgent() {
		// the default ip is the localhost
		ar = new ClientActionRobotJava("127.0.0.1");
		tp = new TrajectoryPlanner();
		prevTarget = null;
		firstShot = true;

	}
	/**
	 * Constructor with a specified IP
	 * */
	public ClientNaiveAgent(String ip) {
		ar = new ClientActionRobotJava(ip);
		tp = new TrajectoryPlanner();
		prevTarget = null;
		firstShot = true;

	}
	public ClientNaiveAgent(String ip, int id)
	{
		ar = new ClientActionRobotJava(ip);
		tp = new TrajectoryPlanner();
		prevTarget = null;
		firstShot = true;
		this.id = id;
	}
	

    /* 
     * Run the Client (Naive Agent)
     */
	public void run() {
		
	
		ar.configure(ClientActionRobot.intToByteArray(id));
		//load the initial level (default 1)
		//ar.loadLevel((byte)15);
		ar.loadLevel(currentLevel);
		ar.clickInCenter();
		pigcount.clear();
		GameState state;
		while (true) {
			
			state = solve();
			
			//If the level is solved , go to the next level
			if (state == GameState.WON) {
							
				
			
				System.out.println("Pigcount list:"+pigcount.toString());
				System.out.println(" loading the level " + (currentLevel + 1) );
				ar.loadLevel(++currentLevel);
				ar.clickInCenter();
				pigcount.clear();

				//display the global best scores
				int[] scores = ar.checkScore();
				System.out.println("The global best score: ");
				for (int i = 0; i < scores.length ; i ++)
				{
				    /*  if(scores[i] == 0)
				    	  break;
				      else*/
				    	  System.out.print( " level " + (i+1) + ": " + scores[i]);
				}
				System.out.println();
				System.out.println(" My score: ");
				scores = ar.checkMyScore();
				for (int i = 0; i < scores.length ; i ++)
				{
				      /*if(scores[i] == 0)
				    	  break;
				      else*/
				    	  System.out.print( " level " + (i+1) + ": " + scores[i]);
				}
				System.out.println();
				// make a new trajectory planner whenever a new level is entered
				tp = new TrajectoryPlanner();

				// first shot on this level, try high shot first
				firstShot = true;
				
			} else 
				//If lost, then restart the level
				if (state == GameState.LOST) {
				System.out.println("restart");
				ar.restartLevel();
				pigcount.clear();
			} else 
				if (state == GameState.LEVEL_SELECTION) {
				System.out.println("unexpected level selection page, go to the last current level : "
								+ currentLevel);
				ar.loadLevel(currentLevel);
			} else if (state == GameState.MAIN_MENU) {
				System.out
						.println("unexpected main menu page, reload the level : "
								+ currentLevel);
				ar.loadLevel(currentLevel);
			} else if (state == GameState.EPISODE_MENU) {
				System.out.println("unexpected episode menu page, reload the level: "
								+ currentLevel);
				ar.loadLevel(currentLevel);
			}

		}

	}


	  /** 
	   * Solve a particular level by shooting birds directly to pigs
	   * @return GameState: the game state after shots.
     */
	public GameState solve()

	{
		
		
		// capture Image
		BufferedImage screenshot = ar.doScreenShot();

		// process image
		Vision vision = new Vision(screenshot);

		Rectangle sling = vision.findSlingshot();
		
		//If the level is loaded (in PLAYING State)but no slingshot detected, then the agent will request to fully zoom out.
		while (sling == null && ar.checkState() == GameState.PLAYING) {
			System.out.println("no slingshot detected. Please remove pop up or zoom out");
			/*
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				
				e.printStackTrace();
				
				
			}
			*/
			
			
			BufferedImage birdshot = ar.doScreenShot();
			Vision birdvision = new Vision(birdshot);
			List<Rectangle> red = birdvision.findRedBirds();
			List<Rectangle> blue = birdvision.findBlueBirds();
			List<Rectangle> yellow = birdvision.findYellowBirds();
			int bird = red.size() + blue.size() + yellow.size();
			System.out.println("birdsize"+bird);
			ar.fullyZoomOut();
			screenshot = ar.doScreenShot();
			vision = new Vision(screenshot);
			sling = vision.findSlingshot();
		}

		//find birds and pigs
		List<Rectangle> red_birds = vision.findRedBirds();
		List<Rectangle> blue_birds = vision.findBlueBirds();
		List<Rectangle> yellow_birds = vision.findYellowBirds();
		List<Rectangle> pigs = vision.findPigs();
		int bird_count = 0;
		bird_count = red_birds.size() + blue_birds.size() + yellow_birds.size();

		System.out.println("...found " + pigs.size() + " pigs and "
				+ bird_count + " birds");
		GameState state = ar.checkState();
		pigcount.add(pigs.size());
		int tap_time = 100;
		// if there is a sling, then play, otherwise skip.
		if (sling != null) {
			ar.fullyZoomOut();
			
			//If there are pigs, we pick up a pig randomly and shoot it. 
			if (!pigs.isEmpty()) {		
				Point releasePoint;
				{
					// random pick up a pig
					Random r = new Random();

					
					/**
					 * 确定打哪只猪
					 */
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
					List<Point> tr= new ArrayList<Point>();
					tr.clear();
					for (int i = 0; i<10;i++)
					{	
						Point p = new Point((int)((pig.getCenterX()+sling.getX())/2+(pig.getCenterX()-sling.getX())*i/10),(int)((pig.getCenterY()+sling.getY())/2+(pig.getCenterY()-sling.getY())*i/10));
						tr.add(p);
						System.out.println(p.toString());
					}
					// if the target is very close to before, randomly choose a point near it
					if (prevTarget != null && distance(prevTarget, _tpt) < 10) {
						double _angle = r.nextDouble() * Math.PI * 2;
						_tpt.x = _tpt.x + (int) (Math.cos(_angle) * 10);
						_tpt.y = _tpt.y + (int) (Math.sin(_angle) * 10);
						System.out.println("Randomly changing to " + _tpt);
					}

					prevTarget = new Point(_tpt.x, _tpt.y);

					// estimate the trajectory
					ArrayList<Point> pts = tp.estimateLaunchPoint(sling, _tpt);
					
					
					/**
					 * 判断会不会和地形有交点
					 */
					
					List<Polygon> clods = vision.findClodsPolygon();
					System.out.println("clods:"+clods.size());
					System.out.println(clods.toString());
					int in = 0;
					for (int i=0;i<8; i++)
						for (int j = 0;j<clods.size();j++)
						{
							if (clods.get(j).contains(tr.get(i))){ in += 1;
							System.out.println("in==="+tr.get(i).toString());}
						}
					System.out.println("in:"+in);
					if (in > 2) System.out.println("***********WARNING COLLISION***********");
					
					/**
					 * 选择抛物线，通常选择较低的
					 */
					// do a high shot when entering a level to find an accurate velocity
					if ((in>0)&&(pts.size()>1)&&(r.nextInt(2)==0)||(firstShot)&&((pts.size()>1)))
						 releasePoint = pts.get(1);
					else
						releasePoint = pts.get(0);
					
					Point refPoint = tp.getReferencePoint(sling);
					//Get the center of the active bird as focus point 
					focus_x = (int) ((Env.getFocuslist()
							.containsKey(currentLevel)) ? Env.getFocuslist()
							.get(currentLevel).getX() : refPoint.x);
					focus_y = (int) ((Env.getFocuslist()
							.containsKey(currentLevel)) ? Env.getFocuslist()
							.get(currentLevel).getY() : refPoint.y);
					System.out.println("the release point is: " + releasePoint);

					// Get the release point from the trajectory prediction module
					System.out.println("Shoot!!");

					if (releasePoint != null) {
						double releaseAngle = tp.getReleaseAngle(sling,
								releasePoint);
						System.out.println(" The release angle is : "
								+ Math.toDegrees(releaseAngle));
						
						/**
						 * 确定点击时间
						 */
						tap_time = (int)((_tpt.getX()-releasePoint.getX())/Math.cos(releaseAngle)*1.4);
						if (releaseAngle > Math.PI / 4) tap_time+=800;
						tap_time+=Math.random() * 100;
						tap_time-=Math.random() * 100;
						
						System.out.println("taptime:"+tap_time);
						
					} else
						System.err.println("Out of Knowledge");
				}
				
				// check whether the slingshot is changed. the change of the slingshot indicates a change in the scale.
				{
					ar.fullyZoomOut();
					screenshot = ar.doScreenShot();
					vision = new Vision(screenshot);
					Rectangle _sling = vision.findSlingshot();
					if (sling.equals(_sling)) {

						// make the shot
						ar.fshoot(focus_x, focus_y, (int) releasePoint.getX()
								- focus_x, (int) releasePoint.getY() - 	focus_y,
								0, tap_time, false);
						/*
						 * Test shoot sequence...
						 * int[] shot_1 =  {focus_x, focus_y, (int) releasePoint.getX()
							- focus_x, (int) releasePoint.getY() - focus_y,
							0, tap_time};
							int[] shot_2 =  {focus_x, focus_y, (int) releasePoint.getX()
								- focus_x, (int) releasePoint.getY() - focus_y,
								0, tap_time};
							int[] shot_3 =  {focus_x, focus_y, (int) releasePoint.getX()
								- focus_x, (int) releasePoint.getY() - focus_y,
								0, tap_time};
						ar.cshootSequence(shot_1,shot_2,shot_3);
						*/
						
						// check the state after the shot
						state = ar.checkState();
						// update parameters after a shot is made
						if (state == GameState.PLAYING) {
							screenshot = ar.doScreenShot();
							vision = new Vision(screenshot);
							List<Point> traj = vision.findTrajPoints();
							tp.adjustTrajectory(traj, sling, releasePoint);
							firstShot = false;
						}
					} else
						System.out.println("scale is changed, can not execute the shot, will re-segement the image");
				}
				/**
				 * 确认场景是否静止
				 */
				{
				if ((tap_time * 2 -1000)>0)
					try {
						Thread.sleep(tap_time * 2-1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					};
					
					int pigx = 0;
					int stonex = 0;
					int stoney = 0;
					int _pigx = 0;
					int _stonex = 0;
					int _stoney = 0;
					screenshot = ar.doScreenShot();
					vision = new Vision(screenshot);
					 pigs = vision.findPigs();
					List<Rectangle> stoneBlocks = vision.findStones();
					for (int i = 0;i<pigs.size();i++) pigx += pigs.get(i).getCenterX();
					for (int i = 0;i<stoneBlocks.size();i++) stonex += stoneBlocks.get(i).getCenterX();
					for (int i = 0;i<stoneBlocks.size();i++) stoney += stoneBlocks.get(i).getCenterY();
					
					
					do
					{
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						};
						_pigx = pigx;
						_stonex = stonex;
						_stoney = stoney;
						screenshot = ar.doScreenShot();
						vision = new Vision(screenshot);
						 pigs = vision.findPigs();
						 stoneBlocks = vision.findStones();
						 pigx = 0;
						 stonex = 0;
						for (int i = 0;i<pigs.size();i++) pigx += pigs.get(i).getCenterX();
						for (int i = 0;i<stoneBlocks.size();i++) stonex += stoneBlocks.get(i).getCenterX();
						System.out.println("Not stationery, Waiting!"+pigx+"&"+_pigx);
						
					}
					while ((Math.abs(pigx - _pigx)>0.5)||(Math.abs(stonex - _stonex)>0.5)||(Math.abs(stoney - _stoney)>1));
					
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					};
					
					if (pigs.size() == 0)
				
					try {
						Thread.sleep(6500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					else
					{
					screenshot = ar.doScreenShot();
					vision = new Vision(screenshot);
					red_birds = vision.findRedBirds();
					blue_birds = vision.findBlueBirds();
					yellow_birds = vision.findYellowBirds();
					
					if ((red_birds.size() + blue_birds.size() + yellow_birds.size()) == 0)
						
						try {
							Thread.sleep(6500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						};
					}
					
					
					System.out.println("Finish");
				}
				
				
			}
			else
				return state;
		}
		return state;
	}

	private double distance(Point p1, Point p2) {
		return Math.sqrt((double) ((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y)* (p1.y - p2.y)));
	}
	public void test()
	{
		ar.configure(ClientActionRobot.intToByteArray(id));
		
		for (int i = 3; i < 22; i++)
		{
			System.out.println(" load level : " + i );
			ar.loadLevel((byte)i);
		}
		//ar.fullyZoomIn();
		//ar.clickInCenter();
	/*	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ar.clickInCenter();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ar.clickInCenter();*/
		//ar.fullyZoomOut();
	}
	public static void main(String args[]) {

		ClientNaiveAgent na;
		if(args.length > 0)
			na = new ClientNaiveAgent(args[0]);
		else
			na = new ClientNaiveAgent();
		
		//na.run();
		na.test();
	}
}
