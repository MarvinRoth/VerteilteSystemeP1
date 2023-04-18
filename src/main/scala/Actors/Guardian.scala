package Actors

import akka.NotUsed
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object Guardian {
  def apply(): Behavior[NotUsed] = Behaviors.setup { context =>
    val store = context.spawn(Store(), "the-store")
    val client1 = context.spawn(Client(store), "client1")
    val client2 = context.spawn(Client(store), "client2")

    client1 ! Client.Set("IT", "Italia")
    client2 ! Client.Get("IT")
    client1 ! Client.Get("DE")
    client1 ! Client.Get("IT")

    val reader = context.spawn(FileReader(), "reader")
    val filename = "D:\\Master\\Verteilte Systeme\\Aufgabe1\\src\\main\\trip_data_1000_000.csv"
    val batchSize = 10
    reader ! FileReader.File(filename, client1, batchSize)

    // Fügen Sie den neuen Count-Befehl hinzu
    val countResultActor = context.spawnAnonymous(ResultActor())
    store ! Store.Count(countResultActor)

    Behaviors.same
  }


}
