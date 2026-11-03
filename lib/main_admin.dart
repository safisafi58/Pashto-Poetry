import 'package:flutter/material.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'main.dart' as poetry;

const _ink = Color(0xFF171A20);
const _paper = Color(0xFFF3EEE4);
const _gold = Color(0xFFC9A96E);

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Supabase.initialize(
    url: 'https://etrzfmyskwgiiylxpmdr.supabase.co',
    publishableKey: 'sb_publishable_wafxsHT7_05JD0QcfQiF5Q_SxZ9JU5M',
  );
  runApp(const AdminStandaloneApp());
}

class AdminStandaloneApp extends StatelessWidget {
  const AdminStandaloneApp({super.key});
  @override
  Widget build(BuildContext context) => MaterialApp(
    debugShowCheckedModeBanner: false,
    title: 'Pashto Poetry Admin',
    theme: ThemeData(
      scaffoldBackgroundColor: _paper,
      colorScheme: const ColorScheme.light(primary: _ink, secondary: _gold),
      useMaterial3: false,
    ),
    home: const AdminGate(),
  );
}

class AdminGate extends StatefulWidget {
  const AdminGate({super.key});
  @override
  State<AdminGate> createState() => _AdminGateState();
}

class _AdminGateState extends State<AdminGate> {
  final email = TextEditingController();
  final password = TextEditingController();
  bool busy = false;
  bool loggedIn = false;
  String? message;

  @override
  void initState() {
    super.initState();
    _checkExistingSession();
  }

  Future<void> _checkExistingSession() async {
    final client = Supabase.instance.client;
    if (client.auth.currentSession != null) {
      await client.auth.refreshSession();
      if (client.auth.currentUser?.appMetadata['role'] == 'admin' && mounted) {
        setState(() => loggedIn = true);
      } else {
        await client.auth.signOut();
      }
    }
  }

  Future<void> _signIn() async {
    if (email.text.trim().isEmpty || password.text.isEmpty) {
      setState(() => message = 'برېښنالیک او پټنوم ولیکئ.');
      return;
    }
    setState(() { busy = true; message = null; });
    try {
      final client = Supabase.instance.client;
      await client.auth.signInWithPassword(
        email: email.text.trim(),
        password: password.text,
      );
      await client.auth.refreshSession();
      if (client.auth.currentUser?.appMetadata['role'] != 'admin') {
        await client.auth.signOut();
        setState(() => message = 'دا حساب د اډمین اجازه نه لري.');
      } else {
        setState(() => loggedIn = true);
      }
    } on AuthException catch (e) {
      setState(() => message = e.message);
    } catch (_) {
      setState(() => message = 'ننوتل ناکام شول. انټرنېټ وګورئ.');
    } finally {
      if (mounted) setState(() => busy = false);
    }
  }

  @override
  void dispose() {
    email.dispose();
    password.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (loggedIn) {
      return AdminHome(onLogout: () async {
        await Supabase.instance.client.auth.signOut();
        if (mounted) setState(() => loggedIn = false);
      });
    }
    return Directionality(
      textDirection: TextDirection.rtl,
      child: Scaffold(
        backgroundColor: _paper,
        body: SafeArea(
          child: Center(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(25),
              child: ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 430),
                child: Column(
                  children: [
                    Container(
                      width: 82, height: 82,
                      decoration: BoxDecoration(
                        color: _ink, borderRadius: BorderRadius.circular(25),
                      ),
                      child: const Icon(Icons.admin_panel_settings, color: _gold, size: 44),
                    ),
                    const SizedBox(height: 20),
                    const Text('د پښتو شاعرۍ اډمین', style: TextStyle(
                      color: _ink, fontSize: 25, fontWeight: FontWeight.w900,
                    )),
                    const SizedBox(height: 7),
                    const Text('یوازې د مجاز اډمین لپاره', style: TextStyle(color: Colors.black54)),
                    const SizedBox(height: 30),
                    _field(email, 'د اډمین برېښنالیک', false),
                    const SizedBox(height: 13),
                    _field(password, 'پټنوم', true),
                    if (message != null) Padding(
                      padding: const EdgeInsets.only(top: 12),
                      child: Text(message!, style: const TextStyle(color: Colors.red)),
                    ),
                    const SizedBox(height: 22),
                    SizedBox(
                      width: double.infinity, height: 52,
                      child: ElevatedButton(
                        style: ElevatedButton.styleFrom(
                          backgroundColor: _ink, foregroundColor: _gold,
                          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                        ),
                        onPressed: busy ? null : _signIn,
                        child: Text(busy ? 'مهرباني وکړئ...' : 'اډمین ننوتل'),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _field(TextEditingController c, String hint, bool secret) => TextField(
    controller: c, obscureText: secret, textDirection: TextDirection.ltr,
    decoration: InputDecoration(
      hintText: hint, filled: true, fillColor: Colors.white,
      prefixIcon: Icon(secret ? Icons.lock_outline : Icons.email_outlined, color: _gold),
      border: OutlineInputBorder(borderRadius: BorderRadius.circular(14), borderSide: BorderSide.none),
    ),
  );
}

class AdminHome extends StatelessWidget {
  final Future<void> Function() onLogout;
  const AdminHome({super.key, required this.onLogout});
  @override
  Widget build(BuildContext context) => Directionality(
    textDirection: TextDirection.rtl,
    child: Scaffold(
      appBar: AppBar(
        backgroundColor: _ink,
        title: const Text('د اډمین مدیریت', style: TextStyle(color: _paper)),
        iconTheme: const IconThemeData(color: _gold),
        actions: [
          IconButton(
            tooltip: 'وتل',
            onPressed: onLogout,
            icon: const Icon(Icons.logout, color: _gold),
          ),
        ],
      ),
      body: const poetry.AdminPage(),
    ),
  );
}
