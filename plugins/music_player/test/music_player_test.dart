import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:music_player/music_player.dart';

void main() {
  const MethodChannel channel = MethodChannel('music_player');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await MusicPlayer.platformVersion, '42');
  });
}
