package ru.roman3pm.stock.exchange.service.helpers

import java.time.{LocalDateTime, OffsetDateTime}

import scala.concurrent.duration._

/**
  * Объект-помощник для работы с Duration
  */
object DurationHelper {
  /**
    * Возващает Duration до следующей минуты
    */
  def offsetToNextMinute: FiniteDuration = {
    val now = LocalDateTime.now()
    val next = now.withSecond(0).withNano(0).plusMinutes(1)

    val s = now.toInstant(OffsetDateTime.now().getOffset).getEpochSecond
    val s1 = next.toInstant(OffsetDateTime.now().getOffset).getEpochSecond

    (s1 - s).seconds
  }
}
