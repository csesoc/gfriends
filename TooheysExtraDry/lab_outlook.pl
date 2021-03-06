#!/usr/local/bin/perl

if (!defined($ARGV[0])) {
    print "";
    exit;
}

$lab = $ARGV[0];
$day = "";

$lab = substr($lab, 1);

open(DAY, "date +%a|");

while (<DAY>) {
    if (m/Sun/) {exit;}
    if (m/Sat/) {exit;}
    
#    if (m/Mon/) {$day="[^0-9a-zA-Z]1[^0-9a-zA-Z]";}
#    if (m/Tue/) {$day="[^0-9a-zA-Z]2[^0-9a-zA-Z]";}
#    if (m/Wed/) {$day="[^0-9a-zA-Z]3[^0-9a-zA-Z]";}
#    if (m/Thu/) {$day="[^0-9a-zA-Z]4[^0-9a-zA-Z]";}
#    if (m/Fri/) {$day="[^0-9a-zA-Z]5[^0-9a-zA-Z]";}

    if (m/Mon/) {$day="mon";}
    if (m/Tue/) {$day="tue";}
    if (m/Wed/) {$day="wed";}
    if (m/Thu/) {$day="thu";}
    if (m/Fri/) {$day="fri";}

}
close(DAY);

# open(TIMES, "cat mjc.txt| grep $lab| grep -e \"$day\"|");

# while(<TIMES>) {
    
#    if (m/$day([0-9]+)\s+([0-9]+)\s+([A-Za-z0-9]{8})/) {
#	print "$1 $2 $3\n";
#    }
    
#}

#exit(0);
##====================================================================================================================================================
##====================================================================================================================================================
# below here the version of code that worked for 2008, before the above code was used from the rectangles project

#DEBUG
#$day="wed";
#print "Looking up $lab on $day in session $session\n";

$session = "10s2";


open(TIMES, "cat /home/give/public_html/Timetables/labs/all$session |grep $day |grep $lab |cut -d' ' -f4-7|cut -d' ' -f3 --complement|sort|uniq|");

while (<TIMES>) {
    print "$_";
}


exit(0);

##====================================================================================================================================================
##====================================================================================================================================================
## below here is from before with the crappy not working code
$time="";
$current_time="";
$out="";

if (!defined($ARGV[0])) {
    exit;
}

$lab = $ARGV[0];

open(OUTLOOK, "echo \"av\" | book | head -n 103 | tail -n 96 |grep -v Labs|");

foreach(<OUTLOOK>) {

    if (/Period: (\d\d):(\d\d)/) {
	$time = "$1"."$2";
	if ($current_time eq "") {
	    $current_time = $time;
	} else {
	    if ($current_time > $time) {
		last;
	    }
	}
    } else {
	if (!/$lab/) {
	    $out=$out."$time\n";
	}

    }
}

system("echo \"$out\" |uniq");

close(OUTLOOK);



