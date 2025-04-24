package com.epam.trainings.gen_ai_task_6.plugins;

import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.util.StringUtils.parseLocale;

@Slf4j
public class TimePlugin {
    public static final String DAY_MONTH_DAY_YEAR = "EEEE, MMMM d, yyyy";

    /**
     * Get the current date and time for the system default timezone.
     *
     * @return a ZonedDateTime object with the current date and time.
     */
    public ZonedDateTime now() {
        log.info("Getting current date and time...");
        return ZonedDateTime.now(ZoneId.systemDefault());
    }

    /**
     * Get the current date.
     *
     * <p>Example: {{time.date}} => Sunday, January 12, 2025
     *
     * @return The current date.
     */
    @DefineKernelFunction(
            name = "date",
            description = "Get the current date")
    public String date(
            @KernelFunctionParameter(
                    name = "locale",
                    description = "Locale to use when formatting the date",
                    required = false)
            String locale) {
        log.info("Getting current date...with locale: {}", locale);
        return DateTimeFormatter.ofPattern(DAY_MONTH_DAY_YEAR)
                .withLocale(parseLocale(locale))
                .format(now());
    }

    /**
     * Get the current time.
     *
     * <p>Example: {{time.time}} => 9:15:00 AM
     *
     * @return The current time.
     */
    @DefineKernelFunction(
            name = "time",
            description = "Get the current time")
    public String time(
            @KernelFunctionParameter(
                    name = "locale",
                    description = "Locale to use when formatting the date",
                    required = false)
            String locale) {
        log.info("Getting current time...");
        // Example: 09:15:07 PM
        log.info("Locale: {}", locale);
        return DateTimeFormatter.ofPattern("hh:mm:ss a")
                .withLocale(parseLocale(locale))
                .format(now());
    }
}
