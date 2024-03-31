package org.danbrough.mqtt.publish

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import org.danbrough.mqtt.cinterops.MQTTASYNC_SUCCESS
import org.danbrough.mqtt.cinterops.MQTTAsync
import org.danbrough.mqtt.cinterops.MQTTAsyncVar
import org.danbrough.mqtt.cinterops.MQTTAsync_connect
import org.danbrough.mqtt.cinterops.MQTTAsync_connectOptions
import org.danbrough.mqtt.cinterops.MQTTAsync_connectionLost
import org.danbrough.mqtt.cinterops.MQTTAsync_create
import org.danbrough.mqtt.cinterops.MQTTAsync_messageArrived
import org.danbrough.mqtt.cinterops.MQTTAsync_onFailure
import org.danbrough.mqtt.cinterops.MQTTAsync_onSuccess
import org.danbrough.mqtt.cinterops.MQTTAsync_responseOptions
import org.danbrough.mqtt.cinterops.MQTTAsync_setCallbacks
import org.danbrough.mqtt.cinterops.MQTTCLIENT_PERSISTENCE_NONE
import org.danbrough.mqtt.cinterops.printConnectOptions
import org.danbrough.mqtt.createMQTTAsyncConnectOptions
import org.danbrough.xtras.support.initLogging
import platform.posix.free
import platform.posix.usleep

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

object Callbacks {
   val connLost: CPointer<MQTTAsync_connectionLost> = staticCFunction { _, cause ->
    println("connLost: cause: ${cause?.toKString()}")
    // KotlinLogging.logger("MQTTAsync").error { "connection lost: cause: ${cause?.toKString()}" }
  }

   val messageArrived: CPointer<MQTTAsync_messageArrived> =
    staticCFunction { _, topicName, _, _ ->
      println("messageArrived: ${topicName?.toKString()}")
      topicName?.also { free(it) }
      1
    }


/*
* void onConnect(void* context, MQTTAsync_successData* response)
{
	MQTTAsync client = (MQTTAsync)context;
	MQTTAsync_responseOptions opts = MQTTAsync_responseOptions_initializer;
	MQTTAsync_message pubmsg = MQTTAsync_message_initializer;
	int rc;

	printf("Successful connection\n");
	opts.onSuccess = onSend;
	opts.onFailure = onSendFailure;
	opts.context = client;
	pubmsg.payload = PAYLOAD;
	pubmsg.payloadlen = (int)strlen(PAYLOAD);
	pubmsg.qos = QOS;
	pubmsg.retained = 0;
	if ((rc = MQTTAsync_sendMessage(client, TOPIC, &pubmsg, &opts)) != MQTTASYNC_SUCCESS)
	{
		printf("Failed to start sendMessage, return code %d\n", rc);
		exit(EXIT_FAILURE);
	}
}*/

  //typedef void MQTTAsync_onSuccess(void* context, MQTTAsync_successData* response)
  val onConnect: CPointer<MQTTAsync_onSuccess> = staticCFunction { context, _ ->
    println("Callbacks.onConnect()")
    val client = context!!.reinterpret<MQTTAsyncVar>()
    println("got client: $client")


  }

  //typedef void MQTTAsync_onFailure(void* context,  MQTTAsync_failureData* response);
  val onFailure: CPointer<MQTTAsync_onFailure> = staticCFunction { _, response ->
    println("onFailure:: connect failed rc: ${response?.pointed?.code ?: 0}")
    /*
    printf("Connect failed, rc %d\n", response ? response->code : 0);
	finished = 1;
     */
  }
}

private var finished = false

@OptIn(ExperimentalForeignApi::class)
fun main(args: Array<String>) {

  log.trace { "trace()" }
  log.info { "main()" }

  memScoped {
    val client: MQTTAsyncVar = alloc()

    val connectOptions: CPointer<MQTTAsync_connectOptions> = createMQTTAsyncConnectOptions()
    //val message: MQTTAsync_message = createMQTTAsyncMessage()

    runCatching {
      log.debug { "MQTTAsync_create" }

      MQTTAsync_create(
        client.ptr.getPointer(this),
        ADDRESS,
        CLIENTID,
        MQTTCLIENT_PERSISTENCE_NONE,
        null
      ).also {
        if (it != MQTTASYNC_SUCCESS)
          error("Failed to create client, return code $it")
      }

      log.debug { "MQTTAsync_setCallbacks" }
      MQTTAsync_setCallbacks(client.value, client.value, Callbacks.connLost, Callbacks.messageArrived, null).also {
        if (it != MQTTASYNC_SUCCESS)
          error("MQTTAsync_setCallbacks failed: error: $it")
        else println("MQTTAsync_setCallbacks success")
      }

      connectOptions.pointed.apply {
        keepAliveInterval = 20
        cleansession = 1
        onSuccess = Callbacks.onConnect
        onFailure = Callbacks.onFailure
        context = client.ptr
      }

      log.info { "keepAliveInterval: ${connectOptions.pointed.keepAliveInterval} cleansession: ${connectOptions.pointed.cleansession}" }
      printConnectOptions(connectOptions)

      log.debug { "MQTTAsync_connect .." }
      MQTTAsync_connect(client.value,connectOptions).also {
        if (it != MQTTASYNC_SUCCESS)
          error("MQTTAsync_connect failed: code:$it")
      }

      log.debug { "connected" }
      do {
       usleep(10000u)
      } while (!finished)

    }
  }.exceptionOrNull().also {
    if (it != null) {
      log.error(it) { "exception ${it.message}" }
    }
  }
  /*



   */
}


