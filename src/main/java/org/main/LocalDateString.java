
package org.main;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class LocalDateString {

    private static final String[] monthsDopelniacz = {
            "Stycznia", "Lutego", "Marca",
            "Kwietnia", "Maja", "Czerwca",
            "Lipca", "Sierpnia", "Września",
            "Października", "Listopada", "Grudnia",
    };

    private static final String[] monthsMianownik = {
            "Styczeń", "Luty", "Marzec",
            "Kwiecień", "Maj", "Czerwiec",
            "Lipiec", "Sierpień", "Wrześnień",
            "Październik", "Listopad", "Grudzień",
    };

    /**
     * Creates a string representation of date with provided configuration.
     * May be written out (In Polish)
     *
     * @param options Options of the representation (Arrangement, written_out, grammatical case)
     */
    public static String create(LocalDateOptions options) {
        LocalDate date = LocalDate.now();

        String Arrangement = options.arrangement;
        String WrittenOut = options.written_out;
        String GrammaCase = options.gramma_case;

        if (!Boolean.parseBoolean(WrittenOut)) {
            return date.format(DateTimeFormatter.ofPattern(Arrangement));
        }

        int year = date.getYear();
        int day = date.getDayOfMonth();
        //System.out.println(date.getMonthValue());

        String month = switch (GrammaCase.charAt(0)) {
            case 'm' -> monthsMianownik[date.getMonthValue() - 1];
            case 'd' -> monthsDopelniacz[date.getMonthValue() - 1];
            default -> "Brak Daty";
        };

        if (Objects.equals(Arrangement, "MM-yyyy")) {
            return month + " " + year;
        }

        return day + " " + month + " " + year;
    }
}
