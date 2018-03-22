package com.cgi.commons.utils;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

// TODO to be replaced by DateUtils
/**
 * Date time with notion of *NOW.
 */
public class DateTimeUpgraded {

	/**
	 * Value of the date.
	 */
	String valeur;

	/***
	 * Constructor.
	 * @param valeur The value.
	 */
	public DateTimeUpgraded(String valeur) {
		this.valeur = valeur;
	}

	/**
	 * Get the date.
	 * 
	 * @return the Date of the value if different of *NOW, else the date of the day.
	 */
	public Date getDate()
	{
		try {
			if ("NOW".equals(valeur)) {
				return DateUtils.today();
			} else {
				return DateUtils.stringToDate(valeur);
			}
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Get the time.
	 * 
	 * @return the Time of the value if different than {@code NOW}, else the current time.
	 */
	public Time getTime() {
		try {
			if ("NOW".equals(valeur)) {
				return new Time(DateUtils.now().getTime());
			}
			return DateUtils.stringToTime(valeur);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Get the timestamp.
	 * 
	 * @return the Timestamp of the value if different than {@code NOW}, else the current time.
	 */
	public Timestamp getTimestamp() {
		try {
			if ("NOW".equals(valeur)) {
				return new Timestamp(DateUtils.todayNow().getTime());
			}
			return DateUtils.stringToTimestamp(valeur);
		} catch (Exception e) {
			return null;
		}
	}
}
