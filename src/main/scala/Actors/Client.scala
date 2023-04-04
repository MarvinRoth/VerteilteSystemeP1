package Actors

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

object Client {
  sealed trait Command
  case class Set(key: String, value: String) extends Command
  case class Get(key: String) extends Command

  def apply(store: ActorRef[Store.Command]): Behavior[Command] =
    Behaviors.setup {
      context => new Client(store, context)
    }
}

class Client(store: ActorRef[Store.Command], context: ActorContext[Client.Command]) extends AbstractBehavior[Client.Command](context){
  import Client._

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case Set(key, value) =>
        val printer = context.spawnAnonymous(Printer())
        store ! Store.Set(printer, key.getBytes, value.getBytes)
        Behaviors.same
      case Get(key) =>
        val printer = context.spawnAnonymous(Printer())
        store ! Store.Get(printer, key.getBytes)
        Behaviors.same
    }
  }
}
