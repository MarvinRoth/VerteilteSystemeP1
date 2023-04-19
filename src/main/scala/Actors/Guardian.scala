package Actors

import akka.NotUsed
import akka.actor.typed.Behavior
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.Behaviors

object Guardian {
  def apply(): Behavior[Receptionist.Listing] = Behaviors.setup[Receptionist.Listing] { context =>

    context.spawnAnonymous(Store())
    context.system.receptionist ! Receptionist.Subscribe(Store.StoreServiceKey, context.self)
    val client1 = context.spawn(Client(), "client1")
    val client2 = context.spawn(Client(), "client2")
    context.system.receptionist ! Receptionist.Subscribe(Client.ClientServiceKey, context.self)
    client1 ! Client.Set("IT", "Italia")
    client2 ! Client.Get("IT")
    client1 ! Client.Get("DE")
    client1 ! Client.Get("IT")
    val reader = context.spawn(FileReader(), "reader")
    context.system.receptionist ! Receptionist.Subscribe(FileReader.ReaderServiceKey, context.self)
    val filename = "src/main/resources/trip_data_1000_000.csv"
    val batchSize = 100
    reader ! FileReader.File(filename, batchSize)

    Behaviors.same
  }

}
