package com.nhd.processor.re.services;

import lombok.experimental.UtilityClass;

import org.mvel2.MVEL;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@UtilityClass
public class RuleFunctions {

    private static final DateTimeFormatter DF_DDMMMYYYY =
            DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);

    public static <T> List<T> topBy(List<T> items, String field, int count) {
        List<T> result = new ArrayList<>();
        if (count <= 0) {
            return result;
        }

        for (T item : items) {
            if (item != null && MVEL.getProperty(field, item) != null) {
                result.add(item);
            }
        }

        Comparator<T> byValue = Comparator.comparing(
                item -> new BigDecimal(MVEL.getProperty(field, item).toString()));
        result.sort(byValue.reversed());

        return new ArrayList<>(result.subList(0, Math.min(count, result.size())));
    }

    public static <T> Map<Object, T> mapBy(List<T> items, String field) {
        Map<Object, T> result = new LinkedHashMap<>();
        for (T item : items) {
            if (item != null) {
                Object key = MVEL.getProperty(field, item);
                if (key != null) {
                    result.put(key, item);
                }
            }
        }
        return result;
    }

    public static LocalDate parseDate_ddMonyyyy(String value) {
        return value == null ? null : LocalDate.parse(value, DF_DDMMMYYYY);
    }
}
