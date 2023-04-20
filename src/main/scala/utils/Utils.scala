package utils

import com.typesafe.config.{Config, ConfigFactory}

object Utils {
  def getConfig(port: Int): Config = ConfigFactory
    .parseString(s"akka.remote.artery.canonical.port=$port" +
    s"""
     |akka.cluster.roles = [${if(port == 25251) "store" else if(port == 25252) "client" else "reader"}]
     |""".stripMargin)
    .withFallback(ConfigFactory.load())
}