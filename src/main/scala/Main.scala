import Actors.{Client, FileReader, Guardian, ResultActor, Store}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.ActorSystem
import akka.actor.typed.receptionist.Receptionist

object Main {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem.create(Guardian(), "main-sytem")
    println("Press ENTER to exit the system")
    scala.io.StdIn.readLine()

    system.terminate()
  }
}