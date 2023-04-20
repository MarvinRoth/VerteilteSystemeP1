package Actors

import akka.NotUsed
import akka.actor.ProviderSelection.cluster
import akka.actor.typed.Behavior
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.ClusterEvent.MemberEvent
import akka.cluster.typed.{Cluster, Subscribe}

object Guardian {
  def apply(): Behavior[Receptionist.Listing] = Behaviors.setup[Receptionist.Listing] { context =>

    val member = Cluster(context.system).selfMember
    if(member.hasRole("store")) {
      context.spawnAnonymous(Store())
      context.system.receptionist ! Receptionist.Subscribe(Store.StoreServiceKey, context.self)
    }
    else if(member.hasRole("client")){
      val client1 = context.spawn(Client(), "client1")
      //val client2 = context.spawn(Client(), "client2")
      client1 ! Client.Set("IT", "Italia")
      //client2 ! Client.Get("IT")
      client1 ! Client.Get("DE")
      client1 ! Client.Get("IT")
      context.system.receptionist ! Receptionist.Subscribe(Client.ClientServiceKey, context.self)
    }
    else if(member.hasRole("reader")){
      val reader = context.spawn(FileReader(), "reader")
      val filename = "src/main/resources/trip_data_1000_000.csv"
      val batchSize = 1000
      reader ! FileReader.File(filename, batchSize)
      context.system.receptionist ! Receptionist.Subscribe(FileReader.ReaderServiceKey, context.self)
    }
    Behaviors.same
  }

}
