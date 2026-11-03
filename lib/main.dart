import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:share_plus/share_plus.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

const supabaseUrl = 'https://etrzfmyskwgiiylxpmdr.supabase.co';
const supabasePublishableKey = 'sb_publishable_wafxsHT7_05JD0QcfQiF5Q_SxZ9JU5M';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Supabase.initialize(url: supabaseUrl, publishableKey: supabasePublishableKey);
  runApp(const PashtoPoetryApp());
}

final supabase = Supabase.instance.client;

class PashtoPoetryApp extends StatelessWidget {
  const PashtoPoetryApp({super.key});

  @override
  Widget build(BuildContext context) {
    const green = Color(0xFF174B3A);
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'پښتو شاعري',
      theme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(seedColor: green, brightness: Brightness.light),
        scaffoldBackgroundColor: const Color(0xFFF8F6F0),
        fontFamily: 'Roboto',
        appBarTheme: const AppBarTheme(backgroundColor: Color(0xFFF8F6F0), foregroundColor: green, elevation: 0),
        cardTheme: CardThemeData(color: Colors.white, elevation: 1, shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(18))),
      ),
      home: const PoetryHome(),
    );
  }
}

class PoetryHome extends StatefulWidget {
  const PoetryHome({super.key});
  @override
  State<PoetryHome> createState() => _PoetryHomeState();
}

class _PoetryHomeState extends State<PoetryHome> {
  int tab = 0;
  String query = '';
  String categoryId = 'all';
  final searchController = TextEditingController();
  final Set<String> favorites = {};
  List<Map<String, dynamic>> poems = [];
  List<Map<String, dynamic>> categories = [];
  bool loading = true;
  String? error;

  @override
  void initState() {
    super.initState();
    _loadFavorites();
    _loadData();
  }

  @override
  void dispose() {
    searchController.dispose();
    super.dispose();
  }

  Future<void> _loadFavorites() async {
    final prefs = await SharedPreferences.getInstance();
    if (mounted) setState(() => favorites.addAll(prefs.getStringList('favorite_poems') ?? []));
  }

  Future<void> _loadData() async {
    setState(() { loading = true; error = null; });
    try {
      final results = await Future.wait([
        supabase.from('poetry_categories').select().order('sort_order'),
        supabase.from('poems').select('id,title,poet_name,poem_text,category_id,tags,is_featured,created_at').eq('is_published', true).order('created_at', ascending: false),
      ]);
      if (!mounted) return;
      setState(() {
        categories = List<Map<String, dynamic>>.from(results[0]);
        poems = List<Map<String, dynamic>>.from(results[1]);
        loading = false;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() { loading = false; error = 'له سرور سره اړیکه ټینګه نه شوه. انټرنېټ وګورئ او بیا هڅه وکړئ.'; });
    }
  }

  Future<void> _toggleFavorite(String id) async {
    setState(() {
      if (!favorites.add(id)) favorites.remove(id);
    });
    final prefs = await SharedPreferences.getInstance();
    await prefs.setStringList('favorite_poems', favorites.toList());
  }

  List<Map<String, dynamic>> get visiblePoems {
    Iterable<Map<String, dynamic>> items = poems;
    if (tab == 2) items = items.where((p) => favorites.contains(p['id'].toString()));
    if (categoryId != 'all') items = items.where((p) => p['category_id']?.toString() == categoryId);
    if (query.trim().isNotEmpty) {
      final q = query.trim().toLowerCase();
      items = items.where((p) => ['title', 'poet_name', 'poem_text'].any((k) => (p[k] ?? '').toString().toLowerCase().contains(q)));
    }
    return items.toList();
  }

  @override
  Widget build(BuildContext context) {
    return Directionality(
      textDirection: TextDirection.rtl,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('پښتو شاعري', style: TextStyle(fontWeight: FontWeight.w800, fontSize: 23)),
          actions: [IconButton(tooltip: 'تازه کول', onPressed: _loadData, icon: const Icon(Icons.refresh_rounded))],
        ),
        body: RefreshIndicator(
          onRefresh: _loadData,
          child: CustomScrollView(slivers: [
            SliverToBoxAdapter(child: _header()),
            if (tab == 0 && query.isEmpty) SliverToBoxAdapter(child: _featuredSection()),
            SliverToBoxAdapter(child: _search()),
            if (tab == 1) SliverToBoxAdapter(child: _categoryChips()),
            SliverToBoxAdapter(child: Padding(
              padding: const EdgeInsets.fromLTRB(18, 20, 18, 10),
              child: Row(children: [
                Text(tab == 2 ? 'خوښ شوي شعرونه' : tab == 1 ? 'د کټګورۍ شعرونه' : 'ټول شعرونه', style: const TextStyle(fontSize: 19, fontWeight: FontWeight.bold)),
                const Spacer(),
                Text('${visiblePoems.length} شعرونه', style: TextStyle(color: Colors.grey.shade600, fontSize: 12)),
              ]),
            )),
            if (loading) const SliverFillRemaining(child: Center(child: CircularProgressIndicator()))
            else if (error != null) SliverFillRemaining(child: _message(error!, Icons.cloud_off_rounded, action: _loadData))
            else if (visiblePoems.isEmpty) SliverFillRemaining(child: _message(tab == 2 ? 'تر اوسه مو کوم شعر خوښ کړی نه دی' : 'تر اوسه شعرونه نه دي اضافه شوي', Icons.menu_book_rounded))
            else SliverPadding(
              padding: const EdgeInsets.fromLTRB(16, 0, 16, 24),
              sliver: SliverList(delegate: SliverChildBuilderDelegate(
                (context, index) => _poemCard(visiblePoems[index]),
                childCount: visiblePoems.length,
              )),
            ),
          ]),
        ),
        bottomNavigationBar: NavigationBar(
          selectedIndex: tab,
          onDestinationSelected: (v) => setState(() { tab = v; categoryId = 'all'; }),
          destinations: const [
            NavigationDestination(icon: Icon(Icons.auto_stories_outlined), selectedIcon: Icon(Icons.auto_stories), label: 'شعرونه'),
            NavigationDestination(icon: Icon(Icons.category_outlined), selectedIcon: Icon(Icons.category), label: 'کټګورۍ'),
            NavigationDestination(icon: Icon(Icons.favorite_border), selectedIcon: Icon(Icons.favorite), label: 'خوښې'),
          ],
        ),
      ),
    );
  }

  Widget _header() => Container(
    margin: const EdgeInsets.fromLTRB(16, 8, 16, 12),
    padding: const EdgeInsets.all(22),
    decoration: BoxDecoration(
      borderRadius: BorderRadius.circular(24),
      gradient: const LinearGradient(colors: [Color(0xFF174B3A), Color(0xFF28745A)], begin: Alignment.topRight, end: Alignment.bottomLeft),
    ),
    child: const Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
      Text('د پښتو ادب ښکلی جهان', style: TextStyle(color: Colors.white, fontSize: 22, fontWeight: FontWeight.bold)),
      SizedBox(height: 8),
      Text('شعرونه ولولئ، له شاعرانو سره مینه تازه کړئ.', style: TextStyle(color: Colors.white70, fontSize: 14)),
      SizedBox(height: 16),
      Row(children: [Icon(Icons.menu_book_rounded, color: Colors.white70), SizedBox(width: 8), Text('پښتو ژبه، پښتو احساس', style: TextStyle(color: Colors.white, fontWeight: FontWeight.w600))]),
    ]),
  );

  void _openPoem(Map<String, dynamic> poem) {
    final id = poem['id'].toString();
    Navigator.push(context, MaterialPageRoute(builder: (_) => PoemDetail(
      poem: poem,
      initiallyLiked: favorites.contains(id),
      onFavorite: () => _toggleFavorite(id),
    )));
  }

  Widget _featuredSection() {
    final featured = poems.where((p) => p['is_featured'] == true).take(5).toList();
    if (featured.isEmpty) return const SizedBox.shrink();
    return Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
      const Padding(
        padding: EdgeInsets.fromLTRB(18, 20, 18, 10),
        child: Row(children: [
          Icon(Icons.auto_awesome_rounded, color: Color(0xFF28745A), size: 20),
          SizedBox(width: 8),
          Text('غوره شوي شعرونه', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
        ]),
      ),
      SizedBox(
        height: 154,
        child: ListView.separated(
          padding: const EdgeInsets.symmetric(horizontal: 16),
          scrollDirection: Axis.horizontal,
          itemCount: featured.length,
          separatorBuilder: (_, __) => const SizedBox(width: 10),
          itemBuilder: (context, index) {
            final poem = featured[index];
            return SizedBox(
              width: 260,
              child: Card(
                clipBehavior: Clip.antiAlias,
                child: InkWell(
                  onTap: () => _openPoem(poem),
                  child: Padding(
                    padding: const EdgeInsets.all(15),
                    child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                      const Icon(Icons.format_quote_rounded, color: Color(0xFF28745A)),
                      const SizedBox(height: 5),
                      Text((poem['title'] ?? 'غوره شوی شعر').toString(), maxLines: 1, overflow: TextOverflow.ellipsis, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                      const SizedBox(height: 5),
                      Expanded(child: Text((poem['poem_text'] ?? '').toString(), maxLines: 2, overflow: TextOverflow.ellipsis, style: const TextStyle(height: 1.7))),
                      Text((poem['poet_name'] ?? 'نامعلوم شاعر').toString(), style: const TextStyle(color: Color(0xFF28745A), fontSize: 12, fontWeight: FontWeight.w600)),
                    ]),
                  ),
                ),
              ),
            );
          },
        ),
      ),
    ]);
  }

  Widget _search() => Padding(
    padding: const EdgeInsets.symmetric(horizontal: 16),
    child: TextField(
      controller: searchController,
      onChanged: (v) => setState(() => query = v),
      decoration: InputDecoration(
        hintText: 'شعر، شاعر یا کلیمه ولټوئ...',
        prefixIcon: const Icon(Icons.search),
        suffixIcon: query.isEmpty ? null : IconButton(onPressed: () { searchController.clear(); setState(() => query = ''); }, icon: const Icon(Icons.close)),
        filled: true, fillColor: Colors.white,
        border: OutlineInputBorder(borderRadius: BorderRadius.circular(16), borderSide: BorderSide.none),
      ),
    ),
  );

  Widget _categoryChips() => Padding(
    padding: const EdgeInsets.only(top: 14),
    child: SizedBox(height: 44, child: ListView(
      scrollDirection: Axis.horizontal,
      padding: const EdgeInsets.symmetric(horizontal: 16),
      children: [
        _categoryChip('ټولې کټګورۍ', 'all'),
        ...categories.map((c) => _categoryChip((c['name'] ?? '').toString(), c['id'].toString())),
      ],
    )),
  );

  Widget _categoryChip(String name, String id) => Padding(
    padding: const EdgeInsets.only(left: 8),
    child: ChoiceChip(label: Text(name), selected: categoryId == id, onSelected: (_) => setState(() => categoryId = id)),
  );

  Widget _poemCard(Map<String, dynamic> poem) {
    final id = poem['id'].toString();
    final liked = favorites.contains(id);
    final text = (poem['poem_text'] ?? '').toString();
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: InkWell(
        borderRadius: BorderRadius.circular(18),
        onTap: () => _openPoem(poem),
        child: Padding(
          padding: const EdgeInsets.all(17),
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Row(children: [
              const Icon(Icons.format_quote_rounded, color: Color(0xFF28745A)),
              const SizedBox(width: 8),
              Expanded(child: Text((poem['title'] ?? 'بې سرلیکه شعر').toString(), style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 17))),
              IconButton(onPressed: () => _toggleFavorite(id), icon: Icon(liked ? Icons.favorite : Icons.favorite_border, color: liked ? Colors.red : Colors.grey)),
            ]),
            const SizedBox(height: 6),
            Text(text, maxLines: 3, overflow: TextOverflow.ellipsis, style: const TextStyle(height: 1.9, fontSize: 15)),
            const SizedBox(height: 10),
            Row(children: [
              const Icon(Icons.person_outline, size: 16, color: Colors.grey),
              const SizedBox(width: 5),
              Text((poem['poet_name'] ?? 'نامعلوم شاعر').toString(), style: TextStyle(color: Colors.grey.shade600, fontSize: 12)),
              const Spacer(),
              const Text('بشپړ شعر', style: TextStyle(color: Color(0xFF28745A), fontWeight: FontWeight.bold, fontSize: 12)),
              const Icon(Icons.chevron_left, size: 17, color: Color(0xFF28745A)),
            ]),
          ]),
        ),
      ),
    );
  }

  Widget _message(String text, IconData icon, {Future<void> Function()? action}) => Center(
    child: Padding(padding: const EdgeInsets.all(28), child: Column(mainAxisAlignment: MainAxisAlignment.center, children: [
      Icon(icon, size: 48, color: Colors.grey.shade400),
      const SizedBox(height: 12),
      Text(text, textAlign: TextAlign.center, style: TextStyle(color: Colors.grey.shade700, fontSize: 15)),
      if (action != null) ...[const SizedBox(height: 12), FilledButton.icon(onPressed: action, icon: const Icon(Icons.refresh), label: const Text('بیا هڅه وکړئ'))],
    ])),
  );
}

class PoemDetail extends StatefulWidget {
  final Map<String, dynamic> poem;
  final bool initiallyLiked;
  final VoidCallback onFavorite;
  const PoemDetail({super.key, required this.poem, required this.initiallyLiked, required this.onFavorite});

  @override
  State<PoemDetail> createState() => _PoemDetailState();
}

class _PoemDetailState extends State<PoemDetail> {
  late bool liked;

  @override
  void initState() {
    super.initState();
    liked = widget.initiallyLiked;
  }

  @override
  Widget build(BuildContext context) {
    final poem = widget.poem;
    final title = (poem['title'] ?? '').toString();
    final poet = (poem['poet_name'] ?? 'نامعلوم شاعر').toString();
    final body = (poem['poem_text'] ?? '').toString();
    return Directionality(
      textDirection: TextDirection.rtl,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('د شعر لوستل'),
          actions: [
            IconButton(onPressed: () { setState(() => liked = !liked); widget.onFavorite(); }, icon: Icon(liked ? Icons.favorite : Icons.favorite_border, color: liked ? Colors.red : null)),
            IconButton(onPressed: () { Clipboard.setData(ClipboardData(text: '$title\n\n$body\n\n$poet')); ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('شعر کاپي شو'))); }, icon: const Icon(Icons.copy)),
            IconButton(onPressed: () => Share.share('$title\n\n$body\n\n$poet'), icon: const Icon(Icons.share)),
          ],
        ),
        body: ListView(padding: const EdgeInsets.all(22), children: [
          const Icon(Icons.auto_stories_rounded, size: 42, color: Color(0xFF28745A)),
          const SizedBox(height: 18),
          Text(title, textAlign: TextAlign.center, style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold, height: 1.6)),
          const SizedBox(height: 8),
          Text('— $poet', textAlign: TextAlign.center, style: TextStyle(color: Colors.grey.shade600)),
          const Divider(height: 36),
          SelectableText(body, textAlign: TextAlign.center, style: const TextStyle(fontSize: 19, height: 2.25, letterSpacing: .2)),
          const SizedBox(height: 36),
        ]),
      ),
    );
  }
}
