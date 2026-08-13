package com.draxlmaier.leavehub.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.HashSet;
import java.util.Set;

/**
 * Calculeaza numarul de zile lucratoare dintr-un interval, excluzand weekend-urile
 * si zilele libere legale din Romania (fixe + Paste/Rusalii, calculate pe baza
 * algoritmului Meeus pentru Pastele ortodox).
 */
public final class WorkingDaysCalculator {

    private static final Set<MonthDay> FIXED_HOLIDAYS = Set.of(
            MonthDay.of(1, 1),   // Anul Nou
            MonthDay.of(1, 2),   // Anul Nou
            MonthDay.of(1, 24),  // Unirea Principatelor Romane
            MonthDay.of(5, 1),   // Ziua Muncii
            MonthDay.of(6, 1),   // Ziua Copilului
            MonthDay.of(8, 15),  // Adormirea Maicii Domnului
            MonthDay.of(11, 30), // Sf. Andrei
            MonthDay.of(12, 1),  // Ziua Nationala
            MonthDay.of(12, 25), // Craciun
            MonthDay.of(12, 26)  // Craciun
    );

    private WorkingDaysCalculator() {
    }

    public static int calculate(LocalDate start, LocalDate end) {
        if (start == null || end == null || end.isBefore(start)) {
            throw new IllegalArgumentException("Interval de date invalid.");
        }

        Set<LocalDate> holidays = legalHolidaysBetween(start.getYear(), end.getYear());

        int workingDays = 0;
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            boolean weekend = date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
            if (!weekend && !holidays.contains(date)) {
                workingDays++;
            }
        }
        return workingDays;
    }

    private static Set<LocalDate> legalHolidaysBetween(int startYear, int endYear) {
        Set<LocalDate> holidays = new HashSet<>();
        for (int year = startYear; year <= endYear; year++) {
            for (MonthDay md : FIXED_HOLIDAYS) {
                holidays.add(md.atYear(year));
            }
            LocalDate orthodoxEaster = orthodoxEaster(year);
            holidays.add(orthodoxEaster.minusDays(2)); // Vinerea Mare (optional, tratata ca libera)
            holidays.add(orthodoxEaster);               // Paste (duminica, oricum weekend)
            holidays.add(orthodoxEaster.plusDays(1));    // A doua zi de Paste
            holidays.add(orthodoxEaster.plusDays(49));   // Rusalii (duminica, oricum weekend)
            holidays.add(orthodoxEaster.plusDays(50));   // A doua zi de Rusalii
        }
        return holidays;
    }

    /** Algoritmul Meeus pentru calculul datei Pastelui ortodox (calendar Gregorian). */
    private static LocalDate orthodoxEaster(int year) {
        int a = year % 4;
        int b = year % 7;
        int c = year % 19;
        int d = (19 * c + 15) % 30;
        int e = (2 * a + 4 * b - d + 34) % 7;
        int month = (d + e + 114) / 31;
        int day = ((d + e + 114) % 31) + 1;

        LocalDate julianDate = LocalDate.of(year, month, day);
        // Conversie din calendarul Iulian in Gregorian (offset pentru secolele 1900-2099: 13 zile)
        int offset = julianToGregorianOffset(year);
        return julianDate.plusDays(offset);
    }

    private static int julianToGregorianOffset(int year) {
        if (year >= 1900 && year <= 2099) {
            return 13;
        }
        if (year >= 2100 && year <= 2199) {
            return 14;
        }
        return 13; // aproximare rezonabila in afara intervalului uzual
    }
}
