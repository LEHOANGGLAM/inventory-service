package com.yes4all.common.utils;

import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.StopWatch;

public class DateUtil {

    public static final String ISO_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static final String ISO_DATE_TIME_FORMAT_CUSTOM = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final String SIMPLE_DATE_FORMAT = "dd-MMM-yyyy";

    public static final String SIMPLE_DATE_TIME_FORMAT_EXPORT_PDF = "dd/MM/yy hh.mm.ss aa";

    public static final String SIMPLE_DATE_FORMAT_TO_SECOND_SIMPLE = "dd-MM-yyyy HH:mm:ss";

    public static final String SIMPLE_DATE_FORMAT_AM_PM_MARKER = "MM/dd/yyyy hh:mm:ss a";
    public static final String SIMPLE_DATE_FORMAT_NOT_MARKER = "MM/dd/yyyy HH:mm:ss";
    public static final String SIMPLE_DATE_FORMAT_TO_SECOND = "dd/MM/yyyy HH:mm:ss";
    public static final String SIMPLE_DATE_FORMAT_TO_DAY = "dd/MM/yyyy";
    public static final String SIMPLE_DATE_FORMAT_TO_MONTH = "MM/dd/yyyy";
    public static final String SIMPLE_DATE_FORMAT_SOURCING_UPLOAD = "dd-MMM-yyyy";
    public static final String STANDARD_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String STANDARD_DATE_TIME_CURRENT_FORMAT = "yyyyMMddHHmmss";
    public static final String STANDARD_YEAR_MONTH_SHORT_FORMAT = "yyMM";
    public static final String DATE_WITH_ALPHABET_MONTH_REGEX = "^([1-9]|[12]\\d|3[01])[-/.]([A-Z][a-z]{2}[-/.])(19|20)\\d{2}$";

    public static final String ZONE_DEFAULT = "America/Los_Angeles";
    public static final String ZONE_UTC = "UTC";
    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.of(ZONE_DEFAULT);

    public static String formatDate(Date date, String format) {
        DateFormat df = getDateFormat(format);
        return isNotEmpty(date) ? df.format(date) : "";
    }

    public static Date parseDateString(String dateStr, String pattern) {
        if (StringUtils.isNotBlank(dateStr) && StringUtils.isNotBlank(pattern)) {
            try {
                SimpleDateFormat df = new SimpleDateFormat(pattern);
                return df.parse(dateStr);
            } catch (ParseException ex) {
                return null;
            }
        }
        return null;
    }

    public static boolean isNotEmpty(Object data) {
        return !isEmpty(data);
    }

    public static boolean isEmpty(Object data) {
        return data == null;
    }

    public static DateFormat getDateFormat(String format) {
        return strictDateFormatForPattern(format);
    }

    public static Date convertStringToDate(String strDate) {
        try {
            //            // Add 12:00:00 because if not date will save back 1 day
            strDate = strDate + " 12:00:00";
            SimpleDateFormat formatter = new SimpleDateFormat(SIMPLE_DATE_FORMAT_TO_SECOND, Locale.ENGLISH);
            Date date = formatter.parse(strDate);
            return date;
        } catch (ParseException e) {
            return null;
        }
    }

    private static DateFormat strictDateFormatForPattern(String pattern) {
        DateFormat dateFormat = new SimpleDateFormat(pattern);
        dateFormat.setLenient(false);
        return dateFormat;
    }

    public static boolean validateDateFormat(String dateString) {
        if (CommonDataUtil.isEmpty(dateString)) {
            return false;
        }
        Pattern pattern = Pattern.compile(DATE_WITH_ALPHABET_MONTH_REGEX);
        return pattern.matcher(dateString).matches();
    }

    public static Date formatDateTime(String dateStr) {
        String format = DateUtil.validateDateFormat(dateStr) ? DateUtil.SIMPLE_DATE_FORMAT : DateUtil.ISO_DATE_TIME_FORMAT;
        return DateUtil.parseDateString(dateStr, format);
    }

    public static LocalDate convertStringLocalDate(String strDate) {
        String inputPattern = "MMM dd,yyyy";
        String outputPattern = "dd-MMM-yyyy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern, Locale.ENGLISH);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern, Locale.ENGLISH);
        Date date = null;
        String str = null;
        try {
            String strCurrentDate = strDate;
            date = inputFormat.parse(strCurrentDate); // it's format should be same as inputPattern
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date dateOutput = DateUtil.parseDateString(str, SIMPLE_DATE_FORMAT_SOURCING_UPLOAD);
        return dateOutput.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDateTime convertStringLocalDateTime(String strDate) {
        Date date = DateUtil.parseDateString(strDate, STANDARD_DATE_TIME_FORMAT);
        if (CommonDataUtil.isNotNull(date)) {
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        return null;
    }

    public static Date convertStringToInstantDate(String dateString) {
        return DateUtil.parseDateString(dateString, ISO_DATE_TIME_FORMAT_CUSTOM);
    }

    public static Date convertStringToInstantDate(String dateString, String format) {
        return DateUtil.parseDateString(dateString, format);
    }

    public static String convertInstantToDate(Instant dateStr) {
        SimpleDateFormat formatter = new SimpleDateFormat(SIMPLE_DATE_FORMAT_TO_DAY, Locale.ENGLISH);
        Date date = Date.from(dateStr);
        return formatter.format(date);
    }

    public static String convertInstantToDate(Instant dateStr, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.ENGLISH);
        Date date = Date.from(dateStr);
        return formatter.format(date);
    }

    public static StopWatch initStopWatch() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        return stopWatch;
    }

    public static String calculateTime(StopWatch stopWatch) {
        stopWatch.stop();
        String pattern = "mm:ss:SSS";
        Date date = new Date(stopWatch.getTotalTimeMillis());
        Format format = new SimpleDateFormat(pattern);
        return format.format(date) + " [mm:ss:SSS]";
    }

    public static Date currentDate() {
        return new Date();
    }

    public static Instant currentInstant() {
        return DateUtil.currentDate().toInstant();
    }

    public static Calendar getCalendar(Date date) {
        if (CommonDataUtil.isNull(date)) {
            date = DateUtil.currentDate();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    public static String getCurrentTimeByFormat() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(STANDARD_DATE_TIME_FORMAT);
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    public static Instant getStartOfDay(Instant date) {
        LocalDate localDate = LocalDate.ofInstant(date, DEFAULT_ZONE_ID);
        return localDate.atStartOfDay(DEFAULT_ZONE_ID).toInstant();
    }

    public static String toString(String format) {
        return toString(format, toZonedDateTime());
    }

    public static String toString(String format, ZonedDateTime inst) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(format);
        return fmt.format(inst);
    }

    public static ZonedDateTime toZonedDateTime() {
        return ZonedDateTime.now();
    }

    public static LocalDateTime convertInstantToLocalDateTime(Instant instant) {
        return convertInstantToLocalDateTime(instant, ZONE_DEFAULT);
    }

    public static LocalDateTime convertInstantToLocalDateTime(Instant instant, String zone) {
        try {
            if (CommonDataUtil.isNull(instant)) {
                return null;
            }
            ZoneId zoneId = ZoneId.of(zone);
            LocalDateTime dateTime = LocalDateTime.ofInstant(instant, zoneId);
            return dateTime;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String convertInstantToString(Instant instant, String pattern) {
        try {
            LocalDateTime dateTime = convertInstantToLocalDateTime(instant);
            if (CommonDataUtil.isNull(dateTime)) {
                return null;
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return dateTime.format(formatter);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static LocalDateTime convertStringToLocalDateTime(String dateStr, String pattern) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return LocalDateTime.parse(dateStr, formatter);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Instant convertStringToInstant(String dateStr, String pattern) {
        try {
            LocalDateTime ldt = convertStringToLocalDateTime(dateStr, pattern);
            if (CommonDataUtil.isNotNull(ldt)) {
                return ldt.atZone(DEFAULT_ZONE_ID).toInstant();
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Instant convertDateToDateTimeStart(String dateStr) {
        try {
            dateStr = dateStr + " 00:00:00";
            return convertStringToInstant(dateStr, SIMPLE_DATE_FORMAT_NOT_MARKER);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Instant convertDateToDateTimeEnd(String dateStr) {
        try {
            dateStr = dateStr + " 23:59:59";
            return convertStringToInstant(dateStr, SIMPLE_DATE_FORMAT_NOT_MARKER);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Instant currentInstantUTC() {
        return Instant.now();
    }
}
