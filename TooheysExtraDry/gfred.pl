#!/usr/local/bin/perl -w

#Graphical Friends List - based on the friends script qritten by trents@cse
# and the server scanning friends script by neilm@cse
#By Stephen Cossell (scos506)



#print "leafn20.0\n";
# --> note: if a lab is responding put it below this comment, otherwise put it in the line above
# ^ don't need the above anymore, i have fixed the non-responsive lab problem


sub purify {
    my $str = shift(@_);
    $str =~ s/;//g;
    return $str
}


# THE LIST OF LABS TO SCAN
@labs=("moog","spoons","bell","leaf","oud","banjo","bugle","pipe","piano","organ","clavier","sanhu","erhu");

$username = "";

open(UN, "id -un|");
while (<UN>) {
    if (m/(\S+)/) {
	$username = "$1";
    }
}
close(UN);

# ADD IN OUD IF YOU HAVE ACCESS TO OUD

#if (system("pp OudAccess/ | grep -q `id -un`") == 0 ||
#    system("pp |grep -q stevec") == 0 ||
#    system("pp |grep -q nmen702") == 0 ||
#    system("pp |grep -q hlai") == 0) {
#    @labs=(@labs, "oud");
#}


# ADD IN BANJO IF YOU HAVE ACCESS TO BANJO
#if (system("pp |grep -q 4910_Student") == 0 ||
#    system("pp |grep -q 4911_Student") == 0 ||
#    system("pp |grep -q BIOM5920_Student") == 0 ||
#    system("pp |grep -q BIOM5940_Student") == 0 ||
#    system("pp |grep -q BIOM5909_Student") == 0 ||
#    system("pp |grep -q BIOM5921_Student") == 0 ||
#    system("pp |grep -q BIOM5941_Student") == 0 ||
#    system("pp |grep -q BIOM5904_Student") == 0 ||
#    system("acc |grep -q ajc") == 0 ||
#   system("acc |grep -q nmen702") == 0) {
#    @labs=(@labs, "banjo");
#}



%therms=();

foreach $lab (@labs) {
    
    open(THERM, "tail -n 1 ~status/temperature/lab-$lab|") or die "could not do";
    
    while (<THERM>) {
	if (m/^\d+\s(\d*[.]\d)/) {
	    if ($lab eq "oud") {
		$therms{"oudx"} = $1;
	    } else {
		$therms{substr($lab,0,4)} = $1;
	    }
	}
    }    
    close(THERM);
}


%friends=();
$friends="$ENV{HOME}/.friends";

$current_group_index=0;
%friends_to_group=();
print "grfriends,0,255,0\n";

#%murders=();
#$murders="/import/elfman/2/scos506/bin/.murder";

#%sengers=();
#$sengers="/home/scossell/bin/.sengers";

if (! -r $friends) {
    system("echo -e 'You do not have enough friends to run this program.  Please make some friends. =P  \n For the meantime we have leant you some friends. \n Check the ~/.friends file to add friends.' | xmessage -file - ");
    system("echo 'name=stevec, Steve' >> ~/.friends");
    system("echo 'name=ijgo605, Ian' >> ~/.friends");

    system("echo ' ' >> ~/.friends");
    system("echo 'group:CSESoc Exec:255,200,0' >> ~/.friends");
    system("echo 'name=peterm, Peter' >> ~/.friends");
    system("echo 'name=akeswani, Aditya' >> ~/.friends");
    system("echo 'name=nataliew, Natalie' >> ~/.friends");
    system("echo 'name=ddp,   Dan' >> ~/.friends");
    system("echo 'name=yhunter, Youssef' >> ~/.friends");
    
}

print(STDERR "found all files\n");

open (RC,"<$friends") or 
    die "ERROR: could not open your ~/.friends: $!";
while (<RC>) {
    next if (/^\s*$/);
    if (/\s*name\s*=\s*(\S+)\s*,\s*(.*)/) {
	$fr_login = purify("$1");
	$fr_nick = purify("$2");

        $friends{"$fr_login"}=$fr_nick;
	$friends_to_group{"$fr_login"}=$current_group_index;
    } elsif (/\s*group:(.*):([0-9]*),([0-9]*),([0-9]*)/) {
	$gr_name = purify("$1");
	$gr_r = purify("$2");
	$gr_g = purify("$3");
	$gr_b = purify("$4");
	
	print "gr$gr_name,$gr_r,$gr_g,$gr_b\n";
	$current_group_index += 1;
    } else {
	warn "warning: ignoring line: $_";
    }
}
close (RC);

print(STDERR "Friends file read\n");

#open (RC,"<$murders") or
#    print "There seems to be a problem in the murder list";
#while (<RC>) {
#    next if (/^\s*$/);
#if (/\s*name\s*=\s*(\S+)\s*,\s*(.*)/) {
#    $murders{"$1"}=$2;
#} else {
#    warn "warning: ignoring line: $_";
#}
#}
#close (RC);

#print(STDERR "DEBUG:Murders file read\n");

#
#open (RC,"<$sengers") or
#    print "There seems to be a problem in the sengers list";
#while (<RC>) {
#    next if (/^\s*$/);
#    if (/\s*name\s*=\s*(\S+)\s*,\s*(.*)/) {
#        $sengers{"$1"}=$2;
#    } else {
#	warn "warning: ignoring line: $_";
#    }
#}
#close (RC);

#print (STDERR "Sengers file read\n");

#----------BELOW THIS LINE WORKS ON ITS OWN

$last=0;
foreach $lab (@labs) {

    print(STDERR "\e[1;36m$lab\e[0m:\e[1;34mrequesting\e[0m...");

    $tmp_filename = "/tmp/$lab.$username";

    system("/home/stevec/TooheysExtraDry/sclab $lab > $tmp_filename 2> /dev/null");
    system("chmod 777 $tmp_filename");

    if (system("wc -l $tmp_filename | cut -f1 -d' ' | grep -qe '^0'") == 0) {
	# the sclab command timed out
	print(STDERR "\e[1;33mnot responding\e[0m...\e[4;31mignoring\e[0m\n");
	system("rm -f $tmp_filename");
	next;
    }

    open(LAB, "$tmp_filename") or die "Could not scan $lab lab";

    print(STDERR "\e[1;33mscanning\e[0m...");

    while (<LAB>) {
	next if (/^\s*$/);
	
	if (/^(\S+):/) {
	    $machine=$1;
	    if (/clavier([0-9])([0-9])/) {$machine = "clav"."$1"."$2";}
	    if (/organ([0-9])([0-9])/) {$machine = "orga"."$1"."$2";}
	    if (/piano([0-9])([0-9])/) {$machine = "pian"."$1"."$2";}
	    if (/bongo([0-9])([0-9])/) {$machine = "bong"."$1"."$2";}
	    if (/spoons([0-9])([0-9])/) {$machine = "spoo"."$1"."$2";}
	    if (/bugle([0-9])([0-9])/) {$machine = "bugl"."$1"."$2";}
	    if (/banjo([0-9])([0-9])/) {$machine = "banj"."$1"."$2";}
	    if (/conga([0-9])([0-9])/) {$machine = "cong"."$1"."$2";}
	    if (/oud([0-9])([0-9])/) {$machine = "oudx"."$1"."$2";}
	    if (/sanhu([0-9])([0-9])/) {$machine = "sanh"."$1"."$2";}
	    
	    if (/:Down/) {
		print "$machine"."du\n";
	    } elsif (/(Allocated|Tentative): (\s+).*{\sCOMP(\w\w\w\w)/) {
		print "$machine"."buCOMP"."$3\n";
	    } elsif (/(Allocated|Tentative): (\s+).*{\sBINF(\w\w\w\w)/) {	 
		print "$machine"."buBINF"."$3\n";
	    } elsif (/(Allocated|Tentative): (\s+).*{\sENGG(\w\w\w\w)/) {
		print "$machine"."buENGG"."$3\n";
	    } elsif (/(Allocated|Tentative): (\S+).*\ssince\s(\S+)/) {
		$user=$2;
	       
		if (defined($friends{$user})) {
		    print "$machine"."of"."$user"."=$friends{$user}"."=$friends_to_group{$user}\n";
		} else {
		    print "$machine"."or"."$user\n";			
		}

	    } else {
		print "$machine"."au\n";
	    }
		 } else {
		     # special "oud has three letters and therefore oud->oudx" hack
		     if (/Lab\soud.*\sCLOSED/) {
			 $machine = "oudx";
			 print "$machine"."x"."$therms{$machine}\n";
		     }
		     if (/Lab\soud.*\sFULL/) {
			 $machine = "oudx";
			 print "$machine"."c"."$therms{$machine}\n";
		     }
		     if (/Lab\soud.*\sFREE/) {
			 $machine = "oudx";
			 print "$machine"."n"."$therms{$machine}\n";
		     }
		     
		     # the rest of the labs
		     if (/Lab\s(\w\w\w\w).*\sCLOSED/) {
			 $machine=$1;
			 print "$machine"."x"."$therms{$machine}\n";
		     }	
		     if (/Lab\s(\w\w\w\w).*\sFULL/) {
			 $machine=$1;
			 print "$machine"."c"."$therms{$machine}\n";
		     }
		     if (/Lab\s(\w\w\w\w).*\sFREE/) {
			 $machine=$1;
			 if (!defined($therms{$machine})) {
			     print "$machine"."n23.0\n";
			 } else {
			     print "$machine"."n"."$therms{$machine}\n";
			 }
		     }
		     $last++;
		 }
		    
		     
		 }
		     close(LAB);
		     print(STDERR "\e[4;31mdone\e[0m\n");
		  
		     system("rm -f $tmp_filename");
		 }

	$s_blink = "\e[5;31m";
	$s_NC = "\e[0m";
	print(STDERR "\n$s_blink Thanks for using gfriends [CSE's first social network]. $s_NC\n");


    exit(0);

    print(STDERR "looking at weill...");

print "weilw";
open(WEILL, "ssh weill who 2>/dev/null |") or die "cannot connect...";
while(<WEILL>) {
    next if (/^\s*$/);
    ($user,$_,$month,$date,$time,$location) = split(/\s+/);
    if (defined($friends{$user})) {
	if ($location =~ /math/) {
	    print "$friends{$user}(maths),";
	} elsif ($location =~ /wireless/) {
	    print "$friends{$user}(wireless),";
	} else {
	    print "$friends{$user},";
	}
    }
}
print "\n";
close(WEILL);

    print(STDERR "done\n");

    print(STDERR "Looking at wagner...");

print "wagnw";
open(WAGNER, "ssh wagner who 2>/dev/null |") or die "cannot connect...";
while(<WAGNER>) {
    next if (/^\s*$/);
    ($user,$_,$month,$date,$time,$location) = split(/\s+/);
    if (defined($friends{$user})) {
	if ($location =~ /math/) {
	    print "$friends{$user}(maths),";
	} elsif ($location =~ /wireless/) {
	    print "$friends{$user}(wireless),";
	} else {
	    print "$friends{$user},";
	}
    }    
}
print "\n";
close(WAGNER);

    print(STDERR "done\n");

    print(STDERR "Looking at williams...");

print "willw";

open(WILLIAMS, "ssh williams who 2>/dev/null|") or die "cannot connect...";
while(<WILLIAMS>) {
    next if (/^\s*$/);
    ($user,$_,$month,$date,$time,$location) = split(/\s+/);
    if (defined($friends{$user})) {
	if ($location =~ /math/) {
	    print "$friends{$user}(maths),";
	} elsif ($location =~ /wireless/) {
	    print "$friends{$user}(wireless),";
	} else {
	    print "$friends{$user},";
	}
    }
}
print "\n";
close(WILLIAMS);

    print(STDERR "done\n");
