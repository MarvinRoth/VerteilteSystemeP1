package Actors

import Actors.ResultActor.Result
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.Behavior

object ResultActor {
  sealed trait Result

  case class GetResult(key: Seq[Byte], value: Option[Seq[Byte]]) extends Result

  case class SetResult(key: Seq[Byte], success: Boolean) extends Result

  case class CountResult(count: Int) extends Result

  def apply(): Behavior[Result] =
    Behaviors.setup {
      context => new ResultActor(context)
    }
}

class ResultActor(context: ActorContext[Result]) extends AbstractBehavior[Result](context) {
  import ResultActor._
  override def onMessage(msg: Result): Behavior[Result] = {
    msg match{
      case GetResult(key, Some(value)) =>
        context.log.info(s"GetResult: Key: ${new String(key.toArray)}, Value: ${new String(value.toArray)}")
        Behaviors.stopped

      case GetResult(key, None) =>
        context.log.info(s"GetResult: Key: ${new String(key.toArray)} not found")
        Behaviors.stopped

      case SetResult(key, success) =>
        if (success) {
          context.log.info(s"SetResult: Key: ${new String(key.toArray)} successfully set")
        } else {
          context.log.info(s"SetResult: Key: ${new String(key.toArray)} setting failed")
        }
        Behaviors.stopped

      case CountResult(count) =>
        context.log.info(s"CountResult: Total keys in the store: $count")
        Behaviors.stopped
    }
  }
}
