package Kossy::Connection;

use strict;
use warnings;
use Class::Accessor::Lite (
    new => 1,
    rw => [qw/req res stash args tx debug/]
);
use JSON qw//;
use Kossy::Exception;

our $VERSION = '0.38';
my $_JSON = JSON->new()->allow_blessed(1)->convert_blessed(1)->ascii(1);

# for IE7 JSON venularity.
# see http://www.atmarkit.co.jp/fcoding/articles/webapp/05/webapp05a.html
# Copy from Amon2::Plugin::Web::JSON => Fixed to escape only string parts
my %_ESCAPE = (
    '+' => '\\u002b', # do not eval as UTF-7
    '<' => '\\u003c', # do not eval as HTML
    '>' => '\\u003e', # ditto.
);
sub escape {
    my $self = shift;
    my $body = shift;
    $body =~ s!([+<>])!$_ESCAPE{$1}!g;
    return qq("$body");
}
sub escape_json {
    my $self = shift;
    my $body = shift;
    # escape only string parts
    $body =~ s/"((?:\\"|[^"])*)"/$self->escape($1)/eg;
    return $body;
}

*request = \&req;
*response = \&res;

sub env {
    $_[0]->{req}->env;
}

sub halt {
    my $self = shift;
    die Kossy::Exception->new(@_);
}

sub redirect {
    my $self = shift;
    $self->res->redirect(@_);
    $self->res;
}

sub render {
    my $self = shift;
    my $file = shift;
    my %args = ( @_ && ref $_[0] ) ? %{$_[0]} : @_;
    my %vars = (
        c => $self,
        stash => $self->stash,
        %args,
    );

    my $body = $self->tx->render($file, \%vars);
    $self->res->status( 200 );
    $self->res->content_type('text/html; charset=UTF-8');
    $self->res->body( $body );
    $self->res;
}

sub render_json {
    my $self = shift;
    my $obj = shift;

    # defense from JSON hijacking
    # Copy from Amon2::Plugin::Web::JSON
    if ( !$self->req->header('X-Requested-With')  && 
         ($self->req->user_agent||'') =~ /android/i &&
         defined $self->req->header('Cookie') &&
         ($self->req->method||'GET') eq 'GET'
    ) {
        $self->halt(403,"Your request is maybe JSON hijacking.\nIf you are not a attacker, please add 'X-Requested-With' header to each request.");
    }

    my $body = $_JSON->encode($obj);
    $body = $self->escape_json($body);

    if ( ( $self->req->user_agent || '' ) =~ m/Safari/ ) {
        $body = "\xEF\xBB\xBF" . $body;
    }

    $self->res->status( 200 );
    $self->res->content_type('application/json; charset=UTF-8');
    $self->res->header( 'X-Content-Type-Options' => 'nosniff' ); # defense from XSS
    $self->res->body( $body );
    $self->res;    
}



1;

