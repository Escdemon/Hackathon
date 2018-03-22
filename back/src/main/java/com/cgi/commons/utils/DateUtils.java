package com.cgi.commons.utils;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.HOUR;
import static java.util.Calendar.MILLISECOND;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.SECOND;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Classe regroupant des méthodes utilitaires pour gérer les dates. Comprend de base pas mal de choses héritées du DateUtils de Apache.
 * 
 */
public final class DateUtils {

	/**
	 * Constructor private.
	 */
	private DateUtils() {
	}

	/**
	 * 
	 * Renvoie la date sous le format "dd/MM/yyyy hh:mm".
	 * 
	 * @param date
	 *            date à formatter.
	 * @return la date formattée.
	 */
	public static String formatDateHeure(final Date date) {
		if (date != null) {
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			return format.format(date);
		}
		return "";
	}
	
	/**
	 * 
	 * Renvoie le timestamp sous le format "dd/MM/yyyy hh:mm:ss.S".
	 * 
	 * @param date
	 *            date à formatter.
	 * @return la date formattée.
	 */
	public static String formatDateHeure(final Timestamp date) {
		if (date != null) {
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.S");
			return format.format(date);
		}
		return "";
	}
	
	/**
	 * 
	 * Renvoie le time sous le format "HH:mm:ss".
	 * 
	 * @param date
	 *            date à formatter.
	 * @return la date formattée.
	 */
	public static String formatHeure(final Time date) {
		if (date != null) {
			SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
			return format.format(date);
		}
		return "";
	}

	/**
	 * 
	 * Renvoie la date sous le format "dd/MM/yyyy".
	 * 
	 * @param date
	 *            date à formatter.
	 * @return la date formattée.
	 */
	public static String formatDate(final Date date) {
		if (date != null) {
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
			return format.format(date);
		}
		return "";
	}

	/**
	 * convertit une chaîne au format Date.
	 * 
	 * @param sDate
	 *            date au format String.
	 * @return Date
	 * @throws Exception If error.
	 */
	public static Date stringToDate(String sDate) throws Exception {
		SimpleDateFormat stringToDate;
		try {
			stringToDate = new SimpleDateFormat("dd/MM/yyyy");
			return stringToDate.parse(sDate);
		} catch (ParseException e) {
			stringToDate = new SimpleDateFormat("yyyy-MM-dd");
			return stringToDate.parse(sDate);
		}
	}

	/**
	 * convertit une chaîne au format Time.
	 * 
	 * @param sTime
	 *            time au format String.
	 * @return Time
	 * @throws Exception If error.
	 */
	public static Time stringToTime(String sTime) throws Exception {
		SimpleDateFormat stringToTime = new SimpleDateFormat("HH:mm:ss");
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTimeInMillis(stringToTime.parse(sTime).getTime());
		return new Time(cal.getTimeInMillis());
	}

	/**
	 * convertit une chaîne au format TimeStamp.
	 * 
	 * @param sTimestamp
	 *            timestamp au format String.
	 * @return Timestamp
	 * @throws Exception If error.
	 */
	public static Timestamp stringToTimestamp(String sTimestamp) throws Exception {
		Date d;
		SimpleDateFormat stringToDate;
		try {
			stringToDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.S");
			d = stringToDate.parse(sTimestamp);
		} catch (ParseException e) {
			stringToDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
			d = stringToDate.parse(sTimestamp);
		}
		return new Timestamp(d.getTime());
	}

	/**
	 * Add a number of days to a Date.
	 * 
	 * @param d The Date.
	 * @param nbJours The Number of days
	 * @return The new Date.
	 */
	public static Date addJours(Date d, int nbJours) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(DAY_OF_MONTH, nbJours);
		return c.getTime();
	}

	/**
	 * Add a number of months to a Date.
	 * 
	 * @param d The Date.
	 * @param nbMois The Number of months
	 * @return The new Date.
	 */
	public static Date addMois(Date d, int nbMois) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(MONTH, nbMois);
		return c.getTime();
	}

	/**
	 * Renvoie une date initialisée au moment de l'appel de cette méthode. La précision est la milliseconde.
	 * 
	 * @return Timestamp correspondant au moment présent.
	 */
	public static Date todayNow() {
		return new Date();
	}

	/**
	 * Renvoie une date initialisée au moment de l'appel de cette méthode. La précision est la seconde.
	 * 
	 * @return Une date contenant les heures, minutes et secondes uniquement.
	 */
	public static Date now() {
		Calendar now = createCalendar(MILLISECOND);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(0);
		calendar.set(HOUR, now.get(HOUR));
		calendar.set(MINUTE, now.get(MINUTE));
		calendar.set(SECOND, now.get(SECOND));
		return calendar.getTime();
	}

	/**
	 * Renvoie une date initialisée au jour de l'appel de cette méthode. La précision est la journée. L'heure est fixée à zéro.
	 * 
	 * @return Date correspondant au jour de l'appel.
	 */
	public static Date today() {
		Calendar calendar = createCalendar(HOUR, MINUTE, SECOND, MILLISECOND);
		calendar.set(Calendar.AM_PM, Calendar.AM);
		return calendar.getTime();
	}

	/**
	 * Creates a new calendar with the given fields to reset.
	 * 
	 * @param resetFields
	 *            Fields to reset (the value is set to {@code 0}).
	 * @return A new calendar.
	 * @see Calendar#getInstance()
	 * @see Calendar#set(int, int)
	 */
	private static Calendar createCalendar(int... resetFields) {
		Calendar calendar = Calendar.getInstance();
		for (int field : resetFields) {
			calendar.set(field, 0);
		}
		return calendar;
	}

	/**
	 * Renvoie la date courante sous le format "yyMMdd".
	 * 
	 * @return date au format YYMMDD
	 */
	public static String getDateYYMMDD() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd");
		Date currentTime = new Date();
		String date = formatter.format(currentTime);

		return date;
	}

	/**
	 * Méthode calculant le nombre d'heure séparant deux dates.
	 * 
	 * @param dateA first date
	 * @param dateB second date
	 * @return Nombre d'heures séparant deux dates
	 */
	public static int getNbHours(Timestamp dateA, Timestamp dateB) {
		return (int) ((dateB.getTime() - dateA.getTime()) / 3600000);
	}

	/**
	 * Retourne la date initialisée au moment de l'appel au format "yyyyMMddHHmmssSSS".
	 * 
	 * @return chaîne de caractère.
	 */
	public static String nowTimestamp() {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String dateFormatee = format.format(DateUtils.todayNow());
		return dateFormatee;
	}
}
