
#include <MQTTAsync.h>

static inline MQTTAsync_connectOptions createConnectOptions(){
  return (MQTTAsync_connectOptions) MQTTAsync_connectOptions_initializer;
}


static inline MQTTAsync_message createMessage(){
  return (MQTTAsync_message) MQTTAsync_message_initializer;
}
