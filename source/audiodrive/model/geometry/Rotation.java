package audiodrive.model.geometry;

public class Rotation extends Vector {
	
	@Override
	public Rotation x(double x) {
		return (Rotation) super.x(unify180(x));
	}
	
	@Override
	public Rotation y(double y) {
		return (Rotation) super.y(unify180(y));
	}
	
	@Override
	public Rotation z(double z) {
		return (Rotation) super.z(unify180(z));
	}
	
	/**
	 * Limits the given angle to the range 0 to 360. <br>
	 * <br>
	 * <i>For example: -5.0 -> 0.0 and 365.0 -> 360.0</i>
	 */
	public static double limit(double angle) {
		if (angle < 0.0) angle = 0.0;
		else if (angle > 360.0) angle = 360.0;
		return angle;
	}
	
	/**
	 * Unifies the given angle into the range 0 to 360. <br>
	 * <br>
	 * <i>For example: -5.0 -> 355.0 and 365.0 -> 5.0</i>
	 */
	public static double unify(double angle) {
		boolean negative = angle < 0.0;
		angle = angle % 360.0;
		if (angle == -0.0) return 0.0;
		if (negative) return 360 + angle;
		return angle;
	}
	
	/**
	 * Unifies the given angle into the range -180 to 180. <br>
	 * <br>
	 * <i>For example: 185.0 -> -175.0 and -185.0 -> 175.0</i>
	 */
	public static double unify180(double angle) {
		double remainder = Math.IEEEremainder(angle, 2 * 180.0);
		if (remainder == -0.0) return 0.0;
		return remainder;
	}
	
}
