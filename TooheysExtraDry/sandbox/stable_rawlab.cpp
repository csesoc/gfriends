
#include <iostream>
#include <cstdlib>
#include <pthread.h>
#include <map>

void init(std::map<char*,int> & mach_num);
void *check_lab(void * input);

bool waiting;
bool answer;

int main(int argc, char **argv) {

  std::map<char*,int> mach_num;
  init(mach_num);
  
  for (std::map<char*,int>::iterator iter = mach_num.begin();
       iter != mach_num.end(); iter++) {
    char *lab = iter->first;
    int num = iter->second;
    

    //# /usr/local/etc/smadmin -t moog00
    waiting = true;
    answer = false;
    
    int threaded_func_id;
    pthread_t p_thread;
    threaded_func_id = pthread_create(&p_thread, NULL, 
				      check_lab,
				      (void*)lab);
        
    
    
    
  }
  
}

void init(std::map<char*,int> & mach_num) {
  
  mach_num["harp"]=20;
  mach_num["tuba"]=21;
  mach_num["drum"]=20;
  mach_num["oboe"]=20;
  
  mach_num["moog"]=20;
  mach_num["bell"]=20;
  mach_num["leaf"]=19;
  mach_num["spoons"]=20;
  
  mach_num["bugle"]=21;
  mach_num["pipe"]=21;

  mach_num["banjo"]=20;
  mach_num["oud"]=16;
  
  mach_num["clavier"]=20;
  mach_num["piano"]=18;
  mach_num["organ"]=20;
  
};


void *check_lab(void *input) {
  
  char *lab = (char *)input;
  
  char command[100];
  sprintf(command, "/usr/local/etc/smadmin -t %s", lab);
  system(command);
  waiting = false;
    
};
