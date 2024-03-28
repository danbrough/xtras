


#include <MQTTClient.h>

MQTTClient_connectOptions createConnectOptions(){
  return (MQTTClient_connectOptions) MQTTClient_connectOptions_initializer;
}

MQTTClient_message createMessage() {
  return (MQTTClient_message) MQTTClient_message_initializer;
}

