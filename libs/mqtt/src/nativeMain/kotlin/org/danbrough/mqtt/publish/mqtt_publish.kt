package org.danbrough.mqtt.publish

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.*
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
import platform.posix.free

private val log = KotlinLogging.logger("MQTTAsync").also {
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



/*
void connlost(void *context, char *cause)
{
MQTTAsync client = (MQTTAsync)context;
MQTTAsync_connectOptions conn_opts = MQTTAsync_connectOptions_initializer;
int rc;

printf("\nConnection lost\n");
if (cause)
printf("     cause: %s\n", cause);

printf("Reconnecting\n");
conn_opts.keepAliveInterval = 20;
conn_opts.cleansession = 1;
if ((rc = MQTTAsync_connect(client, &conn_opts)) != MQTTASYNC_SUCCESS)
{
printf("Failed to start connect, return code %d\n", rc);
finished = 1;
}
}
 */
private val connLost: CPointer<MQTTAsync_connectionLost> = staticCFunction { _, cause ->
  println("connLost: cause: ${cause?.toKString()}")
  // KotlinLogging.logger("MQTTAsync").error { "connection lost: cause: ${cause?.toKString()}" }
}

private val messageArrived: CPointer<MQTTAsync_messageArrived> = staticCFunction{ _,topicName,_,_->
  println("messageArrived: ${topicName?.toKString()}")
  topicName?.also { free(it) }
  1
}

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

    connectOptions.useContents {
      keepAliveInterval = 21
    }
    log.info { "keepAliveInterval: ${connectOptions.ptr.pointed.keepAliveInterval}" }
    runCatching {
      log.debug { "MQTTAsync_create" }

      /*
          if ((rc = MQTTAsync_create(&client, ADDRESS, CLIENTID,
        MQTTCLIENT_PERSISTENCE_NONE, NULL)) != MQTTCLIENT_SUCCESS)
       */


      MQTTAsync_create(client.ptr.getPointer(this), ADDRESS, CLIENTID, MQTTCLIENT_PERSISTENCE_NONE, null).also {
        if (it != MQTTASYNC_SUCCESS)
          error("Failed to create client, return code $it")
      }

      log.debug { "MQTTAsync_setCallbacks" }
      MQTTAsync_setCallbacks(client.value, client.value, connLost, messageArrived, null).also {
        if (it != MQTTASYNC_SUCCESS)
          error("MQTTAsync_setCallbacks failed: error: $it")
        else println("MQTTAsync_setCallbacks success")
      }


      /*
      	conn_opts.keepAliveInterval = 20;
	conn_opts.cleansession = 1;
	conn_opts.onSuccess = onConnect;
	conn_opts.onFailure = onConnectFailure;
	conn_opts.context = client;
	if ((rc = MQTTAsync_connect(client, &conn_opts)) != MQTTASYNC_SUCCESS)
	{
		printf("Failed to start connect, return code %d\n", rc);
		exit(EXIT_FAILURE);
	}
       */



      connectOptions.useContents {
        keepAliveInterval = 24
        cleansession = 1
      }

      log.info { "keepAliveInterval: ${connectOptions.useContents { keepAliveInterval }}" }

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