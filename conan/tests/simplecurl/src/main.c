#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <curl/curl.h>
#include <zlib.h>

int main(int argc, char **args) {

    printf("Running %s argc: %d\n", args[0], argc);

    char *url = argc < 1 ? args[1] : "https://www.danbrough.org";
    fprintf(stderr, "downloading %s ...\n", url);

    CURL *curl = curl_easy_init();
    if (curl != NULL) {
        curl_easy_setopt(curl, CURLOPT_URL, url);
        curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 1);
        CURLcode res = curl_easy_perform(curl);
        if (res != CURLE_OK) {
            printf("curl_easy_perform() failed: %s\n",curl_easy_strerror(res));
        }
        curl_easy_cleanup(curl);
    }

    return EXIT_SUCCESS;
}
