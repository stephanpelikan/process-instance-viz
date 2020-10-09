package org.camunda.bpm.engine.impl.util;

import java.util.Date;

import org.slf4j.LoggerFactory;

/**
 * Replace Camundas ClockUtil by a thread based implementation
 * 
 * @author Stephan Pelikan
 */
public class ClockUtil {

	static class ClockUtilAttributes {
		long offset = 0;
		Date frozenTime = null;
		boolean trackStart = false;
		Date startTime = null;
	}
	
	private static final ThreadLocal<ClockUtilAttributes> attributes = new ThreadLocal<ClockUtilAttributes>() {
		@Override
		protected ClockUtilAttributes initialValue() {
			return new ClockUtilAttributes();
		}
	};
	
	/**
	 * Freezes the clock to a specified Date that will be returned by {@link #now()}
	 * and {@link #getCurrentTime()}
	 * 
	 * @param currentTime the Date to freeze the clock at
	 */
	public static void setCurrentTime(Date currentTime) {
		LoggerFactory.getLogger(ClockUtil.class).info("setCurrentTime: {}", currentTime);
		final ClockUtilAttributes currentAttributes = attributes.get();
		currentAttributes.frozenTime = currentTime;
		if (currentAttributes.trackStart) {
			currentAttributes.startTime = currentTime;
			currentAttributes.trackStart = false;
		}
	}

	public static void setTrackStart() {
		attributes.get().trackStart = true;
	}
	
	public static long timeEllapsed() {
		final ClockUtilAttributes currentAttributes = attributes.get();
		if (currentAttributes.startTime == null) {
			return -1;
		}
		return getCurrentTime().getTime() - currentAttributes.startTime.getTime();
	}
	
	public static void reset() {
		resetClock();
	}

	public static Date getCurrentTime() {
		return now();
	}

	public static Date now() {
		final ClockUtilAttributes currentAttributes = attributes.get();
		if (currentAttributes.frozenTime != null) {
			return currentAttributes.frozenTime;
		}
		return new Date(System.currentTimeMillis() + currentAttributes.offset);
	}

	/**
	 * Moves the clock by the given offset and keeps it running from that point on.
	 * 
	 * @param offsetInMillis the offset to move the clock by
	 * @return the new 'now'
	 */
	public static Date offset(Long offsetInMillis) {
		LoggerFactory.getLogger(ClockUtil.class).info("offset: {}", offsetInMillis);
		attributes.get().offset = offsetInMillis;
		return now();
	}

	public static Date resetClock() {
		LoggerFactory.getLogger(ClockUtil.class).info("resetClock");
		final ClockUtilAttributes currentAttributes = attributes.get();
		currentAttributes.frozenTime = null;
		currentAttributes.offset = 0;
		return now();
	}

}
