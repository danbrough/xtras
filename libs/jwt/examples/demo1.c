#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <jwt.h>



void jwt_log(char* msg){
    printf("LOG:%s\n",msg);
}

const int iat = 123456789;
const char* secret = "secret";


int main(int argc, char **argv) {
    printf("Hello world!!\n");
    jwt_t *jwt = NULL;
    int ret = 0;
    ret = jwt_new(&jwt);

    if (ret != 0 || jwt == NULL) {
        jwt_log( "invalid jwt");
        goto finish;
    }

    jwt_log("create jwt");


    ret = jwt_add_grant_int(jwt, "iat", iat);
    if (ret != 0) {
        jwt_log( "jwt_add_grant_int failed");
        goto finish;
    }

    printf("grant iat to %d\n",iat);

    ret = jwt_set_alg(jwt, JWT_ALG_HS256,secret,strlen(secret));
    if (ret != 0) {
        jwt_log( "jwt_set_alg failed");
        goto finish;
    }
    jwt_log("jwt_set_alg");

    jwt_dump_fp(jwt, stderr, 1);
    char *out = jwt_encode_str(jwt);
    if (out == NULL){
        fprintf(stderr,"error: %s\n", strerror(errno));
    }
    printf("output: %s\n",out);
    jwt_free_str(out);

finish:
    jwt_log("finish");
    jwt_free(jwt);
    return 0;
}