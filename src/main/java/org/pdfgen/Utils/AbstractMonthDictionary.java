package org.pdfgen.Utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public abstract class AbstractMonthDictionary {
    /**
     * Creates a string representation of date with provided configuration.
     *
     * @param options Options of the representation (Arrangement, spelled_out, grammatical case)
     */
    public String create(LocalDateOptions options) {
        LocalDate date = LocalDate.now();

        String formatting = options.formatting;
        String spelled_out = options.spelled_out;
        String grammaCase = options.gramma_case;

        if (!Boolean.parseBoolean(spelled_out)) {
            return date.format(DateTimeFormatter.ofPattern(formatting));
        }

        int year = date.getYear();
        int day = date.getDayOfMonth();

        String month = switch (grammaCase.charAt(0)) {
            case 'm' -> getMonthsByMianownik(date.getMonthValue() - 1);
            case 'd' -> getMonthsByDopelniacz(date.getMonthValue() - 1);
            default -> "Brak Daty";
        };

        return getLocaleFormatting(formatting, day, month, year);
    }

    protected abstract String getMonthsByMianownik(int index);
    protected abstract String getMonthsByDopelniacz(int index);
    protected abstract String getLocaleFormatting(String format, int day, String month, int year);

}
