package kspt.bank.controllers

import kspt.bank.BankVaultCoreApplication
import kspt.bank.services.WebTimeService
import java.lang.Exception
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class WebTimeController : ErrorHandlingController() {
    private val timeService by lazy {
        BankVaultCoreApplication.getApplicationContext().getBean(WebTimeService::class.java)
    }

    fun getFormattedDateTime(zoneName: String) = try {
        timeService.getCurrentTime(ZoneId.of(zoneName))
                ?.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))
    } catch (e: Exception) {
        logger.error("Failed to get time from {}: {}", WebTimeService.ROOT_URL, e.message)
        ""
    }
}