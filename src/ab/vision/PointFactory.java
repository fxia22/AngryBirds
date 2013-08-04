package ab.vision;

public class PointFactory {
/**
 * 单例模式，大批量产生Point，也可以手动产生Point
 */
	private JPoint[] points = null;
	private int newIndex;
	private int firstIndex = 0;

	public JPoint[] getPoints() {
		return points;
	}

	public int getFirstIndex() {
		return firstIndex;
	}

	public static PointFactory getInstance() {
		return new PointFactory();
	}

	public static PointFactory getInstance(int count) {
		return new PointFactory(count);
	}
	
	public static PointFactory getInstance(int[] x, int[] y) {
		return new PointFactory(x, y);
	}

	private PointFactory() {
		this(10);
	}

	private PointFactory(int count) {
		points = new JPoint[count];
		for (int i = 0; i < count; i++) {
			points[i] = new JPoint();
			newIndex = i;
			validatePoints();
		}
		firstIndex = getFirstPoint();
	}

	public PointFactory(int[] x, int[] y) {
		points = new JPoint[y.length];
		for (int i = 0; i < y.length; i++) {
			points[i] = new JPoint(x[i], y[i]);
		}
		firstIndex = getFirstPoint();
	}

	private void validatePoints() {
		for(int i = 0; i < newIndex; i++) {
				if(points[i].equals(points[newIndex])) {
					points[newIndex] = new JPoint();
					validatePoints();
				}
			}
	}

	public int getFirstPoint() {
		int minIndex = 0;
		for (int i = 1; i < points.length; i++) {
			if (points[i].getY() < points[minIndex].getY()) {
				minIndex = i;
			} else if ((points[i].getY() == points[minIndex].getY())
					&& (points[i].getX() < points[minIndex].getX())) {
				minIndex = i;
			}
		}
		return minIndex;
	}

}