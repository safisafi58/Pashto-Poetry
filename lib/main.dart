import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:share_plus/share_plus.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

const supabaseUrl='https://etrzfmyskwgiiylxpmdr.supabase.co';
const supabasePublishableKey='sb_publishable_wafxsHT7_05JD0QcfQiF5Q_SxZ9JU5M';
const ink=Color(0xFF171A20), ink2=Color(0xFF292D36), paper=Color(0xFFF3EEE4), paper2=Color(0xFFE7DECF), gold=Color(0xFFC9A96E), muted=Color(0xFF85878C);
final supabase=Supabase.instance.client;
Future<void> main() async { WidgetsFlutterBinding.ensureInitialized(); await Supabase.initialize(url:supabaseUrl,publishableKey:supabasePublishableKey); runApp(const PoetryApp()); }
class PoetryApp extends StatelessWidget { const PoetryApp({super.key}); @override Widget build(BuildContext context)=>MaterialApp(debugShowCheckedModeBanner:false,title:'پښتو شاعري',theme:ThemeData(scaffoldBackgroundColor:paper,primaryColor:ink,colorScheme:const ColorScheme.light(primary:ink,secondary:gold),useMaterial3:false),home:const HomePage()); }

class HomePage extends StatefulWidget { const HomePage({super.key}); @override State<HomePage> createState()=>_HomePageState(); }
class _HomePageState extends State<HomePage>{
 int tab=0; String search='',category='all'; final searchCtrl=TextEditingController(); final Set<String> favorites={};
 List<Map<String,dynamic>> poems=[],categories=[]; bool loading=true; String? error;
 @override void initState(){super.initState();loadFavorites();load();}
 @override void dispose(){searchCtrl.dispose();super.dispose();}
 Future<void> loadFavorites()async{final p=await SharedPreferences.getInstance();if(mounted)setState(()=>favorites.addAll(p.getStringList('favorite_poems')??[]));}
 Future<void> load()async{
  if(mounted)setState(() { loading = true; error = null; });
  try{final r=await Future.wait([supabase.from('poetry_categories').select().order('sort_order'),supabase.from('poems').select('id,title,poet_name,poem_text,category_id,tags,is_featured,created_at').eq('is_published',true).order('created_at',ascending:false)]);
   if(!mounted)return;setState(() { categories = List<Map<String,dynamic>>.from(r[0]); poems = List<Map<String,dynamic>>.from(r[1]); loading = false; });
  }catch(_){if(mounted)setState(() { loading = false; error = 'له سرور سره اړیکه ټینګه نه شوه. انټرنېټ وګورئ.'; });}
 }
 Future<void> toggle(String id)async{setState(()=>favorites.contains(id)?favorites.remove(id):favorites.add(id));final p=await SharedPreferences.getInstance();await p.setStringList('favorite_poems',favorites.toList());}
 List<Map<String,dynamic>> get shown{Iterable<Map<String,dynamic>> x=poems;if(tab==2)x=x.where((p)=>favorites.contains(p['id'].toString()));if(tab==1&&category!='all')x=x.where((p)=>p['category_id']?.toString()==category);if(search.trim().isNotEmpty){final q=search.toLowerCase().trim();x=x.where((p)=>['title','poet_name','poem_text'].any((k)=>(p[k]??'').toString().toLowerCase().contains(q)));}return x.toList();}
 void openPoem(Map<String,dynamic> p)=>Navigator.push(context,MaterialPageRoute(builder:(_)=>PoemPage(poem:p,liked:favorites.contains(p['id'].toString()),onLike:()=>toggle(p['id'].toString()))));
 @override Widget build(BuildContext context){final list=shown;return Directionality(textDirection:TextDirection.rtl,child:Scaffold(backgroundColor:paper,
  body:SafeArea(child:RefreshIndicator(color:gold,onRefresh:load,child:CustomScrollView(slivers:[
   SliverToBoxAdapter(child:topBar()),SliverToBoxAdapter(child:hero()),if(tab==0&&search.isEmpty)SliverToBoxAdapter(child:featured()),SliverToBoxAdapter(child:searchBox()),if(tab==1)SliverToBoxAdapter(child:categoryStrip()),
   SliverToBoxAdapter(child:Padding(padding:const EdgeInsets.fromLTRB(20,23,20,11),child:Row(children:[Text(tab==2?'زما خوښې':tab==1?'د شعرونو ټولګه':'وروستي شعرونه',style:const TextStyle(color:ink,fontSize:20,fontWeight:FontWeight.w900)),const Spacer(),Text('${list.length} شعرونه',style:const TextStyle(color:muted,fontSize:11))]))),
   if(loading)const SliverFillRemaining(child:Center(child:CircularProgressIndicator(color:gold))) else if(error!=null)SliverFillRemaining(child:empty(error!,load)) else if(list.isEmpty)SliverFillRemaining(child:empty(tab==2?'تر اوسه مو شعر نه دی خوښ کړی':'تر اوسه شعرونه نه دي خپاره شوي',load)) else SliverPadding(padding:const EdgeInsets.fromLTRB(16,0,16,25),sliver:SliverList(delegate:SliverChildBuilderDelegate((c,i)=>poemTile(list[i]),childCount:list.length)))
  ]))),bottomNavigationBar:bottomBar()));}
 Widget topBar()=>Padding(padding:const EdgeInsets.fromLTRB(19,12,19,7),child:Row(children:[
  Container(width:42,height:42,decoration:BoxDecoration(color:ink,borderRadius:BorderRadius.circular(14)),child:const Icon(Icons.menu_book_rounded,color:gold)),
  const SizedBox(width:10),const Column(crossAxisAlignment:CrossAxisAlignment.start,children:[Text('پښتو شاعري',style:TextStyle(color:ink,fontWeight:FontWeight.w900,fontSize:19)),Text('د ادب او احساس کور',style:TextStyle(color:muted,fontSize:10))]),
  const Spacer(),smallAction(Icons.person_outline,'حساب',()=>Navigator.push(context,MaterialPageRoute(builder:(_)=>const AccountPage())))
 ]));
 Widget smallAction(IconData i,String t,VoidCallback f)=>GestureDetector(onTap:f,child:Container(padding:const EdgeInsets.symmetric(horizontal:10,vertical:10),decoration:BoxDecoration(color:paper2,borderRadius:BorderRadius.circular(12)),child:Row(children:[Icon(i,color:ink,size:17),const SizedBox(width:4),Text(t,style:const TextStyle(color:ink,fontWeight:FontWeight.bold,fontSize:10))])));
 Widget hero()=>Container(margin:const EdgeInsets.fromLTRB(16,12,16,8),padding:const EdgeInsets.all(22),decoration:BoxDecoration(color:ink,borderRadius:BorderRadius.circular(27),gradient:const LinearGradient(colors:[ink2,ink],begin:Alignment.topRight,end:Alignment.bottomLeft)),child:Column(crossAxisAlignment:CrossAxisAlignment.start,children:[
  Container(padding:const EdgeInsets.symmetric(horizontal:11,vertical:6),decoration:BoxDecoration(color:gold.withValues(alpha: .16),borderRadius:BorderRadius.circular(20)),child:const Text('د پښتو ادب خزانه',style:TextStyle(color:gold,fontSize:11,fontWeight:FontWeight.bold))),
  const SizedBox(height:15),const Text('شعر یوازې الفاظ نه دي،\nد زړه خبرې دي.',style:TextStyle(color:paper,fontSize:25,height:1.6,fontWeight:FontWeight.w900)),const SizedBox(height:9),const Text('ولولئ، احساس یې کړئ او له نورو سره یې شریک کړئ.',style:TextStyle(color:Colors.white70,fontSize:12)),const SizedBox(height:17),Row(children:[const Icon(Icons.auto_stories,color:gold,size:17),const SizedBox(width:7),Text('${poems.length} خپاره شوي شعرونه',style:const TextStyle(color:gold,fontSize:11,fontWeight:FontWeight.bold))])
 ]));
 Widget featured(){final f=poems.where((p)=>p['is_featured']==true).take(5).toList();if(f.isEmpty)return const SizedBox.shrink();return Column(crossAxisAlignment:CrossAxisAlignment.start,children:[
  const Padding(padding:EdgeInsets.fromLTRB(20,20,20,10),child:Text('د نن ورځې انتخاب',style:TextStyle(color:ink,fontSize:18,fontWeight:FontWeight.w900))),
  SizedBox(height:160,child:ListView.separated(padding:const EdgeInsets.symmetric(horizontal:16),scrollDirection:Axis.horizontal,itemCount:f.length,separatorBuilder:(_,__)=>const SizedBox(width:10),itemBuilder:(c,i){final p=f[i];return GestureDetector(onTap:()=>openPoem(p),child:Container(width:265,padding:const EdgeInsets.all(16),decoration:BoxDecoration(color:const Color(0xFFE3D8C5),borderRadius:BorderRadius.circular(21),border:Border.all(color:gold.withValues(alpha: .5))),child:Column(crossAxisAlignment:CrossAxisAlignment.start,children:[
   const Row(children:[Icon(Icons.star,color:Color(0xFF947747),size:16),SizedBox(width:5),Text('غوره شوی شعر',style:TextStyle(color:Color(0xFF806438),fontSize:10))]),const SizedBox(height:8),Text((p['title']??'').toString(),maxLines:1,overflow:TextOverflow.ellipsis,style:const TextStyle(color:ink,fontWeight:FontWeight.w900,fontSize:16)),const SizedBox(height:6),Expanded(child:Text((p['poem_text']??'').toString(),maxLines:2,overflow:TextOverflow.ellipsis,style:const TextStyle(color:ink2,height:1.8,fontSize:12))),Text((p['poet_name']??'نامعلوم شاعر').toString(),style:const TextStyle(color:Color(0xFF806438),fontSize:11))
  ])));}))
 ]);}
 Widget searchBox()=>Padding(padding:const EdgeInsets.fromLTRB(16,12,16,0),child:Container(decoration:BoxDecoration(color:Colors.white,borderRadius:BorderRadius.circular(16),border:Border.all(color:paper2)),child:TextField(controller:searchCtrl,onChanged:(v)=>setState(()=>search=v),decoration:InputDecoration(hintText:'د شعر یا شاعر نوم ولټوئ...',hintStyle:const TextStyle(fontSize:12,color:muted),prefixIcon:const Icon(Icons.search,color:gold),suffixIcon:search.isEmpty?null:IconButton(onPressed:(){searchCtrl.clear();setState(()=>search='');},icon:const Icon(Icons.close)),border:InputBorder.none))));
 Widget categoryStrip()=>SizedBox(height:52,child:ListView(scrollDirection:Axis.horizontal,padding:const EdgeInsets.fromLTRB(16,10,16,0),children:[pill('ټولې','all'),...categories.map((c)=>pill(c['name'].toString(),c['id'].toString()))]));
 Widget pill(String n,String id)=>GestureDetector(onTap:()=>setState(()=>category=id),child:Container(margin:const EdgeInsets.only(left:8),padding:const EdgeInsets.symmetric(horizontal:16,vertical:9),decoration:BoxDecoration(color:category==id?ink:paper2,borderRadius:BorderRadius.circular(22)),child:Text(n,style:TextStyle(color:category==id?gold:ink,fontWeight:FontWeight.bold,fontSize:11))));
 Widget poemTile(Map<String,dynamic> p)=>GestureDetector(onTap:()=>openPoem(p),child:Container(margin:const EdgeInsets.only(bottom:11),padding:const EdgeInsets.all(16),decoration:BoxDecoration(color:const Color(0xFFFFFCF6),borderRadius:BorderRadius.circular(20),border:Border.all(color:paper2),boxShadow:[BoxShadow(color:ink.withValues(alpha: .035),blurRadius:10,offset:const Offset(0,4))]),child:Column(crossAxisAlignment:CrossAxisAlignment.start,children:[
  Row(children:[Container(width:31,height:31,decoration:BoxDecoration(color:paper2,borderRadius:BorderRadius.circular(9)),child:const Icon(Icons.format_quote,color:Color(0xFF947747),size:18)),const SizedBox(width:9),Expanded(child:Text((p['title']??'بې سرلیکه').toString(),style:const TextStyle(color:ink,fontWeight:FontWeight.w900,fontSize:16))),GestureDetector(onTap:()=>toggle(p['id'].toString()),child:Icon(favorites.contains(p['id'].toString())?Icons.favorite:Icons.favorite_border,color:favorites.contains(p['id'].toString())?Colors.red:muted))]),
  const SizedBox(height:10),Text((p['poem_text']??'').toString(),maxLines:3,overflow:TextOverflow.ellipsis,style:const TextStyle(color:ink2,fontSize:14,height:1.9)),const SizedBox(height:12),Row(children:[const Icon(Icons.person_outline,color:muted,size:15),const SizedBox(width:5),Text((p['poet_name']??'نامعلوم شاعر').toString(),style:const TextStyle(color:muted,fontSize:11)),const Spacer(),const Text('بشپړ لوستل ←',style:TextStyle(color:Color(0xFF806438),fontWeight:FontWeight.bold,fontSize:11))])
 ])));
 Widget empty(String s,Future<void> Function() retry)=>Center(child:Padding(padding:const EdgeInsets.all(28),child:Column(mainAxisAlignment:MainAxisAlignment.center,children:[const Icon(Icons.auto_stories_outlined,color:gold,size:45),const SizedBox(height:12),Text(s,textAlign:TextAlign.center,style:const TextStyle(color:ink,height:1.8)),const SizedBox(height:14),TextButton(onPressed:retry,child:const Text('بیا هڅه وکړئ',style:TextStyle(color:ink)))])));
 Widget bottomBar()=>Container(padding:const EdgeInsets.fromLTRB(14,9,14,12),decoration:const BoxDecoration(color:ink,borderRadius:BorderRadius.vertical(top:Radius.circular(22))),child:Row(children:[nav(0,Icons.auto_stories_outlined,'شعرونه'),nav(1,Icons.grid_view_rounded,'کټګورۍ'),nav(2,Icons.favorite_border,'خوښې')]));
 Widget nav(int n,IconData i,String t)=>Expanded(child:GestureDetector(onTap:()=>setState(() { tab = n; category = 'all'; }),child:Container(padding:const EdgeInsets.symmetric(vertical:8),decoration:BoxDecoration(color:tab==n?Colors.white.withValues(alpha: .08):Colors.transparent,borderRadius:BorderRadius.circular(13)),child:Column(mainAxisSize:MainAxisSize.min,children:[Icon(i,color:tab==n?gold:Colors.white60,size:21),const SizedBox(height:4),Text(t,style:TextStyle(color:tab==n?gold:Colors.white60,fontSize:10,fontWeight:FontWeight.bold))]))));
}

class PoemPage extends StatefulWidget{final Map<String,dynamic> poem;final bool liked;final VoidCallback onLike;const PoemPage({super.key,required this.poem,required this.liked,required this.onLike});@override State<PoemPage> createState()=>_PoemPageState();}
class _PoemPageState extends State<PoemPage>{late bool liked;@override void initState(){super.initState();liked=widget.liked;}
 @override Widget build(BuildContext context){final p=widget.poem;final title=(p['title']??'').toString(),body=(p['poem_text']??'').toString(),poet=(p['poet_name']??'نامعلوم شاعر').toString();return Directionality(textDirection:TextDirection.rtl,child:Scaffold(backgroundColor:paper,appBar:AppBar(backgroundColor:ink,elevation:0,title:const Text('د شعر پاڼه',style:TextStyle(color:paper)),iconTheme:const IconThemeData(color:gold),actions:[
  IconButton(onPressed:(){setState(()=>liked=!liked);widget.onLike();},icon:Icon(liked?Icons.favorite:Icons.favorite_border,color:gold)),
  IconButton(onPressed:(){Clipboard.setData(ClipboardData(text:'$title\n\n$body\n\n$poet'));ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content:Text('شعر کاپي شو')));},icon:const Icon(Icons.copy,color:gold)),
  IconButton(onPressed:()=>Share.share('$title\n\n$body\n\n$poet'),icon:const Icon(Icons.share,color:gold))
 ]),body:ListView(padding:const EdgeInsets.all(20),children:[Container(padding:const EdgeInsets.fromLTRB(22,28,22,30),decoration:BoxDecoration(color:const Color(0xFFFFFCF6),borderRadius:BorderRadius.circular(24),border:Border.all(color:paper2)),child:Column(children:[const Icon(Icons.format_quote,color:gold,size:42),const SizedBox(height:16),Text(title,textAlign:TextAlign.center,style:const TextStyle(color:ink,fontSize:24,height:1.7,fontWeight:FontWeight.w900)),const SizedBox(height:8),Text('— $poet —',style:const TextStyle(color:Color(0xFF806438))),const Padding(padding:EdgeInsets.symmetric(vertical:22),child:Divider(color:paper2)),SelectableText(body,textAlign:TextAlign.center,style:const TextStyle(color:ink2,fontSize:19,height:2.3))]))])));}}
class AccountPage extends StatefulWidget{const AccountPage({super.key});@override State<AccountPage> createState()=>_AccountPageState();}
class _AccountPageState extends State<AccountPage>{
 @override Widget build(BuildContext context){final u=supabase.auth.currentUser;return Directionality(textDirection:TextDirection.rtl,child:Scaffold(backgroundColor:paper,appBar:AppBar(backgroundColor:ink,title:const Text('زما حساب',style:TextStyle(color:paper)),iconTheme:const IconThemeData(color:gold)),body:Padding(padding:const EdgeInsets.all(22),child:Column(children:[
  const SizedBox(height:25),Container(width:78,height:78,decoration:BoxDecoration(color:ink,borderRadius:BorderRadius.circular(25)),child:const Icon(Icons.person,color:gold,size:42)),const SizedBox(height:14),Text(u?.email??'مېلمه کاروونکی',style:const TextStyle(color:ink,fontSize:18,fontWeight:FontWeight.w900)),const SizedBox(height:7),Text(u==null?'بې له حسابه هم شعرونه لوستلی شئ':'کاروونکی حساب',style:const TextStyle(color:muted,fontSize:12)),const SizedBox(height:27),
  if(u==null)action('ننوتل یا نوی حساب جوړول',Icons.login,()async{await Navigator.push(context,MaterialPageRoute(builder:(_)=>const AuthPage()));if(mounted)setState((){});})
  else ...[action('له حسابه وتل',Icons.logout,()async{await supabase.auth.signOut();if(mounted)setState((){});})],
  const Spacer(),const Text('پښتو شاعري • د پښتو ادب لپاره',style:TextStyle(color:muted,fontSize:11))
 ]))));}
 Widget action(String t,IconData i,VoidCallback f)=>GestureDetector(onTap:f,child:Container(width:double.infinity,margin:const EdgeInsets.only(bottom:10),padding:const EdgeInsets.all(16),decoration:BoxDecoration(color:const Color(0xFFFFFCF6),borderRadius:BorderRadius.circular(16),border:Border.all(color:paper2)),child:Row(children:[Icon(i,color:gold),const SizedBox(width:10),Text(t,style:const TextStyle(color:ink,fontWeight:FontWeight.bold)),const Spacer(),const Icon(Icons.chevron_left,color:muted)])));
}
class AuthPage extends StatefulWidget{const AuthPage({super.key});@override State<AuthPage> createState()=>_AuthPageState();}
class _AuthPageState extends State<AuthPage>{
 final email=TextEditingController(),password=TextEditingController();bool signup=false,busy=false;String? message;
 @override void dispose(){email.dispose();password.dispose();super.dispose();}
 Future<void> submit()async{if(email.text.trim().isEmpty||password.text.length<6){setState(()=>message='برېښنالیک او لږ تر لږه ۶ توري پټنوم ولیکئ.');return;}setState(() { busy = true; message = null; });
  try{if(signup){final r=await supabase.auth.signUp(email:email.text.trim(),password:password.text);if(r.session!=null&&mounted)Navigator.pop(context);else if(mounted)setState(()=>message='حساب جوړ شو؛ که د ایمیل تایید فعال وي، ایمیل تایید کړئ.');}else{await supabase.auth.signInWithPassword(email:email.text.trim(),password:password.text);if(mounted)Navigator.pop(context);}}
  on AuthException catch(e){if(mounted)setState(()=>message=e.message);}catch(_){if(mounted)setState(()=>message='اړیکه ناکامه شوه. بیا هڅه وکړئ.');}finally{if(mounted)setState(()=>busy=false);}
 }
 @override Widget build(BuildContext context)=>Directionality(textDirection:TextDirection.rtl,child:Scaffold(backgroundColor:paper,appBar:AppBar(backgroundColor:ink,title:const Text('کاروونکي حساب',style:TextStyle(color:paper)),iconTheme:const IconThemeData(color:gold)),body:ListView(padding:const EdgeInsets.all(22),children:[
  const SizedBox(height:25),const Icon(Icons.menu_book,color:gold,size:50),const SizedBox(height:12),Text(signup?'نوی حساب جوړ کړئ':'بېرته ښه راغلاست',textAlign:TextAlign.center,style:const TextStyle(color:ink,fontSize:23,fontWeight:FontWeight.w900)),const SizedBox(height:25),
  field(email,'برېښنالیک',false),const SizedBox(height:12),field(password,'پټنوم (لږ تر لږه ۶ توري)',true),if(message!=null)Padding(padding:const EdgeInsets.only(top:12),child:Text(message!,style:const TextStyle(color:Color(0xFF9A4B3E),fontSize:12))),const SizedBox(height:20),darkButton(busy?'مهرباني وکړئ...':(signup?'حساب جوړول':'ننوتل'),busy?null:submit),TextButton(onPressed:busy?null:()=>setState(() { signup = !signup; message = null; }),child:Text(signup?'مخکې حساب لرئ؟ ننوتل':'نوی حساب جوړ کړئ',style:const TextStyle(color:Color(0xFF806438))))
 ])));
 Widget field(TextEditingController c,String hint,bool secret)=>TextField(controller:c,obscureText:secret,textDirection:TextDirection.ltr,decoration:InputDecoration(hintText:hint,filled:true,fillColor:const Color(0xFFFFFCF6),prefixIcon:Icon(secret?Icons.lock_outline:Icons.email_outlined,color:gold),border:OutlineInputBorder(borderRadius:BorderRadius.circular(14),borderSide:BorderSide.none)));
}

class AdminPage extends StatefulWidget{final Future<void> Function()? onLogout;const AdminPage({super.key,this.onLogout});@override State<AdminPage> createState()=>_AdminPageState();}
class _AdminPageState extends State<AdminPage>{
 List<Map<String,dynamic>> poems=[],cats=[];bool busy=true;String? error;
 @override void initState(){super.initState();load();}
 Future<void> load()async{setState(() { busy = true; error = null; });try{final r=await Future.wait([supabase.from('poems').select().order('created_at',ascending:false),supabase.from('poetry_categories').select().order('sort_order')]);if(mounted)setState(() { poems = List<Map<String,dynamic>>.from(r[0]); cats = List<Map<String,dynamic>>.from(r[1]); busy = false; });}catch(_){if(mounted)setState(() { busy = false; error = 'د اډمین لاسرسی یا د ډیټابېس اجازه وګورئ.'; });}}
 void msg(String s)=>ScaffoldMessenger.of(context).showSnackBar(SnackBar(content:Text(s)));
 Future<void> edit([Map<String,dynamic>? p]) async {
  final title = TextEditingController(text: p?['title']?.toString() ?? '');
  final poet = TextEditingController(text: p?['poet_name']?.toString() ?? '');
  final body = TextEditingController(text: p?['poem_text']?.toString() ?? '');
  final tags = TextEditingController(
    text: (p?['tags'] as List?)?.join(', ') ?? '',
  );
  String? cat = p?['category_id']?.toString();
  if (cat != null && !cats.any((c) => c['id'].toString() == cat)) {
    cat = null;
  }
  bool featured = p?['is_featured'] == true;
  bool published = p?['is_published'] != false;

  final ok = await showDialog<bool>(
    context: context,
    builder: (ctx) => StatefulBuilder(
      builder: (ctx, refresh) => Directionality(
        textDirection: TextDirection.rtl,
        child: AlertDialog(
          backgroundColor: paper,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(20),
          ),
          title: Text(
            p == null ? 'نوی شعر' : 'شعر سمول',
            style: const TextStyle(color: ink, fontWeight: FontWeight.w900),
          ),
          content: SizedBox(
            width: 420,
            child: SingleChildScrollView(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  adminField(title, 'د شعر سرلیک'),
                  const SizedBox(height: 9),
                  adminField(poet, 'د شاعر نوم'),
                  const SizedBox(height: 9),
                  adminField(body, 'د شعر متن', 7),
                  const SizedBox(height: 9),
                  adminField(tags, 'ټګونه په کامه جلا کړئ'),
                  const SizedBox(height: 9),
                  DropdownButtonFormField<String>(
                    initialValue: cat,
                    isExpanded: true,
                    decoration: adminDecoration('کټګوري'),
                    items: cats.map((c) => DropdownMenuItem<String>(
                      value: c['id'].toString(),
                      child: Text(c['name'].toString()),
                    )).toList(),
                    onChanged: (v) => refresh(() => cat = v),
                  ),
                  CheckboxListTile(
                    contentPadding: EdgeInsets.zero,
                    title: const Text('غوره شوی شعر'),
                    value: featured,
                    onChanged: (v) => refresh(() => featured = v ?? false),
                  ),
                  CheckboxListTile(
                    contentPadding: EdgeInsets.zero,
                    title: const Text('شعر خپور شي'),
                    value: published,
                    onChanged: (v) => refresh(() => published = v ?? false),
                  ),
                ],
              ),
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(ctx, false),
              child: const Text('لغوه'),
            ),
            TextButton(
              onPressed: () => Navigator.pop(
                ctx,
                title.text.trim().isNotEmpty && body.text.trim().isNotEmpty,
              ),
              child: const Text('ثبتول'),
            ),
          ],
        ),
      ),
    ),
  );

  if (ok == true) {
    try {
      final data = {
        'title': title.text.trim(),
        'poet_name': poet.text.trim(),
        'poem_text': body.text.trim(),
        'category_id': cat,
        'tags': tags.text.split(',').map((x) => x.trim())
            .where((x) => x.isNotEmpty).toList(),
        'is_featured': featured,
        'is_published': published,
        'updated_at': DateTime.now().toUtc().toIso8601String(),
      };
      if (p == null) {
        await supabase.from('poems').insert(data);
      } else {
        await supabase.from('poems').update(data).eq('id', p['id']);
      }
      await load();
      if (mounted) msg('شعر ثبت شو.');
    } catch (_) {
      if (mounted) msg('ثبت ناکام شو؛ د اډمین اجازه وګورئ.');
    }
  }
  title.dispose();
  poet.dispose();
  body.dispose();
  tags.dispose();
 }
 Future<bool> confirm(String s)async=>await showDialog<bool>(context:context,builder:(ctx)=>Directionality(textDirection:TextDirection.rtl,child:AlertDialog(backgroundColor:paper,title:Text(s),actions:[TextButton(onPressed:()=>Navigator.pop(ctx,false),child:const Text('نه')),TextButton(onPressed:()=>Navigator.pop(ctx,true),child:const Text('هو، حذف یې کړه'))])))??false;
 Future<void> removePoem(Map<String,dynamic> p)async{if(!await confirm('دا شعر حذف شي؟'))return;try{await supabase.from('poems').delete().eq('id',p['id']);await load();}catch(_){msg('حذف ناکام شو.');}}
 Future<void> addCategory()async{final name=TextEditingController(),slug=TextEditingController();final ok=await showDialog<bool>(context:context,builder:(ctx)=>Directionality(textDirection:TextDirection.rtl,child:AlertDialog(backgroundColor:paper,title:const Text('نوې کټګوري'),content:Column(mainAxisSize:MainAxisSize.min,children:[adminField(name,'د کټګورۍ نوم'),const SizedBox(height:9),adminField(slug,'Slug لکه ghazal')]),actions:[TextButton(onPressed:()=>Navigator.pop(ctx,false),child:const Text('لغوه')),TextButton(onPressed:()=>Navigator.pop(ctx,true),child:const Text('ثبتول'))])));if(ok==true&&name.text.trim().isNotEmpty&&slug.text.trim().isNotEmpty){try{await supabase.from('poetry_categories').insert({'name':name.text.trim(),'slug':slug.text.trim(),'sort_order':cats.length+1});await load();}catch(_){msg('کټګوري ثبت نه شوه؛ نوم یا slug ښايي تکراري وي.');}}name.dispose();slug.dispose();}
 Future<void> removeCat(Map<String,dynamic> c)async{if(!await confirm('دا کټګوري حذف شي؟'))return;try{await supabase.from('poetry_categories').delete().eq('id',c['id']);await load();}catch(_){msg('کټګوري حذف نه شوه.');}}
 @override Widget build(BuildContext context){final u=supabase.auth.currentUser;if(u==null||u.appMetadata['role']!='admin')return const Scaffold(body:Center(child:Text('د اډمین اجازه نشته.')));
  return Directionality(textDirection:TextDirection.rtl,child:Scaffold(backgroundColor:paper,appBar:AppBar(backgroundColor:ink,title:const Text('د اډمین مرکز',style:TextStyle(color:paper)),iconTheme:const IconThemeData(color:gold),actions:[IconButton(onPressed:load,icon:const Icon(Icons.refresh,color:gold)),if(widget.onLogout!=null)IconButton(onPressed:widget.onLogout,icon:const Icon(Icons.logout,color:gold))]),floatingActionButton:FloatingActionButton.extended(backgroundColor:ink,foregroundColor:gold,onPressed:()=>edit(),icon:const Icon(Icons.add),label:const Text('نوی شعر')),
   body:busy?const Center(child:CircularProgressIndicator(color:gold)):error!=null?Center(child:Text(error!)):ListView(padding:const EdgeInsets.all(16),children:[
    Container(padding:const EdgeInsets.all(18),decoration:BoxDecoration(color:ink,borderRadius:BorderRadius.circular(20)),child:Row(children:[stat('ټول شعرونه','${poems.length}'),stat('خپور شوي','${poems.where((p)=>p['is_published']==true).length}'),stat('کټګورۍ','${cats.length}') ])),
    const SizedBox(height:22),const Text('د شعرونو اداره',style:TextStyle(color:ink,fontSize:19,fontWeight:FontWeight.w900)),const SizedBox(height:9),if(poems.isEmpty)const Text('تر اوسه شعر نشته. د نوي شعر تڼۍ کېکاږئ.'),
    ...poems.map((p)=>Container(margin:const EdgeInsets.only(bottom:8),padding:const EdgeInsets.all(12),decoration:BoxDecoration(color:const Color(0xFFFFFCF6),borderRadius:BorderRadius.circular(15)),child:Row(children:[const Icon(Icons.article_outlined,color:gold),const SizedBox(width:8),Expanded(child:Column(crossAxisAlignment:CrossAxisAlignment.start,children:[Text(p['title'].toString(),style:const TextStyle(color:ink,fontWeight:FontWeight.bold)),Text(p['is_published']==true?'خپور شوی':'پټ شوی',style:const TextStyle(color:muted,fontSize:10))])),IconButton(onPressed:()=>edit(p),icon:const Icon(Icons.edit_outlined,color:Color(0xFF806438))),IconButton(onPressed:()=>removePoem(p),icon:const Icon(Icons.delete_outline,color:Colors.redAccent))]))),
    const SizedBox(height:20),Row(children:[const Text('کټګورۍ',style:TextStyle(color:ink,fontSize:19,fontWeight:FontWeight.w900)),const Spacer(),TextButton.icon(onPressed:addCategory,icon:const Icon(Icons.add,color:gold),label:const Text('نوې کټګوري',style:TextStyle(color:ink)))]),
    ...cats.map((c)=>Container(margin:const EdgeInsets.only(bottom:7),padding:const EdgeInsets.symmetric(horizontal:13,vertical:8),decoration:BoxDecoration(color:const Color(0xFFFFFCF6),borderRadius:BorderRadius.circular(13)),child:Row(children:[const Icon(Icons.folder_outlined,color:gold),const SizedBox(width:8),Expanded(child:Text(c['name'].toString(),style:const TextStyle(color:ink,fontWeight:FontWeight.bold))),IconButton(onPressed:()=>removeCat(c),icon:const Icon(Icons.delete_outline,color:Colors.redAccent))]))),const SizedBox(height:70)
   ])));}
 Widget stat(String label,String value)=>Expanded(child:Column(children:[Text(value,style:const TextStyle(color:gold,fontSize:23,fontWeight:FontWeight.w900)),Text(label,style:const TextStyle(color:paper,fontSize:10))]));
}
Widget adminField(TextEditingController c,String label,[int lines=1])=>TextField(controller:c,maxLines:lines,textDirection:TextDirection.rtl,decoration:adminDecoration(label));
InputDecoration adminDecoration(String label)=>InputDecoration(labelText:label,filled:true,fillColor:const Color(0xFFFFFCF6),border:OutlineInputBorder(borderRadius:BorderRadius.circular(12),borderSide:BorderSide.none));
Widget darkButton(String label,VoidCallback? f)=>GestureDetector(onTap:f,child:Container(width:double.infinity,padding:const EdgeInsets.symmetric(vertical:15),decoration:BoxDecoration(color:ink,borderRadius:BorderRadius.circular(14)),child:Center(child:Text(label,style:const TextStyle(color:gold,fontWeight:FontWeight.w900)))));
