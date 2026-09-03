package org.pdfgen.factory;

import org.pdfgen.utils.EnglishMonthDictionary;
import org.pdfgen.utils.LocalDateOptions;
import org.pdfgen.utils.PolishMonthDictionary;
import java.util.Objects;

public class DateStringFactory {

    public static String create(LocalDateOptions dateConfig, String locale) {
        String result = "";

        locale = locale.toUpperCase();

        if (Objects.equals(locale, "PL")) {
            PolishMonthDictionary pmd = new PolishMonthDictionary();

            result = pmd.create(dateConfig);
        } else if (Objects.equals(locale, "EN")) {
            EnglishMonthDictionary emd = new EnglishMonthDictionary();
            result = emd.create(dateConfig);
        }

        return result;
    }
}
