#!/usr/local/bin/perl

if (!defined($ARGV[0])) {
    print "ERROR";
    exit(0);
}

$username = $ARGV[0];

open(PP, "pp $username | grep 'User Name' | sed -e \"s/.*User.Name : //g\" | cut -f 1 -d' ' |");

while (<PP>) {
    print "$_";
}

close(PP);

open(PP, "pp $username | grep Aliases | sed -e 's/.*Aliases : //g' | sed -e \"s/ /\\n/g\" | ");

while (<PP>) {   
    
    print "$_";
    
}

close(PP);
