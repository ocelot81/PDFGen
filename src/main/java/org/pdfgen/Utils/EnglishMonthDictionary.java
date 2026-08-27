
package org.pdfgen.Utils;

import java.util.Objects;

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
        if (Objects.equals(formatting, LocalDateOptions.DATE_FORMAT_MONTH_YEAR)) {
            return month + " " + year;
        }
        return month + " " + day + getOrdinal(day) + " " + year;
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
