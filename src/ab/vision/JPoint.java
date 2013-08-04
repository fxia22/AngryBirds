package ab.vision;

public class JPoint {
	
//	������x��y���֮꣬������int���ͣ���Ϊ���պ�����ڼ������Ļ�Ͻ��п��ӻ���
	private int x;
	private int y;

//	x,y��get����
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
//	����㵽��Ļ��Ե�ľ���
	private static double PADDING = 20;
//	������Ļ�еķ�Χ
	private static double POINT_RANGE = (800 - PADDING * 2);

//	Ĭ�Ϲ��췽�������������
	public JPoint() {
		this.x = (int) ((Math.random() * POINT_RANGE) + PADDING);
		this.y = (int) ((Math.random() * POINT_RANGE) + PADDING);
	}
	
//	���ι��췽��������ʵ���ֶ�����̶���	
	public JPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}

//	��дhashCode()��equals()������ʵ�ֱȽϺ�Hash
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		JPoint other = (JPoint) obj;
		if ((x == other.x) && (y == other.y))
			return true;
		
		return false;
	}
	
	
}