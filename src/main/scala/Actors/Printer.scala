package Actors

import Actors.Printer.{Result, SuccessSet}
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

object Printer {

sealed trait Result
  case class SuccessSet(key: Seq[Byte], value: Seq[Byte]) extends Result
  case class FailureGet(key: Seq[Byte]) extends Result
  case class SuccessGet(key: Seq[Byte], value: Seq[Byte]) extends Result

  def apply(): Behavior[Result] =
    Behaviors.setup {
      context => new Printer(context)
    }

}

class Printer(context: ActorContext[Result]) extends AbstractBehavior[Result](context){
  import Printer._
  override def onMessage(msg: Result): Behavior[Result] = {
    msg match{
      case SuccessSet(key,value) =>
        val keyString = new String(key.toArray)
        val valueString = new String(value.toArray)
        context.log.info(s"Success: Value of Key: $keyString is now $valueString")
        Behaviors.stopped
      case FailureGet(key) =>
        val keyString = new String(key.toArray)
        context.log.info(s"Failure: No value found for Key: $keyString")
        Behaviors.stopped
      case SuccessGet(key, value) =>
        val keyString = new String(key.toArray)
        val valueString = new String(value.toArray)
        context.log.info(s"At Key: $keyString, Value is: $valueString")
        Behaviors.stopped
    }
  }
}
