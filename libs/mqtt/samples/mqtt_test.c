#include <stdio.h>
#include <stdlib.h>
#include "MQTTClient.h"

#define ADDRESS     "tcp://mqtt.eclipseprojects.io:1883"
#define CLIENTID    "ExampleClientPub"
#define TOPIC       "MQTT Examples"
#define PAYLOAD     "Hello World!"
#define QOS         1
#define TIMEOUT     10000L

MQTTClient_connectOptions createConnectOptions(){
  return (MQTTClient_connectOptions) MQTTClient_connectOptions_initializer;
}

int main(int argc,char **args){



  MQTTClient client;
  MQTTClient_connectOptions conn_opts = MQTTClient_connectOptions_initializer;
  MQTTClient_message pubmsg = MQTTClient_message_initializer;
  MQTTClient_deliveryToken token;
  int rc;

  printf("Hello world\n");

  if ((rc = MQTTClient_create(&client, ADDRESS, CLIENTID,
                              MQTTCLIENT_PERSISTENCE_NONE, NULL)) != MQTTCLIENT_SUCCESS)
  {
    printf("Failed to create client, return code %d\n", rc);
    rc = EXIT_FAILURE;
    goto exit;
  }

  exit:
  return 0;
}