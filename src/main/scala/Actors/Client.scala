package Actors

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

import scala.collection.immutable
import scala.concurrent.duration.DurationInt

object Client {
  sealed trait Command
  case class Set(key: String, value: String) extends Command
  case class Get(key: String) extends Command
  case class BatchSet(pairs: immutable.Seq[(String, String)]) extends Command
  case object Finished extends Command
  private case class StoreUpdate(store: ActorRef[Store.Command]) extends Command

  val ClientServiceKey: ServiceKey[Command] = ServiceKey[Client.Command]("ClientService")

  def apply(): Behavior[Command] =
    Behaviors.setup { context =>
      context.system.receptionist ! Receptionist.Register(ClientServiceKey, context.self)
      new Client(None, context)
      }
}

class Client(store: Option[ActorRef[Store.Command]], context: ActorContext[Client.Command]) extends AbstractBehavior[Client.Command](context){
  import Client._

  val delay = 2.seconds

  def findStore(): Unit = {
    context.spawnAnonymous(Behaviors.setup[Receptionist.Listing] { ctx =>
      ctx.system.receptionist ! Receptionist.Find(Store.StoreServiceKey, ctx.self)
      Behaviors.receiveMessage {
        case Store.StoreServiceKey.Listing(storeRefs) =>
          if (storeRefs.nonEmpty)
            context.scheduleOnce(delay, context.self, StoreUpdate(storeRefs.headOption.orNull))
          Behaviors.same
      }
    })
  }
  override def onMessage(msg: Command): Behavior[Command] = {

    msg match {
      case StoreUpdate(newStore) =>
        new Client(Some(newStore), context)

      case _ if store.nonEmpty =>
        msg match {
          case BatchSet(pairs) =>
            val printer = context.spawnAnonymous(Printer())
            store.get ! Store.SetBatch(printer, pairs)
            Behaviors.same
          case Set(key, value) =>
            val printer = context.spawnAnonymous(Printer())
            store.get ! Store.Set(printer, key.getBytes, value.getBytes)
            Behaviors.same
          case Get(key) =>
            val printer = context.spawnAnonymous(Printer())
            store.get ! Store.Get(printer, key.getBytes)
            Behaviors.same
          case Finished =>
            val printer = context.spawnAnonymous(Printer())
            store.get ! Store.Count(printer)
            Behaviors.stopped
        }

      case _ =>
        msg match {
          case BatchSet(pairs) =>
            context.self ! BatchSet(pairs)
            findStore()
            Behaviors.same
          case Set(key, value) =>
            context.self ! Set(key, value)
            findStore()
            Behaviors.same
          case Get(key) =>
            context.self ! Get(key)
            findStore()
        }
        Behaviors.same
    }
  }
}
