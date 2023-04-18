package Actors

import Actors.Printer.Result
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

object Printer {

sealed trait Result
  case class SetResult(key: Seq[Byte], success: Boolean) extends Result
  case class GetResult(key: Seq[Byte], value: Option[Seq[Byte]]) extends Result
  case class PrintCount(count: Int) extends Result

  def apply(): Behavior[Result] =
    Behaviors.setup {
      context => new Printer(context)
    }

}

class Printer(context: ActorContext[Result]) extends AbstractBehavior[Result](context){
  import Printer._
  override def onMessage(msg: Result): Behavior[Result] = {
    msg match{
      case SetResult(key, success) =>
        if (success) {
          context.log.info(s"Set Successful: Key: ${new String(key.toArray)} successfully set")
        } else {
          context.log.info(s"Set Failed: Key: ${new String(key.toArray)} failed to set")
        }
        Behaviors.stopped
      case GetResult(key, Some(value)) =>
        context.log.info(s"Get Successful: Key: ${new String(key.toArray)}, Value: ${new String(value.toArray)}")
        Behaviors.stopped
      case GetResult(key, None) =>
        context.log.info(s"Get Failed: Key: ${new String(key.toArray)} not found")
        Behaviors.stopped
      case PrintCount(count) =>
        context.log.info(s"CountResult: Total keys in the store: $count")
        Behaviors.stopped
    }
  }
}
