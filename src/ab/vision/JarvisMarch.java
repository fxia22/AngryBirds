package ab.vision;
import static java.lang.Math.abs;

import java.awt.Point;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;


public class JarvisMarch {

	private int count;

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	private static int MAX_ANGLE = 4;
	private double currentMinAngle = 0;
	private List<JPoint> hullPointList;
	private List<Integer> indexList;
	private PointFactory pf;
	private JPoint[] ps;

	public JPoint[] getPs() {
		return ps;
	}

	private int firstIndex;

	public int getFirstIndex() {
		return firstIndex;
	}

	public JarvisMarch() {
		this(10);
	}

	public JarvisMarch(int count) {
		pf = PointFactory.getInstance(count);
		initialize();
	}

	public JarvisMarch(int[] x, int[] y) {
		pf = PointFactory.getInstance(x, y);
		initialize();
	}

	private void initialize() {
		hullPointList = new LinkedList<JPoint>();
		indexList = new LinkedList<Integer>();
		firstIndex = pf.getFirstIndex();
		ps = pf.getPoints();
		addToHull(firstIndex);
	}

	private void addToHull(int index) {
		indexList.add(index);
		hullPointList.add(ps[index]);
	}

	public int calculateHull() {
		for (int i = getNextIndex(firstIndex); i != firstIndex; i = getNextIndex(i)) {
			addToHull(i);
		}
		//showHullPoints();
		return 0;
	}

	public ArrayList<Point> showHullPoints() {
		Iterator<JPoint> itPoint = hullPointList.iterator();
		Iterator<Integer> itIndex = indexList.iterator();
		JPoint p;
		ArrayList<Point> hullPoint = new ArrayList<Point>();
		int i;
		int index = 0;
	//	System.out.println("The hull points is: -> ");
		while (itPoint.hasNext()) {
			i = itIndex.next();
			p = itPoint.next();
		//	System.out.print(i + ":(" + p.getX() + "," + p.getY() + ")  ");
			
			hullPoint.add(new Point(p.getX(),p.getY()));
			index++;
		//	if (index % 10 == 0)
		//		System.out.println();
		}
	//	System.out.println();
	//	System.out.println("****************************************************************");
	//	System.out.println("The count of all hull points is " + index);
		return hullPoint;
	}

	public int getNextIndex(int currentIndex) {
		double minAngle = MAX_ANGLE;
		double pseudoAngle;
		int minIndex = 0;
		for (int i = 0; i < ps.length; i++) {
			if (i != currentIndex) {
				pseudoAngle = getPseudoAngle(ps[i].getX() - ps[currentIndex].getX(), 
											 ps[i].getY() - ps[currentIndex].getY());
				if (pseudoAngle >= currentMinAngle && pseudoAngle < minAngle) {
					minAngle = pseudoAngle;
					minIndex = i;
				} else if (pseudoAngle == minAngle){
						if((abs(ps[i].getX() - ps[currentIndex].getX()) > 
							abs(ps[minIndex].getX() - ps[currentIndex].getX()))
							|| (abs(ps[i].getY() - ps[currentIndex].getY()) > 
							abs(ps[minIndex].getY() - ps[currentIndex].getY()))){
							minIndex = i;
						}
				}
			}

		}
		currentMinAngle = minAngle;
		return minIndex;
	}

	public double getPseudoAngle(double dx, double dy) {
		if (dx > 0 && dy >= 0)
			return dy / (dx + dy);
		if (dx <= 0 && dy > 0)
			return 1 + (abs(dx) / (abs(dx) + dy));
		if (dx < 0 && dy <= 0)
			return 2 + (dy / (dx + dy));
		if (dx >= 0 && dy < 0)
			return 3 + (dx / (dx + abs(dy)));
		throw new Error("Impossible");
	}

}