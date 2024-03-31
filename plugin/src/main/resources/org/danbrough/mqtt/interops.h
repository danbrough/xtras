
#include <MQTTAsync.h>

static inline MQTTAsync_connectOptions mqtt_createConnectOptions(){
  return (MQTTAsync_connectOptions) MQTTAsync_connectOptions_initializer;
}


static inline MQTTAsync_message mqtt_createMessage(){
  return (MQTTAsync_message) MQTTAsync_message_initializer;
}

static inline void printConnectOptions(MQTTAsync_connectOptions* opts){
  printf("printConnectOptions: keepAliveInterval: %d cleanSession: %d\n",opts->keepAliveInterval,opts->cleansession);
}


static inline MQTTAsync_responseOptions mqtt_responseOptions(){
  return (MQTTAsync_responseOptions ) MQTTAsync_responseOptions_initializer;
}

