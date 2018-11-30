package me.Chestsort.main;

public class Timer {
	long beforeTime;
	long afterTime;
	long timePaused;
	boolean running;
	boolean isPauseTimer;
	Timer pauseTimer;

	public Timer() {
		beforeTime = 0;
		afterTime = 0;
		timePaused = 0;
		running = false;
		isPauseTimer = false;
		pauseTimer = new Timer("Pause Timer");
	}

	private Timer(String str) {
		beforeTime = 0;
		afterTime = 0;
		isPauseTimer = true;
		running = false;
	}

	public void start() {
		if (!isPauseTimer) {
			if (!running && !pauseTimer.running) {
				beforeTime = afterTime = System.currentTimeMillis();
				running = true;
			}
		} else if (isPauseTimer) {
			if (!running) {
				beforeTime = System.currentTimeMillis();
				running = true;
			}
		}
	}

	public void stop() {
		if (!isPauseTimer) { // Stop normal timer
			if (!running && pauseTimer.running) {
				unPause();
				pauseTimer.stop();
				timePaused = pauseTimer.getTime();
			} else if (running) {
				unPause();
				pauseTimer.stop();
				afterTime = System.currentTimeMillis();
				running = false;
			}
		} else if (isPauseTimer) { // Stop the pauseTimer
			if (running) {
				afterTime = System.currentTimeMillis();
				running = false;
			}
		}
	}

	public void pause() {
		if (running && !isPauseTimer) {
			pauseTimer.start();
			running = false;
		}
	}

	public void unPause() {
		if (!running && !isPauseTimer) {
			pauseTimer.stop();
			timePaused += pauseTimer.getTime();
			running = true;
		}
	}

	public long getTime() {
		if (!isPauseTimer) { // Time from normal timer
			if ((running) || (!running && pauseTimer.running)) {
				// Print.pl("one");
				return System.currentTimeMillis() - beforeTime - getPausedTime();
			} else if (!running && !pauseTimer.running) {
				// Print.pl("two");
				return afterTime - beforeTime - getPausedTime();
			}
		} else if (isPauseTimer) {
			if (running) {
				return System.currentTimeMillis() - beforeTime;
			} else {
				return afterTime - beforeTime;
			}
		}
		return 0;// Shouldn't happen
	}

	public String getTimeFormated() {
		if ((running) || (!running && pauseTimer.running)) {
			return convertMilliseconds(System.currentTimeMillis() - beforeTime - getPausedTime());
		} else if (!running && !pauseTimer.running) {
			return convertMilliseconds(afterTime - beforeTime - getPausedTime());
		}

		return "¯\\_(ツ)_/¯";
	}
	
	private String convertMilliseconds(long firstMilliseconds) {
		String result = "";
		
		long hours;
		int minutes;
		int seconds;
		int milliseconds;

		hours = (firstMilliseconds / 3600000);
		if (hours >= 1) {
			result += "Hours: " + hours;
			firstMilliseconds -= 3600000 * hours;
		} else {
			result += "Hours: " + 0;
		}
		
		result += " - ";

		minutes = (int) (firstMilliseconds / 60000);
		if (minutes >= 1) {
			result += "Minutes: " + minutes;
			firstMilliseconds -= 60000 * minutes;
		} else {
			result += "Minutes: " + 0;
		}

		result += " - ";
		
		seconds = (int) (firstMilliseconds / 1000);
		if (seconds >= 1) {
			result += "Seconds: " + seconds;
			firstMilliseconds -= 1000 * seconds;
		} else {
			result += "Seconds: " + 0;			
		}
		
		result += " - ";
		
		milliseconds = (int) firstMilliseconds;
		if(milliseconds >= 1) {
			result += "Milliseconds: " + milliseconds;
		} else {
			result += "Milliseconds: " + 0;
		}
		
		return result;
	}

	private long getPausedTime() {
		if (pauseTimer.running) {
			return timePaused + pauseTimer.getTime();
		} else {
			return timePaused;
		}
	}
}