package Actors

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

import scala.io.Source
import scala.io.Source.fromFile

object FileReader {
  sealed trait Message
  case class File(filename:String, client: ActorRef[Client.Command], batchSize: Int) extends Message

  def apply(): Behavior[Message] = Behaviors.setup { context =>
    new FileReader(context)
  }


}

class FileReader(context: ActorContext[FileReader.Message]) extends AbstractBehavior[FileReader.Message](context) {
  import FileReader._
  import Client._

  override def onMessage(msg: Message): Behavior[Message] = {
    msg match {
      case File(filename, client, batchSize) =>
        val source = Source.fromFile(filename)
        val lines = source.getLines().toSeq
        lines.grouped(batchSize).foreach { batch =>
          val pairs = batch.flatMap { line =>
            val fields = line.split(",")
            if (fields.length >= 2) {
              val key = fields(0).trim
              val value = fields(1).trim
              Some((key, value))
            } else None
          }
          client ! Client.BatchSet(pairs)
        }
        source.close()
        println(s"Finished reading file: $filename")
        client ! Client.Finished
        this
    }
  }
}
