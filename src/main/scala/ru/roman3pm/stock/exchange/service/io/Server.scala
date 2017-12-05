package ru.roman3pm.stock.exchange.service.io

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, Props}
import akka.io.{IO, Tcp}
import akka.pattern.ask
import akka.util.{ByteString, Timeout}

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

/**
  * Актор-сервер для клиентских подключений
  */
class Server(state: ActorRef) extends Actor with ActorLogging {

  import akka.io.Tcp._
  import context.system
  import ru.roman3pm.stock.exchange.service.state.StateEvents._

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 6666))

  def receive: Receive = {
    case b@Bound(localAddress) =>
      log.info("listening on port {}", localAddress.getPort)
      context.parent ! b

    case CommandFailed(Bind(_, local, _, _, _)) =>
      log.warning(s"cannot bind to [$local]")
      context stop self

    case Connected(remote, local) =>
      implicit val ec: ExecutionContextExecutor = context.dispatcher
      implicit val timeout: Timeout = Timeout(5 seconds)

      log.info("received connection from {}", remote)
      val connection = sender()
      val handler = context.actorOf(Props(classOf[ClientConnectionHandler], connection, state))
      // При успешном подключении клиента идем в актор-стейт за данным за последние 10 минут и отсылаем
      (state ? GetAll).mapTo[String].foreach(all => connection ! Write(ByteString.fromString(all, "UTF-8")))
      connection ! Register(handler)
  }
}

/**
  * Актор-хэндлер клиентских подключений
  */
class ClientConnectionHandler(connection: ActorRef, state: ActorRef) extends Actor with ActorLogging {

  import Tcp._
  import context.dispatcher
  import ru.roman3pm.stock.exchange.service.helpers.DurationHelper._
  import ru.roman3pm.stock.exchange.service.state.StateEvents._

  case object Tick

  context watch connection

  // Шедулер для отправки данных за последнюю минуту
  val scheduler: Cancellable = context.system.scheduler.schedule(offsetToNextMinute, 60 seconds, self, Tick)

  override def postStop(): Unit = scheduler.cancel()

  def receive: Receive = {
    case Tick =>
      implicit val timeout: Timeout = Timeout(5 seconds)

      log.info("tick")
      // По шедулеру идем за данными за последнюю минуту в стейт и отправляем клиенту
      (state ? GetLast).mapTo[String].foreach(last => connection ! Write(ByteString.fromString(last, "UTF-8")))
    case PeerClosed => context stop self
  }
}

