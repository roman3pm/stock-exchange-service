package ru.roman3pm.stock.exchange.service.datatypes

import java.time.LocalDateTime

/**
  * Класс для агрегированных данных
  */
case class Data(ticker: String, timestamp: LocalDateTime, open: Double, high: Double, low: Double, close: Double, volume: Int)
