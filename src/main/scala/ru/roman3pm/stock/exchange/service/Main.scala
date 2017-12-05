package ru.roman3pm.stock.exchange.service

import java.net.InetSocketAddress

import akka.actor.Status.Success
import akka.actor._
import akka.stream._
import akka.stream.scaladsl._
import ru.roman3pm.stock.exchange.service.datatypes.{ByteData, Data}
import ru.roman3pm.stock.exchange.service.helpers.DurationHelper._
import ru.roman3pm.stock.exchange.service.helpers.JsonHelper._
import ru.roman3pm.stock.exchange.service.io.{Server, UpstreamListener}
import ru.roman3pm.stock.exchange.service.state.State

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Главный класс
  */
object Main extends App {
  println("Syncing with system clock...")
  Thread.sleep(offsetToNextMinute.toMillis) // ¯\_(ツ)_/¯

  implicit val system: ActorSystem = ActorSystem("stock-exchange-service")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val state = system.actorOf(Props(classOf[State]), "state") // Актор-хранитель агрегированных данных
  val server = system.actorOf(Props(classOf[Server], state), "server") // Сервер для клиентских подключений

  implicit val ordering: Ordering[ByteData] = (x: ByteData, y: ByteData) => {
    if (x.price < y.price) -1 else 1
  }

  // Стрим для обработки входящего потока, делит на окна по 1 минуте и осылает в актор, ответсвенный за стейт
  val stream = Source.actorRef[ByteData](Int.MaxValue, OverflowStrategy.fail)
    .groupedWithin(Int.MaxValue, 60 seconds)
    .via(
      Flow[Seq[ByteData]]
        .map[String](f => f.toIndexedSeq.groupBy(_.ticker).map { case (_, seq) =>
        wrapToJson(Data(seq.head.ticker, seq.head.timestamp, seq.head.price, seq.max.price, seq.min.price, seq.last.price, seq.foldLeft(0)(_ + _.size)))
      }.mkString("\n")))
    .to(Sink.actorRef[String](state, Success))
    .run()

  // Слушатель входящего потока, отсылает полученные данные в стрим
  val upstreamListener = system.actorOf(UpstreamListener.props(new InetSocketAddress("localhost", 5555), stream), "upstreamListener")

  Await.result(system.whenTerminated, Duration.Inf)
}
