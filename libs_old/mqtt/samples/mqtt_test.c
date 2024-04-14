#include <stdio.h>
#include <stdlib.h>
//#include "MQTTClient.h"
#include "MQTTAsync.h"

#define ADDRESS     "tcp://mqtt.eclipseprojects.io:1883"
#define CLIENTID    "ExampleClientPub"
#define TOPIC       "MQTT Examples"
#define PAYLOAD     "Hello World!"
#define QOS         1
#define TIMEOUT     10000L





MQTTAsync_connectOptions createConnectOptions(){
  return (MQTTAsync_connectOptions) MQTTAsync_connectOptions_initializer;
}

int main(int argc,char **args){



  MQTTAsync client;
  MQTTAsync_connectOptions conn_opts = MQTTAsync_connectOptions_initializer;
  MQTTAsync_message pubmsg = MQTTAsync_message_initializer;
  int rc;

  printf("Hello world\n");

  if ((rc = MQTTAsync_create(&client, ADDRESS, CLIENTID,
                              MQTTCLIENT_PERSISTENCE_NONE, NULL)) != MQTTASYNC_SUCCESS)
  {
    printf("Failed to create client, return code %d\n", rc);
    rc = EXIT_FAILURE;
    goto exit;
  }

  printf("Created client\n");

  exit:
  return 0;
}