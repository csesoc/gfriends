#!/usr/local/bin/perl

die "ERROR" unless (defined($ARGV[0]));

print "debug->$ARGV[0]\n";

$cmd="xterm -geom 100x40 -exec \"/home/stevec/bin/qstat -P -q3s $ARGV[0] ; ask 'Press to close'\"";

system($cmd);

