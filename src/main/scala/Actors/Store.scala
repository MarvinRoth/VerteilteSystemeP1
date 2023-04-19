package Actors
import Actors.Printer._
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

import scala.collection.mutable

object Store {

  sealed trait Command
  case class Get(replyTo: ActorRef[Result], key: Seq[Byte]) extends Command
  case class Set(replyTo: ActorRef[Result], key: Seq[Byte], value: Seq[Byte]) extends Command
  case class Count(replyTo: ActorRef[Result]) extends Command

  val StoreServiceKey: ServiceKey[Command] = ServiceKey[Store.Command]("StoreService")

  def apply(): Behavior[Command] =
    Behaviors.setup{ context =>
    context.system.receptionist ! Receptionist.Register(StoreServiceKey, context.self)
    new Store(context)
  }
}
class Store(context: ActorContext[Store.Command]) extends AbstractBehavior[Store.Command](context) {
  import Store._

  private val storage = mutable.Map[Seq[Byte], Seq[Byte]]()
  override def onMessage(msg: Store.Command): Behavior[Store.Command] = {
    msg match {
      case Get(replyTo, key) =>
          replyTo ! Printer.GetResult(key, storage.get(key))
        this
      case Set(replyTo, key, value) =>
        storage.put(key, value)
        replyTo ! SetResult(key, success = true)
        this
      case Count(replyTo) =>
        replyTo ! PrintCount(storage.size)
        this
    }
  }
}
