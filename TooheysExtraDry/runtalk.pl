#!/usr/local/bin/perl

if (!defined($ARGV[0]) && !defined($ARGV[1])) {
    die "no username specified";
    
}

$user=$ARGV[0];
$mach=$ARGV[1];

$command="xterm -exec \"talk "."$user"."@"."$mach"."\"";

system("$command");
