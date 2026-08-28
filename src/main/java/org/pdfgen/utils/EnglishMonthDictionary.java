
package org.pdfgen.utils;

public class EnglishMonthDictionary extends AbstractMonthDictionary {

    private final String[] months = {
            "January", "February", "March",
            "April", "May", "June",
            "July", "August", "September",
            "October", "November", "December",
    };

    @Override
    protected String getMonthsByMianownik(int index) {
        return months[index];
    }

    @Override
    protected String getMonthsByDopelniacz(int index) {
        return months[index];
    }

    @Override
    protected String getLocaleFormatting(String formatting, int day, String month, int year) {
        return switch (formatting) {
            case LocalDateOptions.DATE_FORMAT_DAY_MONTH_YEAR -> month + " " + day + getOrdinal(day) + " " + year;
            case LocalDateOptions.DATE_FORMAT_YEAR -> String.valueOf(year);

            default -> month + " " + year;
        };
    }

    private static String getOrdinal(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }

        return switch (day % 10) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
    }
}
