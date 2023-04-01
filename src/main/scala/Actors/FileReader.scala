package Actors

import akka.actor.typed.ActorRef

object FileReader {
  sealed trait Message
  case class File(filename:String, client: ActorRef[Client.Command]) extends Message

}
