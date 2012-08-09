
#include <iostream>

int main(int argc, char **argv) {

  int return_status = 0;

  if (argc > 1) {
    char command[100];
    sprintf(command, "/home/stevec/TooheysExtraDry/runlab %s ; killall lab &> /dev/null", argv[1]);
    return_status = system(command);
  } 

  return return_status;
}
