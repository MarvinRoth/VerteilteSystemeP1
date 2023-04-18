package Actors

import Actors.Client.Command
import Actors.ResultActor.{GetResult, SetResult}
import Actors.Store.StoreServiceKey
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.AskPattern._

import scala.concurrent.duration._
import scala.util.{Failure, Success}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, Scheduler}
import akka.util.Timeout

import scala.concurrent.ExecutionContext

object Client {
  sealed trait Command
  case class Get(key: String) extends Command
  case class Set(key: String, value: String) extends Command
  case class BatchSet(pairs: Seq[(String, String)]) extends Command

  case object finished extends Command

  def apply(store: ActorRef[Store.Command]): Behavior[Command] = {
    Behaviors.setup(context => new Client(context, store))
  }
}

class Client(context: ActorContext[Client.Command], store: ActorRef[Store.Command])
  extends AbstractBehavior[Client.Command](context) {
  import Client._

  private def toBytes(s: String): Seq[Byte] = s.getBytes("UTF-8").toSeq

  private def fromBytes(bytes: Seq[Byte]): String = new String(bytes.toArray, "UTF-8")

  override def onMessage(msg: Command): Behavior[Command] = {
    val resultActor = context.spawnAnonymous(ResultActor())
    msg match {

      case BatchSet(pairs) =>
        pairs.foreach { case (key, value) =>
          store ! Store.Set(resultActor, toBytes(key), toBytes(value))
        }
        this

      case Get(key) =>
        store ! Store.Get(resultActor, toBytes(key))
        this

      case Set(key, value) =>
        store ! Store.Set(resultActor, toBytes(key), toBytes(value))
        this

      case finished =>
        store ! Store.Count(resultActor)
        Behaviors.stopped
    }
  }
}
