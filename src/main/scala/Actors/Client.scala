package Actors

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

import scala.collection.Seq

object Client {
  sealed trait Command extends utils.Serializable
  case class Set(key: String, value: String) extends Command
  case class Get(key: String) extends Command
  case class BatchSet(pairs: Seq[(String, String)]) extends Command
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

  if (store.isEmpty) {
    context.spawnAnonymous(Behaviors.setup[Receptionist.Listing] { ctx =>
      ctx.system.receptionist ! Receptionist.Find(Store.StoreServiceKey, ctx.self)
      Behaviors.receiveMessage {
        case Store.StoreServiceKey.Listing(storeRefs) =>
          context.self ! StoreUpdate(storeRefs.headOption.orNull)
          Behaviors.same
      }
    })
  }

  override def onMessage(msg: Command): Behavior[Command] = {
    val printer = context.spawnAnonymous(Printer())
    msg match {
      case StoreUpdate(newStore) =>
        new Client(Some(newStore), context)

      case _ if store.isDefined =>
        msg match {
          case BatchSet(pairs) =>
            pairs.foreach(pair => store.get ! Store.Set(printer, pair._1.getBytes, pair._2.getBytes))
            Behaviors.same
          case Set(key, value) =>
            store.get ! Store.Set(printer, key.getBytes, value.getBytes)
            Behaviors.same
          case Get(key) =>
            store.get ! Store.Get(printer, key.getBytes)
            Behaviors.same
          case Finished =>
            store.get ! Store.Count(printer)
            Behaviors.stopped
        }

      case _ =>
        msg match {
          case BatchSet(pairs) =>
            context.self ! BatchSet(pairs)
            Behaviors.same
          case Set(key, value) =>
            context.self ! Set(key, value)
            Behaviors.same
          case Get(key) =>
            context.self ! Get(key)
            Behaviors.same
        }
        Behaviors.same
    }
  }
}
