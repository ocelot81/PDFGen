package org.pdfgen.Utils;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LocalDateOptions {
    public static final String DATE_FORMAT_MONTH_YEAR = "MM-yyyy";
    public static final String DATE_FORMAT_DAY_MONTH_YEAR = "dd-MM-yyyy";

    public String formatting;
    public String spelled_out;
    public String gramma_case;
}
