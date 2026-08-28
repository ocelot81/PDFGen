
package org.pdfgen.utils;

import java.util.Objects;

public class PolishMonthDictionary extends AbstractMonthDictionary {

    private final String[] monthsDopelniacz = {
            "Stycznia", "Lutego", "Marca",
            "Kwietnia", "Maja", "Czerwca",
            "Lipca", "Sierpnia", "Września",
            "Października", "Listopada", "Grudnia",
    };

    private final String[] monthsMianownik = {
            "Styczeń", "Luty", "Marzec",
            "Kwiecień", "Maj", "Czerwiec",
            "Lipiec", "Sierpień", "Wrześnień",
            "Październik", "Listopad", "Grudzień",
    };


    @Override
    protected String getMonthsByMianownik(int index) {
        return monthsMianownik[index];
    }

    @Override
    protected String getMonthsByDopelniacz(int index) {
        return monthsDopelniacz[index];
    }

    @Override
    protected String getLocaleFormatting(String formatting, int day, String month, int year) {
        if (Objects.equals(formatting, LocalDateOptions.DATE_FORMAT_MONTH_YEAR)) {
            return month + " " + year;
        }
        return day + " " + month + " " + year;
    }
}
