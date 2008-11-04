#!/usr/bin/perl

my %words;

while(<>) {
    $words{$_} = 1;
}


for(keys(%words)) {
    print "$_";
}
