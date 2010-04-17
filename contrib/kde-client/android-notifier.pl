#!/usr/bin/perl
use Net::DBus;
use IO::Socket;
# Localisation
my %titles = (
	'PING'    => 'New Ping',
	'RING'    => 'New incoming call',
	'SMS'     => 'New SMS',
	'MMS'     => 'New MMS',
	'BATTERY' => 'Battery message',
	'START'   => 'Android notifier'
);
$startmes = 'Android notifier was started';

my $bus     = Net::DBus->session;
my $service = $bus->get_service("org.kde.knotify");
my $object  = $service->get_object( "/Notify", "org.kde.KNotify" );

my $socket = IO::Socket::INET->new(
	Proto     => 'udp',
	Type      => SOCK_DGRAM,
	LocalPort => 10600
) || die "error: failed to create broadcast udp socket - $!";

$object->event(
	"warning", "kde", [], $titles{'START'},
	$startmes, [ 0, 0, 0, 0 ], [], 10000,
	0
);
while ( $socket->recv( my $data, 200 ) ) {
	my ( $dev, $id, $type, $mes ) = split( '/', $data, 4 );
	print $type, ': ', $mes . "\n";
	$object->event(
		"warning", "kde", [], $titles{$type},
		$mes, [ 0, 0, 0, 0 ], [], 10000,
		0
	);
}
