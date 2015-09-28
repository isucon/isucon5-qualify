package Kossy::Validator;

use 5.008005;
use strict;
use warnings;
use Hash::MultiValue;

our $VERSION = "0.01";

our %VALIDATOR = (
    NOT_NULL => sub {
        my ($req,$val) = @_;
        return if not defined($val);
        return if $val eq "";
        return 1;
    },
    CHOICE => sub {
        my ($req, $val, @args) = @_;
        for my $c (@args) {
            if ($c eq $val) {
                return 1;
            }
        }
        return;
    },
    INT => sub {
        my ($req,$val) = @_;
        return if not defined($val);
        $val =~ /^\-?[\d]+$/;
    },
    UINT => sub {
        my ($req,$val) = @_;
        return if not defined($val);
        $val =~ /^\d+$/;  
    },
    NATURAL => sub {
        my ($req,$val) = @_;
        return if not defined($val);
        $val =~ /^\d+$/ && $val > 0;
    },
    FLOAT => sub {
        my ($req,$val) = @_;
        return if not defined($val);
        $val =~ /^\-?(\d+\.?\d*|\.\d+)(e[+-]\d+)?$/;
    },
    DOUBLE => sub {
        my ($req,$val) = @_;
        return if not defined($val);
        $val =~ /^\-?(\d+\.?\d*|\.\d+)(e[+-]\d+)?$/;
    },
    REAL => sub {
        my ($req,$val) = @_;
        return if not defined($val);
        $val =~ /^\-?(\d+\.?\d*|\.\d+)(e[+-]\d+)?$/;
    },
    '@SELECTED_NUM' => sub {
        my ($req,$vals,@args) = @_;
        my ($min,$max) = @args;
        scalar(@$vals) >= $min && scalar(@$vals) <= $max
    },
    '@SELECTED_UNIQ' => sub {
        my ($req,$vals) = @_;
        my %vals;
        $vals{$_} = 1 for @$vals;
        scalar(@$vals) == scalar keys %vals;
    },
);

sub check {
    my $class = shift;

    my $req = shift;
    my $rule = shift || [];

    my @errors;
    my $valid = Hash::MultiValue->new;

    for ( my $i=0; $i < @$rule; $i = $i+2 ) {
        my $param = $rule->[$i];
        my $constraints;
        my $param_name = $param;
        $param_name =~ s!^@!!;
        my @vals = $req->param($param_name);
        my $vals = ( $param =~ m!^@! ) ? \@vals : [$vals[-1]];

        if ( ref($rule->[$i+1]) && ref($rule->[$i+1]) eq 'HASH' ) {
            if ( $param !~ m!^@! && !$VALIDATOR{NOT_NULL}->($req,$vals->[0])  && exists $rule->[$i+1]->{default} ) {
                my $default = $rule->[$i+1]->{default};
                $default = $default->() if ref($default) && ref($default) eq 'CODE';
                $vals = [$default];
            }
            $constraints = $rule->[$i+1]->{rule};
        }
        else {
            $constraints = $rule->[$i+1];
        }

        my $error;
        PARAM_CONSTRAINT: for my $constraint ( @$constraints ) {
            if ( ref($constraint->[0]) eq 'ARRAY' ) {
                my @constraint = @{$constraint->[0]};
                my $constraint_name = shift @constraint;
                if ( ref($constraint_name) && ref($constraint_name) eq 'CODE' ) {
                    for my $val ( @$vals ) {
                        if ( !$constraint_name->($req, $val, @constraint) ) {
                            push @errors, { param => $param_name, message => $constraint->[1] };
                            $error=1;
                            last PARAM_CONSTRAINT;
                        }
                    }
                    next PARAM_CONSTRAINT;
                }
                die "constraint:$constraint_name not found" if ! exists $VALIDATOR{$constraint_name};
                if ( $constraint_name =~ m!^@! ) {
                    if ( !$VALIDATOR{$constraint_name}->($req,$vals,@constraint) ) {
                        push @errors, { param => $param_name, message => $constraint->[1] };
                        $error=1;
                        last PARAM_CONSTRAINT;
                    }                    
                }
                else {
                    for my $val ( @$vals ) {
                        if ( !$VALIDATOR{$constraint_name}->($req,$val,@constraint) ) {
                            push @errors, { param => $param_name, message => $constraint->[1] };
                            $error=1;
                            last PARAM_CONSTRAINT;
                        }
                    }
                }
            }
            elsif ( ref($constraint->[0]) eq 'CODE' ) {
                for my $val ( @$vals ) {
                    if ( !$constraint->[0]->($req, $val) ) {
                        push @errors, { param => $param_name, message => $constraint->[1] };
                        $error=1;
                        last PARAM_CONSTRAINT;
                    }
                }
            }
            else {
                die "constraint:".$constraint->[0]." not found" if ! exists $VALIDATOR{$constraint->[0]};
                if ( $constraint->[0] =~ m!^@! ) {
                    if ( !$VALIDATOR{$constraint->[0]}->($req,$vals) ) {
                        push @errors, { param => $param_name, message => $constraint->[1] };
                        $error=1;
                        last PARAM_CONSTRAINT;
                    }                    
                }
                else {
                    for my $val ( @$vals ) {
                        if ( !$VALIDATOR{$constraint->[0]}->($req, $val) ) {
                            push @errors, { param => $param_name, message => $constraint->[1] };
                            $error=1;
                            last PARAM_CONSTRAINT;
                        }
                    }
                }
            }
        }
        $valid->add($param_name,@$vals) unless $error;
    }
    
    Kossy::Validator::Result->new(\@errors,$valid);
}

package Kossy::Validator::Result;

use strict;
use warnings;

sub new {
    my $class = shift;
    my $errors = shift;
    my $valid = shift;
    bless {errors=>$errors,valid=>$valid}, $class;
}

sub has_error {
    my $self = shift;
    return 1 if @{$self->{errors}};
    return;
}

sub messages {
    my $self = shift;
    my @errors = map { $_->{message} } @{$self->{errors}};
    \@errors;
}

sub errors {
    my $self = shift;
    my %errors = map { $_->{param} => $_->{message} } @{$self->{errors}};
    \%errors;
}

sub valid {
    my $self = shift;
    if ( @_ == 2 ) {
        $self->{valid}->add(@_);
        return $_[1];
    }
    elsif ( @_ == 1 ) {
        return $self->{valid}->get($_[0]) if ! wantarray;
        return $self->{valid}->get_all($_[0]);
    }
    $self->{valid};
}


1;
__END__

=encoding utf-8

=head1 NAME

Kossy::Validator - form validator

=head1 SYNOPSIS

  use Kossy::Validator;
  
  my $req = Plack::Request->new($env);
  
  my $result = Kossy::Validator->check($req, [
        'q' => [['NOT_NULL','query must be defined']],
        'level' => {
            default => 'M', # or sub { 'M' }
            rule => [
                [['CHOICE',qw/L M Q H/],'invalid level char'],
            ],
        },
        '@area' => {
            rule => [
                ['UINT','area must be uint'],
                [['CHOICE', (0..40)],'invalid area'],
            ],
        },
  ]);

  $result->has_error:Flag
  $result->messages:ArrayRef[`Str]

  my $val = $result->valid('q');
  my @val = $result->valid('area');

  my $hash = $result->valid:Hash::MultiValue;


=head1 DESCRIPTION

minimalistic form validator used in L<Kossy>

=head1 VALIDATORS

=over 4

=item NOT_NULL

=item CHOICE

  ['CHOICE',qw/dog cat/]

=item INT

int

=item UINT

unsigned int

=item NATURAL

natural number

=item REAL, DOUBLE, FLOAT

floating number

=item @SELECTED_NUM

  ['@SELECTED_NUM',min,max]

=item @SELECTED_UNIQ

all selected values are unique

=back

=head1 CODEref VALIDATOR

  my $result = Kossy::Validator->check($req,[
      'q' => [
          [sub{
              my ($req,$val) = @_;
          },'invalid']
      ],
  ]);
  
  my $result = Kossy::Validator->check($req,[
      'q' => [
          [[sub{
              my ($req,$val,@args) = @_;
          },0,1],'invalid']
      ],
  ]);

=head1 ADDING VALIDATORS

add to %Kossy::Validator::VALIDATOR

  local $Kossy::Validator::VALIDATOR{MYRULE} = sub {
      my ($req, $val, @args) = @_;
      return 1;
  };

  local $Kossy::Validator::VALIDATOR{'@MYRULE2'} = sub {
      my ($req, $vals, $num) = @_;
      return if @$vals != $num;
      return if uniq(@$vals) == $num;
  };

  Kossy::Validator->check($req,[
      key1 => [['MYRULE','my rule']],
      '@key2' => {
         rule => [
             [['@MYRULE2',3], 'select 3 items'],
             [['CHOICE',qw/1 2 3 4 5/], 'invalid']
         ],
      }
  ]);

if rule name start with '@', all values are passed as ArrayRef instead of last value.

=head1 SEE ALSO

L<Kossy>

=head1 LICENSE

Copyright (C) Masahiro Nagano.

This library is free software; you can redistribute it and/or modify
it under the same terms as Perl itself.

=head1 AUTHOR

Masahiro Nagano E<lt>kazeburo@gmail.comE<gt>

=cut

