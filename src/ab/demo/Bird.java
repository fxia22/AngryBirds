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

public class Bird 
{
	private static final int RED = 1;
	private static final int YELLOW = 2;
	private static final int BLUE = 3;
	private static final int BLACK = 4;
	private static final int WHITE = 5;
	
	
	
	public int color;
	public Rectangle rect;
	public int x;
	public int y;
	
	public Bird(Rectangle r, int c)
	{
		this.rect = r;
		this.color = c;
		this.x = (int)r.getCenterX();
		this.y = (int)r.getCenterY();
		
	};
	
	public Bird()
	{
		this(new Rectangle(0,0,0,0),0);
		
	}
	
	
	
	
}