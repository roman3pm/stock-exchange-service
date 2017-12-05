import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.io.{IO, Tcp}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Течтовый клиент для тестирования
  */
class Client(socket: InetSocketAddress) extends Actor with ActorLogging {

  import akka.io.Tcp._
  import context.system

  IO(Tcp) ! Connect(socket)

  def receive: Receive = {
    case CommandFailed(_: Connect) =>
      log.warning("connect failed")
      context stop self

    case Connected(remote, local) =>
      log.info(s"connected to $remote")
      sender() ! Register(self)
      context become {
        case Received(data) =>
          log.info(data.decodeString("UTF-8"))
        case _: ConnectionClosed =>
          log.warning("connection closed")
          context stop self
      }
  }
}


object TestClient extends App {
  implicit val system: ActorSystem = ActorSystem("test-client")
  val client: ActorRef = system.actorOf(Props(classOf[Client], new InetSocketAddress("localhost", 6666)), "client")

  Await.result(system.whenTerminated, Duration.Inf)
}
