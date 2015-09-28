package Kossy::Exception;

use strict;
use warnings;
use HTTP::Status;
use Text::Xslate qw/html_escape/;
use Kossy::Response;

our $VERSION = '0.38';

sub new {
    my $class = shift;
    my $code = shift;
    my %args = (
        code => $code,
    );
    if ( @_ == 1 ) {
        $args{message} = shift;
    }
    elsif ( @_ % 2 == 0) {
        %args = (
            %args,
            @_
        );
    }
    bless \%args, $class;
}

sub response {
    my $self = shift;
    my $code = $self->{code} || 500;
    my $message = $self->{message};
    $message ||= HTTP::Status::status_message($code);

    my @headers = (
         'Content-Type' => q!text/html; charset=UTF-8!,
    );

    if ($code =~ /^3/ && (my $loc = eval { $self->{location} })) {
        push(@headers, Location => $loc);
    }

    #return Kossy::Response->new($code, \@headers, [$self->html($code,$message)])->finalize;
    return Kossy::Response->new($code, \@headers, [$message])->finalize;
}

sub html {
    my $self = shift;
    my ($code,$message) = @_;
    $code = html_escape($code);
    $message = html_escape($message);
    return <<EOF;
<!doctype html>
<html>
<head>
<meta charset=utf-8 />
<style type="text/css">
.message {
  font-size: 200%;
  margin: 20px 20px;
  color: #666;
}
.message strong {
  font-size: 250%;
  font-weight: bold;
  color: #333;
}
</style>
</head>
<body>
<p class="message">
<strong>$code</strong> $message
</p>
</div>
</body>
</html>
EOF
}

1;
