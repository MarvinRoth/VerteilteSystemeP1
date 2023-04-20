import Actors.Guardian
import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.util.LineNumbers.Result

import scala.collection.mutable.Map
object Main {
  def main(args: Array[String]): Unit = {

    val configuration = utils.Utils.getConfig(25253)
    val system = ActorSystem(Guardian(), "hfu", configuration)

    scala.io.StdIn.readLine()
    println("Press ENTER to exit the system")
    scala.io.StdIn.readLine()
    system.terminate()

  }
}