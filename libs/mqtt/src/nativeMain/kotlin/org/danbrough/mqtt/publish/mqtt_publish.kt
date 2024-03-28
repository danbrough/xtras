package org.danbrough.mqtt.publish

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cValue
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.objcPtr
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.set
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKString
import kotlinx.cinterop.useContents
import kotlinx.cinterop.value
import org.danbrough.mqtt.cinterops.MQTTCLIENT_PERSISTENCE_NONE
import org.danbrough.mqtt.cinterops.MQTTCLIENT_SUCCESS
import org.danbrough.mqtt.cinterops.MQTTClient
import org.danbrough.mqtt.cinterops.MQTTClientVar

import org.danbrough.mqtt.cinterops.MQTTClient_connectOptions
import org.danbrough.mqtt.cinterops.MQTTClient_connectionLost
import org.danbrough.mqtt.cinterops.MQTTClient_create
import org.danbrough.mqtt.cinterops.MQTTClient_deliveryComplete
import org.danbrough.mqtt.cinterops.MQTTClient_deliveryToken
import org.danbrough.mqtt.cinterops.MQTTClient_deliveryTokenVar
import org.danbrough.mqtt.cinterops.MQTTClient_freeMessage
import org.danbrough.mqtt.cinterops.MQTTClient_message
import org.danbrough.mqtt.cinterops.MQTTClient_messageArrived
import org.danbrough.mqtt.cinterops.MQTTClient_setCallbacks
import org.danbrough.mqtt.cinterops.createConnectOptions
import org.danbrough.mqtt.cinterops.createMessage
import org.danbrough.xtras.support.initLogging

val log = KotlinLogging.logger("MQTTClient").also {
  initLogging(it)
}

const val ADDRESS = "tcp://mqtt.eclipseprojects.io:1883"
const val CLIENTID = "ExampleClientPub"
val deliveryToken: MQTTClient_deliveryTokenVar = nativeHeap.alloc()

/*


#define ADDRESS     "tcp://mqtt.eclipseprojects.io:1883"
#define CLIENTID    "ExampleClientPub"
#define TOPIC       "MQTT Examples"
#define PAYLOAD     "Hello World!"
#define QOS         1
#define TIMEOUT     10000L
 */
@OptIn(ExperimentalForeignApi::class)
fun main(args: Array<String>) {

  log.trace { "trace()" }
  log.info { "main()" }

  memScoped {
    val client:MQTTClientVar = alloc()
    val connectOptions: CValue<MQTTClient_connectOptions> = createConnectOptions()
    val message: CValue<MQTTClient_message> = createMessage()
    val token: MQTTClient_deliveryTokenVar = alloc()
    val rc = 0


    runCatching {


      log.debug { "MQTTClient_create" }

      /*
          if ((rc = MQTTClient_create(&client, ADDRESS, CLIENTID,
        MQTTCLIENT_PERSISTENCE_NONE, NULL)) != MQTTCLIENT_SUCCESS)
       */
      MQTTClient_create(client.ptr, ADDRESS, CLIENTID, MQTTCLIENT_PERSISTENCE_NONE, null).also {
        if (it != MQTTCLIENT_SUCCESS)
          error("Failed to create client, return code $it")
      }



      //typedef void MQTTClient_connectionLost(void* context, char* cause);
      val connLost: CPointer<MQTTClient_connectionLost> = staticCFunction { _, cause ->
        KotlinLogging.logger("MQTTClient").error { "connection lost: cause: ${cause?.toKString()}" }
      }

      //typedef int MQTTClient_messageArrived(void* context, char* topicName, int topicLen, MQTTClient_message* message);
      val messageArrived: CPointer<MQTTClient_messageArrived> = staticCFunction { _, topicName, topicLen, message ->
        KotlinLogging.logger("MQTTClient").info{"MQTTClient_messageArrived: topicName: $topicName topicLen:$topicLen"}

        if (message != null)
          MQTTClient_freeMessage(message.reinterpret())

        if (topicName != null)
          MQTTClient_freeMessage(topicName.reinterpret())

        1
      }

      //typedef void MQTTClient_deliveryComplete(void* context, MQTTClient_deliveryToken dt);
      val delivered:CPointer<MQTTClient_deliveryComplete> = staticCFunction {_,token->
       // deliveryToken.value = token
        KotlinLogging.logger("MQTTClient").info{"MQTTClient_deliveryComplete: token: $token"}
      }

      log.debug { "MQTTClient_setCallbacks" }
      //if ((rc = MQTTClient_setCallbacks(client, NULL, connlost, msgarrvd, delivered)) != MQTTCLIENT_SUCCESS)
      MQTTClient_setCallbacks(client.ptr, null, null, null, null).also {
        if (it != MQTTCLIENT_SUCCESS)
          error("MQTTClient_setCallbacks failed: error: $it")
      }

      connectOptions.useContents {
        keepAliveInterval = 20
        cleansession = 1
      }

      log.trace { "connectOptions keepAliveInterval: ${connectOptions.useContents { keepAliveInterval }}" }
      log.warn { "connectOptions cleansession: ${connectOptions.useContents { cleansession }}" }


      /*
    if ((rc = MQTTClient_connect(client, &conn_opts)) != MQTTCLIENT_SUCCESS)
    {
        printf("Failed to connect, return code %d\n", rc);
        rc = EXIT_FAILURE;
        goto destroy_exit;
    }
*/
    }
  }.exceptionOrNull().also {
    if (it != null) {
      log.error(it) { "exception ${it.message}" }
    }
  }
  /*



   */
}