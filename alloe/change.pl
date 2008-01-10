#!/usr/bin/perl

open(FILE, "<$ARGV[0]");
while($t = <FILE>) {
    $t =~ s/statements\[([^\]]*)\]/terms.get($1)/g;
    $t =~ s/relations\[([^\]]*)\]/relations.get($1)/g;
    print $t;
}
close(FILE);
