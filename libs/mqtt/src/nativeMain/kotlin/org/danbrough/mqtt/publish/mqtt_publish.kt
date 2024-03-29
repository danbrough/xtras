package org.danbrough.mqtt.publish

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKString
import kotlinx.cinterop.useContents
import org.danbrough.mqtt.cinterops.MQTTASYNC_SUCCESS
import org.danbrough.mqtt.cinterops.MQTTAsyncVar
import org.danbrough.mqtt.cinterops.MQTTAsync_connectOptions
import org.danbrough.mqtt.cinterops.MQTTAsync_connectionLost
import org.danbrough.mqtt.cinterops.MQTTAsync_create
import org.danbrough.mqtt.cinterops.MQTTAsync_deliveryComplete
import org.danbrough.mqtt.cinterops.MQTTAsync_freeMessage
import org.danbrough.mqtt.cinterops.MQTTAsync_message
import org.danbrough.mqtt.cinterops.MQTTAsync_messageArrived
import org.danbrough.mqtt.cinterops.MQTTAsync_setCallbacks
import org.danbrough.mqtt.cinterops.MQTTCLIENT_PERSISTENCE_NONE
import org.danbrough.mqtt.cinterops.createConnectOptions
import org.danbrough.mqtt.cinterops.createMessage
import org.danbrough.xtras.support.initLogging

val log = KotlinLogging.logger("MQTTAsync").also {
  initLogging(it)
}

const val ADDRESS = "tcp://mqtt.eclipseprojects.io:1883"
const val CLIENTID = "ExampleClientPub"


//val deliveryToken: MQTTAsync_deliveryTokenVar = nativeHeap.alloc()

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
    val client:MQTTAsyncVar = alloc()
    val connectOptions: CValue<MQTTAsync_connectOptions> = createConnectOptions()
    val message: CValue<MQTTAsync_message> = createMessage()
    //val token: MQTTAsync_deliveryTokenVar = alloc()
    val rc = 0


    runCatching {


      log.debug { "MQTTAsync_create" }

      /*
          if ((rc = MQTTAsync_create(&client, ADDRESS, CLIENTID,
        MQTTCLIENT_PERSISTENCE_NONE, NULL)) != MQTTCLIENT_SUCCESS)
       */
      MQTTAsync_create(client.ptr.pointed.ptr, ADDRESS, CLIENTID, MQTTCLIENT_PERSISTENCE_NONE, null).also {
        if (it != MQTTASYNC_SUCCESS)
          error("Failed to create client, return code $it")
      }



      //typedef void MQTTAsync_connectionLost(void* context, char* cause);
      val connLost: CPointer<MQTTAsync_connectionLost> = staticCFunction { _, cause ->
       // KotlinLogging.logger("MQTTAsync").error { "connection lost: cause: ${cause?.toKString()}" }
      }

      //typedef int MQTTAsync_messageArrived(void* context, char* topicName, int topicLen, MQTTAsync_message* message);
      val messageArrived: CPointer<MQTTAsync_messageArrived> = staticCFunction { _, topicName, topicLen, message ->
        KotlinLogging.logger("MQTTAsync").info{"MQTTAsync_messageArrived: topicName: $topicName topicLen:$topicLen"}

        if (message != null)
          MQTTAsync_freeMessage(message.reinterpret())

        if (topicName != null)
          MQTTAsync_freeMessage(topicName.reinterpret())

        1
      }

      //typedef void MQTTAsync_deliveryComplete(void* context, MQTTAsync_deliveryToken dt);
      val delivered:CPointer<MQTTAsync_deliveryComplete> = staticCFunction {_,token->
       // deliveryToken.value = token
        KotlinLogging.logger("MQTTAsync").info{"MQTTAsync_deliveryComplete: token: $token"}
      }

      log.debug { "MQTTAsync_setCallbacks" }
      //if ((rc = MQTTAsync_setCallbacks(client, NULL, connlost, msgarrvd, delivered)) != MQTTCLIENT_SUCCESS)
      MQTTAsync_setCallbacks(client.ptr, client.ptr, connLost, null, null).also {
        if (it != MQTTASYNC_SUCCESS)
          error("MQTTAsync_setCallbacks failed: error: $it")
      }

      connectOptions.useContents {
        keepAliveInterval = 20
        cleansession = 1
      }

      log.trace { "connectOptions keepAliveInterval: ${connectOptions.useContents { keepAliveInterval }}" }
      log.warn { "connectOptions cleansession: ${connectOptions.useContents { cleansession }}" }


      /*
    if ((rc = MQTTAsync_connect(client, &conn_opts)) != MQTTCLIENT_SUCCESS)
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