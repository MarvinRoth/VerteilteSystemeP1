package Actors

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

import scala.io.Source.fromFile

object FileReader {
  sealed trait Message
  case class File(filename:String, client: ActorRef[Client.Command]) extends Message

  def apply(): Behavior[Message] = Behaviors.setup { context =>
    new FileReader(context)
  }


}

class FileReader(context: ActorContext[FileReader.Message]) extends AbstractBehavior[FileReader.Message](context) {
  import FileReader._
  import Client._

  override def onMessage(msg: Message): Behavior[Message] = {
    msg match {
      case File(filename, client) =>
        val lines = fromFile(filename).getLines()
        lines.foreach(line => {
          val fields = line.split(",")
          val key = fields(0)
          val value = fields(1)
          client ! Set(key, value)
        })
        Behaviors.same
    }
  }
}
