package ru.roman3pm.stock.exchange.service.state

import akka.actor.{Actor, ActorLogging}

import scala.collection.mutable

/**
  * Актор-стейт для хранения данных
  */
class State extends Actor with ActorLogging {

  import StateEvents._

  // Преобразуем очередь в очередь размером 10
  implicit class FiniteQueue[A](q: mutable.Queue[A]) {
    def enqueueFinite(elem: A, maxSize: Int): Unit = {
      q.enqueue(elem)
      while (q.size > maxSize) {
        q.dequeue // Все старое удаляем
      }
    }
  }

  // Очередь в которой лежат данные в JSON формате поминутно
  val state: mutable.Queue[String] = mutable.Queue[String]()

  def receive: Receive = {
    case data: String =>
      log.info("new data created")
      state.enqueueFinite(data, 10)
    case GetAll =>
      log.info("request all")
      if (state.nonEmpty) sender() ! state.mkString("\n")
    case GetLast =>
      log.info("request last")
      state.lastOption match {
        case Some(last) => sender() ! last
        case None =>
      }
  }
}

/**
  * События для доступа к актору-стейту
  */
object StateEvents {

  case object GetAll

  case object GetLast

}