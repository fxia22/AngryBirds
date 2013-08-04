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
import ab.demo.other.Shot;
import ab.demo.Bird;
import ab.planner.TrajectoryPlanner;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;
import ab.vision.*;

//Naive agent (server/client version)

public class ClientNaiveAgent implements Runnable {
	private static final int RED = 1;
	private static final int YELLOW = 2;
	private static final int BLUE = 3;
	private static final int BLACK = 4;
	private static final int WHITE = 5;
	//focus point
	private int focus_x;
	private int focus_y;
	//Wrapper of the communicating messages
	private ClientActionRobotJava ar;
	public byte currentLevel = 19;
	TrajectoryPlanner tp;
	private int id = 18898;
	private boolean firstShot;
	private Point prevTarget;
	private int level = 0;
	private List<Integer> pigcount =new ArrayList<Integer>();
	int last_shot_score = 0;
	int _last_shot_score = 0;
	Shot lastshot;
	int lastshotp = 0;
	int fail = 0;
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
		
		boolean allplayed = false;
		ar.configure(ClientActionRobot.intToByteArray(id));
		//load the initial level (default 1)
		//ar.loadLevel((byte)15);
		int[] s = ar.checkScore();
		level = s.length;
		
		
		ar.loadLevel(currentLevel);
		fail = 0;
		ar.clickInCenter();
		pigcount.clear();
		GameState state;
		while (true) {
			
			state = solve();
			
			//If the level is solved , go to the next level
			if (state == GameState.WON) {
							
				
				fail = 0;
				System.out.println("Pigcount list:"+pigcount.toString());
				pigcount.clear();
				_last_shot_score = 0;
				last_shot_score = 0;
				
				System.out.println(" loading the level " + (currentLevel + 1) );
				currentLevel++;
				if (currentLevel>level) allplayed = true;
				if (allplayed) 
				{
					System.out.println("All Played!");
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
					int myscores[] = ar.checkMyScore();
					for (int i = 0; i < myscores.length ; i ++)
					{
					      /*if(scores[i] == 0)
					    	  break;
					      else*/
					    	  System.out.print( " level " + (i+1) + ": " + myscores[i]);
					}
					int max = -100000;
					int max2 = -100000;
					int max3 = -100000;
					
					
					for (int i = 0; i < myscores.length ; i ++)
					{
						if (scores[i]-myscores[i]>max) 
						{
							max = scores[i]-myscores[i];
							maxi = i;
							
						}
					}
					
					System.out.println("Play level"+(maxi+1));
					System.out.println();
					currentLevel = (byte)(maxi+1);
				}
				
				
				ar.loadLevel(currentLevel);
				
				ar.clickInCenter();
				pigcount.clear();

				//display the global best scores
				
				// make a new trajectory planner whenever a new level is entered
				tp = new TrajectoryPlanner();

				// first shot on this level, try high shot first
				firstShot = true;
				
			} else 
				//If lost, then restart the level
				if (state == GameState.LOST) {
				ar.restartLevel();
				fail++;
				System.out.println("Fail:"+fail);
				if (fail>1) 
				{
					if (allplayed) 
					{
						System.out.println("All Played!");
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
						int myscores[] = ar.checkMyScore();
						for (int i = 0; i < myscores.length ; i ++)
						{
						      /*if(scores[i] == 0)
						    	  break;
						      else*/
						    	  System.out.print( " level " + (i+1) + ": " + myscores[i]);
						}
						int max = -100000;
						int maxi = 0;
						for (int i = 0; i < myscores.length ; i ++)
						{
							if (scores[i]-myscores[i]>max) 
							{
								max = scores[i]-myscores[i];
								maxi = i;
								
							}
						}
						
						System.out.println("Play level"+(maxi+1));
						System.out.println();
						currentLevel = (byte)(maxi+1);
					}
					else currentLevel+=1;
					ar.loadLevel(currentLevel);
					fail = 0;
					
				}
				System.out.println("restart");
				
				pigcount.clear();
			} else 
				if (state == GameState.LEVEL_SELECTION) {
				System.out.println("unexpected level selection page, go to the last current level : "
								+ currentLevel);
				ar.loadLevel(currentLevel);
				fail = 0;
			} else if (state == GameState.MAIN_MENU) {
				System.out
						.println("unexpected main menu page, reload the level : "
								+ currentLevel);
				ar.loadLevel(currentLevel);
				fail = 0;
			} else if (state == GameState.EPISODE_MENU) {
				System.out.println("unexpected episode menu page, reload the level: "
								+ currentLevel);
				ar.loadLevel(currentLevel);
				fail = 0;
			}

		}

	}


	  /** 
	   * Solve a particular level by shooting birds directly to pigs
	   * @return GameState: the game state after shots.
     */
	public GameState solve()

	{
		
		int wait_time = 0 ;
		// capture Image
		BufferedImage screenshot = ar.doScreenShot();

		// process image
		Vision vision = new Vision(screenshot);
		GameStateExtractor game = new GameStateExtractor();
		GameStateExtractor.GameState gstate = game.getGameState(screenshot);
		
		
		_last_shot_score = last_shot_score;
		
		if (gstate == GameStateExtractor.GameState.PLAYING) {
			System.out.println("In game score : " + game.getScoreInGame(screenshot));
			last_shot_score = game.getScoreInGame(screenshot);
		};
		
		
		Rectangle sling = vision.findSlingshot();
		
		//If the level is loaded (in PLAYING State)but no slingshot detected, then the agent will request to fully zoom out.
		while (sling == null && ar.checkState() == GameState.PLAYING) {
			System.out.println("no slingshot detected. Please remove pop up or zoom out");
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				
				e.printStackTrace();
				
				
			}
			
			
			
		
			
			ar.fullyZoomOut();
			screenshot = ar.doScreenShot();
			vision = new Vision(screenshot);
			sling = vision.findSlingshot();
		}

		//find birds and pigs
		List<Rectangle> red_birds = vision.findRedBirds();
		List<Rectangle> blue_birds = vision.findBlueBirds();
		List<Rectangle> yellow_birds = vision.findYellowBirds();
		List<Rectangle> black_birds = vision.findBlackBirds();
		List<Rectangle> white_birds = vision.findWhiteBirds();
		List<Rectangle> pigs = vision.findPigs();
		int bird_count = 0;
		bird_count = red_birds.size() + blue_birds.size() + yellow_birds.size();

		System.out.println("...found " + pigs.size() + " pigs and "
				+ bird_count + " birds");
		GameState state = ar.checkState();
		pigcount.add(pigs.size());
		/**
		 * 找出弹弓上的鸟
		 */
		Bird b = new Bird();
		boolean tap = false;
		int _bird_on_sling = 0;
		int bird_on_sling = 0;
		boolean find_bird = false;
		List<Bird> birdlist = new ArrayList<Bird>();
		birdlist.clear();
		for (int i = 0; i<red_birds.size();i++) {
			birdlist.add(new Bird(red_birds.get(i),1));
		}
		for (int i = 0; i<yellow_birds.size();i++) {
			birdlist.add(new Bird(yellow_birds.get(i),2));
		}
		for (int i = 0; i<blue_birds.size();i++) {
			birdlist.add(new Bird(blue_birds.get(i),3));
		}
		for (int i = 0; i<black_birds.size();i++) {
			birdlist.add(new Bird(black_birds.get(i),4));
		}
		for (int i = 0; i<white_birds.size();i++) {
			birdlist.add(new Bird(white_birds.get(i),5));
			System.out.println("White"+white_birds.get(i).toString());
		}
		if (sling!=null)
		
		for (int i=0;i<birdlist.size();i++)
		{
			if (sling.contains(birdlist.get(i).x, birdlist.get(i).y)) 
			{
				System.out.println(birdlist.get(i).color);
				if ((birdlist.get(i).color==BLUE)||(birdlist.get(i).color == YELLOW)) tap = true;
				find_bird = true;
				break;
			}
		}
		
		
		
		
		
		if ((!find_bird)&&(sling!=null)&&(pigs.size()>0))
		{
			ar.clickInCenter();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ar.clickInCenter();
			ar.fullyZoomIn();
			//ar.fullyZoomIn();
			
			BufferedImage bscreenshot = ar.doScreenShot();
			//ar.fullyZoomOut();
			Vision bvision = new Vision(bscreenshot);
			Rectangle bsling = bvision.findSlingshot();
			List<Rectangle> bred_birds = bvision.findRedBirds();
			List<Rectangle> bblue_birds = bvision.findBlueBirds();
			List<Rectangle> byellow_birds = bvision.findYellowBirds();
			List<Rectangle> bblack_birds = bvision.findBlackBirds();
			List<Rectangle> bwhite_birds = bvision.findWhiteBirds();
			
			/**
			 * 找出弹弓上的鸟
			 */
			
			//int _bird_on_sling = 0;
			//birdcolor bird_on_sling = RED;
			//boolean find_bird = false;
			List<Bird> bbirdlist = new ArrayList<Bird>();
			bbirdlist.clear();
			for (int i = 0; i<bred_birds.size();i++) {
				bbirdlist.add(new Bird(bred_birds.get(i),1));
			}
			for (int i = 0; i<byellow_birds.size();i++) {
				bbirdlist.add(new Bird(byellow_birds.get(i),2));
			}
			for (int i = 0; i<bblue_birds.size();i++) {
				bbirdlist.add(new Bird(bblue_birds.get(i),3));
			}
			for (int i = 0; i<bblack_birds.size();i++) {
				bbirdlist.add(new Bird(bblack_birds.get(i),4));
			}
			for (int i = 0; i<bwhite_birds.size();i++) {
				bbirdlist.add(new Bird(bwhite_birds.get(i),5));
				
			}
			if (bsling!=null)
			for (int i=0;i<bbirdlist.size();i++)
			{
				if (bsling.contains(bbirdlist.get(i).x, bbirdlist.get(i).y)) 
				{
					System.out.println(bbirdlist.get(i).color);
					if ((bbirdlist.get(i).color==BLUE )||(bbirdlist.get(i).color==YELLOW)) tap = true;
					find_bird = true;
					break;
				}
			}
			
		}
		
		

		if ((!find_bird)&&(sling!=null)&&(pigs.size()>0))
		{
			ar.clickInCenter();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//ar.fullyZoomIn();
			
			BufferedImage bscreenshot = ar.doScreenShot();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ar.fullyZoomOut();
			Vision bvision = new Vision(bscreenshot);
			Rectangle bsling = bvision.findSlingshot();
			List<Rectangle> bred_birds = bvision.findRedBirds();
			List<Rectangle> bblue_birds = bvision.findBlueBirds();
			List<Rectangle> byellow_birds = bvision.findYellowBirds();
			List<Rectangle> bblack_birds = bvision.findBlackBirds();
			List<Rectangle> bwhite_birds = bvision.findWhiteBirds();
			
			/**
			 * 找出弹弓上的鸟
			 */
			
			//int _bird_on_sling = 0;
			//birdcolor bird_on_sling = RED;
			//boolean find_bird = false;
			List<Bird> bbirdlist = new ArrayList<Bird>();
			bbirdlist.clear();
			for (int i = 0; i<bred_birds.size();i++) {
				bbirdlist.add(new Bird(bred_birds.get(i),1));
			}
			for (int i = 0; i<byellow_birds.size();i++) {
				bbirdlist.add(new Bird(byellow_birds.get(i),2));
			}
			for (int i = 0; i<bblue_birds.size();i++) {
				bbirdlist.add(new Bird(bblue_birds.get(i),3));
			}
			for (int i = 0; i<bblack_birds.size();i++) {
				bbirdlist.add(new Bird(bblack_birds.get(i),4));
			}
			for (int i = 0; i<bwhite_birds.size();i++) {
				bbirdlist.add(new Bird(bwhite_birds.get(i),5));
				
			}
			if (bsling!=null)
			for (int i=0;i<bbirdlist.size();i++)
			{
				if (bsling.contains(bbirdlist.get(i).x, bbirdlist.get(i).y)) 
				{
					System.out.println(bbirdlist.get(i).color);
					if ((bbirdlist.get(i).color==BLUE )||(bbirdlist.get(i).color==YELLOW)) tap = true;
					find_bird = true;
					break;
				}
			}
			
		}

		if (!find_bird) 
			if (!pigs.isEmpty())
				{
				return state;
				}
			else
			{
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				}
			}
		
		int tap_time = 100;
		// if there is a sling, then play, otherwise skip.
		if (sling != null) {
			
			/*BufferedImage birdshot = ar.doScreenShot();
			Vision birdvision = new Vision(birdshot);
			List<Rectangle> red = birdvision.findRedBirds();
			List<Rectangle> blue = birdvision.findBlueBirds();
			List<Rectangle> yellow = birdvision.findYellowBirds();
			List<Rectangle> black = birdvision.findBlackBirds();
			List<Rectangle> white = birdvision.findWhiteBirds();
			int bird = red.size() + blue.size() + yellow.size();
			
			
			
			for (int i=0; i<birdlist.size();i++)
			{
				System.out.println(birdlist.get(i).col);
			}
			
			
			System.out.println("birdsize"+bird);
			*/
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
					for (int i = 0; i<20;i++)
					{	
						Point p = new Point((int)((pig.getCenterX()+sling.getX())/2+(pig.getCenterX()-sling.getX())*i/10),(int)((pig.getCenterY()+sling.getY())/2+(pig.getCenterY()-sling.getY())*i/10));
						tr.add(p);
					//	System.out.println(p.toString());
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
					for (int i=0;i<18; i++)
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
					if ((_last_shot_score==0)||(_last_shot_score>=1500))
					{
						if ((in>1)&&(pts.size()>1)&&(r.nextInt(4)!=1)||(firstShot)&&((pts.size()>1)))
						{
							releasePoint = pts.get(1);
							lastshotp = 1;
						}
							
						else
						{
							releasePoint = pts.get(0);
							lastshotp = 0;
						};
					}
					else
					{
						
						System.out.println("Change parabola"+" Last p "+lastshotp);
						if ((lastshotp ==0)&& (pts.size()>1))							
							{
							releasePoint = pts.get(1);
							}
						else releasePoint = pts.get(0);
								
						lastshotp = 1- lastshotp;
					};
					
						
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
					System.out.println("Last Shot Score:"+_last_shot_score);
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
						
						wait_time = tap_time * 2 -1000;
						if (!tap) tap_time = 0;
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
					if (Math.abs((sling.getCenterX()-(_sling.getCenterX())))<2) {

						// make the shot
						
						ar.fshoot(focus_x, focus_y, (int) releasePoint.getX()
								- focus_x, (int) releasePoint.getY() - 	focus_y,
								0, tap_time, false);
						
						lastshot = new Shot(focus_x, focus_y, (int) releasePoint
								.getX() - focus_x, (int) releasePoint.getY()
								- focus_y, 0, tap_time);
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
				
				if ((wait_time>0))
					try {
						Thread.sleep(wait_time);
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
					
					screenshot = ar.doScreenShot();
					System.out.println("Finish");
					if (gstate == GameStateExtractor.GameState.PLAYING) {
						System.out.println("In game score : " + game.getScoreInGame(screenshot));
						last_shot_score = game.getScoreInGame(screenshot) - last_shot_score;
					};
				
				
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
		
		na.run();
		//na.test();
	}
}
