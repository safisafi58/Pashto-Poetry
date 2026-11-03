import 'package:flutter/material.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'main.dart' as poetry;

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Supabase.initialize(
    url: 'https://etrzfmyskwgiiylxpmdr.supabase.co',
    publishableKey: 'sb_publishable_wafxsHT7_05JD0QcfQiF5Q_SxZ9JU5M',
  );
  runApp(const poetry.PoetryApp());
}
