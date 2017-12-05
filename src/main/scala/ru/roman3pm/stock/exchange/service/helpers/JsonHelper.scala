package ru.roman3pm.stock.exchange.service.helpers

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import org.json4s.native.Serialization.write
import org.json4s.{CustomSerializer, DefaultFormats, Formats, JString}


object JsonHelper {
  case object LDTSerializer extends CustomSerializer[LocalDateTime](format => ( {
    case JString(s) => LocalDateTime.parse(s)
  }, {
    case ldt: LocalDateTime =>
      val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:00'Z'")
      JString(ldt.format(formatter))
  }))

  implicit private val formats: Formats = DefaultFormats + LDTSerializer

  def wrapToJson[T <: AnyRef](t: T): String = write[T](t)
}
