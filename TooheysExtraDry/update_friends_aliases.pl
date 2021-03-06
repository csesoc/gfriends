#!/usr/local/bin/perl

sub trim($)
{
    my $string = shift;
    $string =~ s/^\s+//;
    $string =~ s/\s+$//;
    return $string;
}


$friends="$ENV{HOME}/.friends";

system("cp $friends /tmp/.friends.bak");
system("chmod 777 /tmp/.friends.bak");

# as far as I know the file shoudld exist because the caller 
#  script will create it if it dosen't exist
open(RC,"<$friends") or die "ERROR: could not open your ~/.friends: $!";

while (<RC>) {

    print "$_";
    
    # skip blank lines
    next if (m/^\s*$/);
    
    # get 'name=login, nickname' lines
    if (m/\s*name\s*=\s*(\S+)\s*,\s*(.*)/) {
	$username = $1;
	$nickname = $2;
	$cur_login = $username;
	
	# use the $username to get the newest username
	open(MAIN_USERNAME, "/home/stevec/TooheysExtraDry/user_aliases.pl $username | head -n 1|");	
	while (<MAIN_USERNAME>) {$cur_login = $_;}
	close(MAIN_USERNAME);
	       
	$cur_login = trim($cur_login);       

	# if the user's newest login isn't in the ~/.friends file
	if (system("[[ `cat /tmp/.friends.bak | grep -e \"\b$cur_login\b\" | wc -l` == \"0\" ]]") == 0) {
	    print "name = $cur_login, $nickname\n";	    
	}
    }
}

close(RC);

system("rm -f /tmp/.friends.bak");
