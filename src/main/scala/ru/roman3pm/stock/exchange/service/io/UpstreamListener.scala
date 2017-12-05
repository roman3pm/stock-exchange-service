package ru.roman3pm.stock.exchange.service.io

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.{IO, Tcp}
import ru.roman3pm.stock.exchange.service.datatypes.ByteData

object UpstreamListener {
  def props(remote: InetSocketAddress, candles: ActorRef) =
    Props(classOf[UpstreamListener], remote, candles)
}

class UpstreamListener(upstream: InetSocketAddress, candles: ActorRef) extends Actor with ActorLogging {

  import akka.io.Tcp._
  import context.system

  IO(Tcp) ! Connect(upstream)

  def receive: Receive = {
    case CommandFailed(_: Connect) =>
      log.warning("connect failed")
      context stop self

    case Connected(remote, local) =>
      sender() ! Register(self)
      context become {
        case Received(data) =>
          ByteData(data) match {
            case Some(bd) => candles ! bd
            case None => log.warning("wrong upstream data")
          }
        case _: ConnectionClosed =>
          log.warning("connection closed")
          context stop self
      }
  }
}
