package audiodrive.utilities;

import org.newdawn.slick.util.LogSystem;

public class SlickLog implements LogSystem {
	
	public static void bind() {
		org.newdawn.slick.util.Log.setLogSystem(new SlickLog());
	}
	
	@Override
	public void debug(String message) {
		Log.debug(message);
	}
	
	@Override
	public void error(Throwable throwable) {
		Log.error(throwable);
	}
	
	@Override
	public void error(String message) {
		Log.error(message);
	}
	
	@Override
	public void error(String message, Throwable throwable) {
		Log.error(message, throwable);
	}
	
	@Override
	public void info(String message) {
		Log.info(message);
	}
	
	@Override
	public void warn(String message) {
		Log.warning(message);
	}
	
	@Override
	public void warn(String message, Throwable throwable) {
		Log.warning(message, throwable);
	}
	
}
