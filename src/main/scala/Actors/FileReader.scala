package Actors

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

import scala.io.Source

object FileReader {
  sealed trait Message
  case class File(filename:String, batchSize: Int) extends Message
  private case class ClientUpdate(store: ActorRef[Client.Command]) extends Message

  val ReaderServiceKey: ServiceKey[FileReader.Message] = ServiceKey[FileReader.Message]("ReaderService")
  def apply(): Behavior[Message] =
    Behaviors.setup { context =>
      context.system.receptionist ! Receptionist.Register(ReaderServiceKey, context.self)
    new FileReader(None,context)
  }
}

class FileReader(client: Option[ActorRef[Client.Command]],context: ActorContext[FileReader.Message]) extends AbstractBehavior[FileReader.Message](context) {
  import FileReader._

  if (client.isEmpty) {
    context.spawnAnonymous(Behaviors.setup[Receptionist.Listing] { ctx =>
      ctx.system.receptionist ! Receptionist.Find(Client.ClientServiceKey, ctx.self)
      Behaviors.receiveMessage {
        case Client.ClientServiceKey.Listing(clientRefs) =>
          context.self ! ClientUpdate(clientRefs.headOption.orNull)
          Behaviors.same
      }
    })
  }

  override def onMessage(msg: Message): Behavior[Message] = {
    msg match {
      case ClientUpdate(newClient) =>
        new FileReader(Some(newClient), context)
      case _ if client.isDefined =>
        msg match {
          case File(filename, batchSize) =>
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
              client.get ! Client.BatchSet(pairs)
            }
            source.close()
            println(s"Finished reading file: $filename")
            client.get ! Client.Finished
            this
        }
      case File(filename, batchSize) =>
        context.self ! File(filename, batchSize)
        this
    }
  }
}
