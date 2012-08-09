#!/usr/local/bin/perl

foreach(1..3) {
    system("sleep 1");
    print(STDERR "$_,");
}
system("killall lab");
print(STDERR, "Finished\n");
