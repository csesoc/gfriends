

#include <iostream>
#include <unistd.h>
#include <cstdlib>

#define timeout_seconds 2

void catLabDataFile(char * lab);

int main(int argc, char **argv) {

  int return_status = 0;

  int program_execution_normal = 0;

  // if the data stored is pretty new
  char check[100];
  sprintf(check, "test `/home/stevec/TooheysExtraDry/sandbox/sclab/labDataAge.sh %s` -gt 300", argv[1]);
  if (1==system(check)) {
    catLabDataFile(argv[1]);    
    return 0; // meaning good execution
  }



  // otherwise rescan the labs
  alarm(timeout_seconds); // timeout
  if (argc > 1) {
    char command[100];
    sprintf(command, "lab %s" /* > /home/stevec/TooheysExtraDry/lab_data/%s.lab"*/, argv[1]);
    return_status = system(command);
    //    catLabDataFile(argv[1]);
    program_execution_normal = 1;
  } else {
    program_execution_normal = 1;
  }

  return (program_execution_normal ? 0 : 1);
}


void catLabDataFile(char * lab) {
  
  char command[100];
  sprintf(command, "cat /home/stevec/TooheysExtraDry/lab_data/%s.lab", lab);
  system(command);
  
  
}
