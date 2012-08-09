#!/usr/local/bin/perl

$friends="$ENV{HOME}/.friends";

# just incase something fk's up
system("cp -f $friends $friends.backup");

system("/home/stevec/TooheysExtraDry/update_friends_aliases.pl | uniq > /tmp/.friends.up ; cp -f /tmp/.friends.up $friends");

