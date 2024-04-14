package org.danbrough.mqtt

expect fun getenv(name: String): String?

object Demo {
  private const val DEFAULT_ADDRESS = "tcp://mqtt.eclipseprojects.io:1883"
  private const val DEFAULT_TOPIC = "MQTT_Examples"
  private const val DEFAULT_CLIENT_ID = "ExampleClientPub"
  private const val DEFAULT_QOS = "1"


  private fun mqttProperty(name: String, defValue: String? = null): String? =
    "MQTT_${name.uppercase()}".let { key ->
      getenv(key) ?: defValue.also {
        log.trace{"$key not set. Defaulting to $it"}
      }
    }


  val username: String? = mqttProperty("username")

  val password: String? = mqttProperty("password")

  val address: String = mqttProperty("address", DEFAULT_ADDRESS)!!

  val topic: String = mqttProperty("topic", DEFAULT_TOPIC)!!

  val clientID:String = mqttProperty("client_id", DEFAULT_CLIENT_ID)!!

  val qos:Int = mqttProperty("qos", DEFAULT_QOS)!!.toInt()

  val caPath:String? = mqttProperty("capath")

  val caFile:String? = mqttProperty("cafile")

  override fun toString() = """
    Demo: 
      username: $username
      address: $address
      password: ${password?.let { "************" } ?: "not set"}
      topic: $topic 
      clientID: $clientID
      qos: $qos
      caPath: $caPath
      caFile: $caFile

  """.trimIndent()
}


