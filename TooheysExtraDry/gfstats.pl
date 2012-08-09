#!/usr/local/bin/perl

print "Users of gfriends (since 29 Apr 2005)\n";
print "-------------------------------------\n";
open(RESULTS, "cat /home/scossell/bin/gfred.users |sed -e \"s/ /\t/\" |cut -f 1|sort |uniq -c |sort|");

foreach(<RESULTS>) {
    if (/^(.*) (.*)$/) {
	print "$1\t";
	print "$2\t";
#	open(PP, "pp $2|grep -e \"  Name\"|");
#	foreach(<PP>) {
#	    if (/Name : (.*)/) {
#		print "$1";
#	    }
#	}
#	close(PP);
	print "\n";
#	system("classes $2");

	
    }
}
close(RESULTS);


print "Frequency of days used (since 29 Apr 2005)\n";
print "------------------------------------------\n";
system("cat /home/scossell/bin/gfred.users |sed -e \"s/ /\t/\"|cut -f 2|sed \"s/ /\t/\"|cut -f 1|sort|uniq -c|sort|tail -n 7");

print "The 25 most frequently used months\n";
print "----------------------------------\n";
system("cat  ~/bin/gfred.users | awk '{print $3 \" \" $7}' | uniq -c |sort | tail -n 25");
