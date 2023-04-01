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
        context.log.info(s"Success: Value of Key: $key is now $value")
        Behaviors.stopped
      case FailureGet(key) =>
        context.log.info(s"Failure: No value found for Key: $key")
        Behaviors.stopped
      case SuccessGet(key, value) =>
        context.log.info(s"At Key: $key, Value is: $value")
        Behaviors.stopped
    }

  }
}
