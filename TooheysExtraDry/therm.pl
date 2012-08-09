#!/usr/local/bin/perl 

@labs=("moog","bell","spoons","bugle","pipe","drum","tuba","harp","oboe","leaf","oud","clavier","organ","piano");

foreach $lab (@labs) {
    
    open(THERM, "tail -n 1 ~status/temperature/lab-$lab|") or die "could not do";
    
    while (<THERM>) {
	if (m/^\d+\s(\d*[.]\d)/) {
	    print "$lab = $1\n";
	}
	close(THERM);
    }
    
}

