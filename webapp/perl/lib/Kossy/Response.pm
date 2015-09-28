package Kossy::Response;

use strict;
use warnings;
use parent qw/Plack::Response/;
use Encode;
use HTTP::Headers::Fast;
use Cookie::Baker;

our $VERSION = '0.38';

sub headers {
    my $self = shift;

    if (@_) {
        my $headers = shift;
        if (ref $headers eq 'ARRAY') {
            Carp::carp("Odd number of headers") if @$headers % 2 != 0;
            $headers = HTTP::Headers::Fast->new(@$headers);
        } elsif (ref $headers eq 'HASH') {
            $headers = HTTP::Headers::Fast->new(%$headers);
        }
        return $self->{headers} = $headers;
    } else {
        return $self->{headers} ||= HTTP::Headers::Fast->new();
    }
}

sub _body {
    my $self = shift;
    my $body = $self->body;
       $body = [] unless defined $body;
    if (!ref $body or Scalar::Util::blessed($body) && overload::Method($body, q("")) && !$body->can('getline')) {
        return [ Encode::encode_utf8($body) ] if Encode::is_utf8($body);
        return [ $body ];
    } else {
        return $body;
    }
}

sub finalize {
    my $self = shift;
    Carp::croak "missing status" unless $self->status();
    my @headers;
    $self->headers->scan(sub{
        my ($k,$v) = @_;
        return if $k eq 'X-XSS-Protection';
        $v =~ s/\015\012[\040|\011]+/chr(32)/ge; # replace LWS with a single SP
        $v =~ s/\015|\012//g; # remove CR and LF since the char is invalid here
        push @headers, $k, $v;
    });

    while (my($name, $val) = each %{$self->cookies}) {
        my $cookie = bake_cookie($name, $val);
        push @headers, 'Set-Cookie' => $cookie;
    }

    push @headers, 'X-XSS-Protection' => 1;

    return [
        $self->status,
        \@headers,
        $self->_body,
    ];
}

1;
