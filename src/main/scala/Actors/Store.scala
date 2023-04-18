package Actors
import Actors.ResultActor.Result
import akka.actor.typed.scaladsl.Behaviors.Receive
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

import scala.collection.mutable

object Store {
  import ResultActor._
  // Definieren Sie das Protokoll für Store
  sealed trait Command
  case class Get(replyTo: ActorRef[Result], key: Seq[Byte]) extends Command
  case class Set(replyTo: ActorRef[Result], key: Seq[Byte], value: Seq[Byte]) extends Command
  case class Count(replyTo: ActorRef[Result]) extends Command

  val StoreServiceKey: ServiceKey[Command] = ServiceKey[Command]("store-service")

  def apply(): Behavior[Command] = {
    Behaviors.setup(context => new Store(context))
  }
}

class Store(context: ActorContext[Store.Command]) extends AbstractBehavior[Store.Command](context) {
  import Store._
  import ResultActor._

  private val store = mutable.Map.empty[Seq[Byte], Seq[Byte]]

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case Get(replyTo, key) =>
        val value = store.get(key)
        replyTo ! GetResult(key, value)
        this

      case Set(replyTo, key, value) =>
        store.put(key, value)
        replyTo ! SetResult(key, success = true)
        this

      case Count(replyTo) =>
        replyTo ! CountResult(store.size)
        this
    }
  }
}
