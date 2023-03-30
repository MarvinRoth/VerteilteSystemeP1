import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.util.LineNumbers.Result

import scala.collection.mutable.Map

object Store{
  import Client._

  val storage: Map[Seq[Byte], Seq[Byte]] = Map.empty

  sealed trait Command
    case class Get(replyTo: ActorRef[Result], key: Seq[Byte]) extends Command
    case class Set(replyTo: ActorRef[Result], key: Seq[Byte], value: Seq[Byte]) extends Command

  def apply(): Behavior[Command] = {
    Behaviors.receiveMessage { (message) =>
      message match {
        case Get(replyTo, key) =>

          Behaviors.same
        case Set(replyTo, key, value) =>

          Behaviors.same
    }
  }
}

object Guardian {
  import Store._
  import Client._
  def apply(): Behavior[NotUsed] = {
    Behaviors.setup {
      context =>
      val store = context.spawn(Store(), "store")
      val client = context.spawn(Client(), "client")
      client ! Client.Produce(Seq[Byte](1,2,3), Seq[Byte](4,5,6))
      client ! Client.Receive(Seq[Byte](1,2,3))
      Behaviors.same
    }

  }
}

object Client {

  import Store._
  sealed trait Result
    case class Receive(key:Seq[Byte]) extends Result
    case class Produce(key: Seq[Byte], value: Seq[Byte]) extends Result

    def apply(): Behavior[Command] = {
      Behaviors.receiveMessage { (context, message) =>
        message match {
        case Receive(key) =>
           ! Receive(key)
          Behaviors.same
        case Produce(key, value) =>
           ! Produce(key, value)
          Behaviors.same
      }
    }

  }
}

object Main {
  def main(args: Array[String]): Unit = {

    val system = ActorSystem(Guardian(), "hfu")

  }
}