package Actors
import Actors.Printer._
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

import scala.collection.mutable

object Store {

  sealed trait Command
  case class Get(replyTo: ActorRef[Result], key: Seq[Byte]) extends Command
  case class Set(replyTo: ActorRef[Result], key: Seq[Byte], value: Seq[Byte]) extends Command

  def apply(): Behavior[Command] =
    Behaviors.setup{
    context => new Store(context)
  }
}
class Store(context: ActorContext[Store.Command]) extends AbstractBehavior[Store.Command](context) {
  import Store._

  private val storage = mutable.Map[Seq[Byte], Seq[Byte]]()
  override def onMessage(msg: Store.Command): Behavior[Store.Command] = {
    msg match {
      case Get(replyTo, key) =>
        val value = storage.get(key)
        if (value.isEmpty) {
          replyTo ! Printer.FailureGet(key)
        }
        else {
          replyTo ! Printer.SuccessGet(key, value.get)
        }
        Behaviors.same
      case Set(replyTo, key, value) =>
        storage.put(key, value)
        replyTo ! Printer.SuccessSet(key, value)
        Behaviors.same
    }
  }
}
