#!/usr/local/bin/perl

die "ERROR" unless (defined($ARGV[0]));

print "debug->$ARGV[0]\n";

$cmd="xterm -geom 100x50 -exec \"echo 'Requesting $ARGV[0] user info...';ssh $ARGV[0] who; ask 'Press to close'\"";

system($cmd);

