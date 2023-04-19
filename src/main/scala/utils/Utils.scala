package utils

import com.typesafe.config.{Config, ConfigFactory}

object Utils {
  def getConfig(port: Int): Config = ConfigFactory
    .parseString(s"akka.remote.artery.canonical.port=$port")
    .withFallback(ConfigFactory.load())
}