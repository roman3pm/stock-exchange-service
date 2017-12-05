package ru.roman3pm.stock.exchange.service.datatypes

import java.time.{LocalDateTime, ZoneId}
import java.util.Date

import akka.util.ByteString

import scala.util.Try

/**
  * Класс для маппинга входящих сообщений
  */
case class ByteData(timestamp: LocalDateTime, ticker: String, price: Double, size: Int)

object ByteData {
  def apply(bs: ByteString): Option[ByteData] = Try {
    val bb = bs.toByteBuffer
    val date = new Date(bb.getLong(2))
    val timestamp = LocalDateTime.ofInstant(date.toInstant, ZoneId.systemDefault)
    val tickerLen = bb.getShort(10)
    val ticker = bs.slice(12, 12 + tickerLen).decodeString("UTF-8")
    val price = bb.getDouble(12 + tickerLen)
    val size = bb.getInt(12 + tickerLen + 8)
    ByteData(timestamp, ticker, price, size)
  }.toOption
}
