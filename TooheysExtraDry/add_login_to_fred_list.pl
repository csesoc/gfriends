#!/usr/local/bin/perl

$friends_file = "$ENV{HOME}/.friends";

$login=$ARGV[0];
$nick=$ARGV[1];

if ($login ne "" && $nick ne "") {
  system("echo \"\nname = $login, $nick\" >> $friends_file");
}
