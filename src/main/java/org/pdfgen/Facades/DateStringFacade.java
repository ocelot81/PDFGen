package org.pdfgen.Facades;

import org.pdfgen.Utils.EnglishMonthDictionary;
import org.pdfgen.Utils.LocalDateOptions;
import org.pdfgen.Utils.PolishMonthDictionary;

import java.util.Objects;

public class DateStringFacade {

    public static String create(LocalDateOptions dateConfig, String locale) {
        String result = "";

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
