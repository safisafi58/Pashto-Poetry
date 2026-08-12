package com.example.data.repository

import com.example.data.local.entities.PoemEntity
import com.example.data.local.entities.PoetEntity
import com.example.data.local.entities.UserEntity

object InitialSeedData {

    val poets = listOf(
        PoetEntity(
            id = "poet_rahman_baba",
            name = "عبدالرحمان بابا",
            era = "مغولي دوره (۱۰۴۲ - ۱۱۲۸ هـ)",
            bio = "د پښتو ژبې خورا مشهور او تصوفي شاعر. د بابا اشعار د روحي تصوف، اخلاقو او انسان دوستۍ نمونې دي.",
            imageUrl = null,
            poemCount = 12
        ),
        PoetEntity(
            id = "poet_khushal_khattak",
            name = "خوشحال خان خټک",
            era = "مغولي دوره (۱۰۲۲ - ۱۱۰۰ هـ)",
            bio = "د پښتو ژبې ستر لړمون، قومي مشر، توريالی او شمشېر زنی شاعر. د ننګ او غېرت ستر علمدار.",
            imageUrl = null,
            poemCount = 15
        ),
        PoetEntity(
            id = "poet_ghani_khan",
            name = "غني خان",
            era = "معاصره دوره (۱۹۱۴ - ۱۹۹۶ م)",
            bio = "د پښتو ژبې ليونی او مجذوب فیلسوف، انځورګر او روښانه ژبی شاعر. د حسن او عشق فلسفي.",
            imageUrl = null,
            poemCount = 18
        ),
        PoetEntity(
            id = "poet_hamza_baba",
            name = "امیر حمزه خان شنوارى (حمزه بابا)",
            era = "د پښتو غزل بابا (۱۹۰۷ - ۱۹۹۴ م)",
            bio = "د پښتو عصري غزل بنسټ ایښودونکی او د تغزل بابا. د پښتنو د کلتور او احساس ترجمان.",
            imageUrl = null,
            poemCount = 10
        ),
        PoetEntity(
            id = "poet_darwesh_durrani",
            name = "دروېش درانی",
            era = "اوسنۍ دوره",
            bio = "د معاصر پښتو شعر ژور فکرى او ښکلي انځورګر شاعر. د پښتو نوي احساس استازی.",
            imageUrl = null,
            poemCount = 8
        )
    )

    val poems = listOf(
        PoemEntity(
            id = "poem_rahman_1",
            title = "کر د ګلونو کړه",
            content = """
                کــر د ګــلــونــو کــړه چــې ســیــمــه دې ګــلــزار شــي
                خــار مــه کــره پــه پــښــو کــې چــې بــه خــار شــي

                تــه چــې بــل تــه چــاه کــنــې مــنــزل پــه چــاه کــې
                د هــغــه ســړي بــه کــلــه ګــوزاره شــي

                ســپــېــنــې اوښــکــې د مــظــلــوم پــه نــا حــق مــه توېـوه
                دا اوښــکــې بــه ســتــا د کور ودانــي نــســکــوره کــړي
            """.trimIndent(),
            poetId = "poet_rahman_baba",
            poetName = "عبدالرحمان بابا",
            category = "islamic",
            authorUserId = null,
            isApproved = true,
            isFeatured = true,
            likesCount = 248,
            favoritesCount = 180,
            commentsCount = 14,
            createdAt = System.currentTimeMillis() - 864000000L
        ),
        PoemEntity(
            id = "poem_khushal_1",
            title = "د افغان په ننګ",
            content = """
                د افــغــان پــه نــنــګ مــې و بــســتــه تــوره
                نــنــګــیــالــی د ز مــانــې خــوشــحــال خــټــک یــم

                چــې تــوریــالــی وی او نــنــګــیــالــی وی
                هــغــه به بــس ز مــا په ډلــه کــې ســپــارلــی وی

                زه هــغــه ده چــې خــپــل قوم تــه خــدمــت کــړم
                پــه جــهــان بــه د افــغــان نــوم جــګــره نــشــي
            """.trimIndent(),
            poetId = "poet_khushal_khattak",
            poetName = "خوشحال خان خټک",
            category = "patriotic",
            authorUserId = null,
            isApproved = true,
            isFeatured = true,
            likesCount = 320,
            favoritesCount = 210,
            commentsCount = 22,
            createdAt = System.currentTimeMillis() - 720000000L
        ),
        PoemEntity(
            id = "poem_ghani_1",
            title = "مست او لیونی",
            content = """
                زه یــم جــونــګــړه زه یــم ښــار د مــســتــو
                زه یــم ز مــز مــه د جــانــان د هــســتــو

                تــه مــه پــوښــتــه د ســاقــي د جــام قــصــه
                چــې پــه هــر ګــوټ کــې يــې مــســتــي د نــســتــو

                خــپــلــه روح مــې د ګــلــونــو پــه وږمــو شــوه
                خــپــلــه خــنــدا مــې د ســحــر پــه چــنــد نــســتــو
            """.trimIndent(),
            poetId = "poet_ghani_khan",
            poetName = "غني خان",
            category = "ghazal",
            authorUserId = null,
            isApproved = true,
            isFeatured = false,
            likesCount = 195,
            favoritesCount = 130,
            commentsCount = 9,
            createdAt = System.currentTimeMillis() - 500000000L
        ),
        PoemEntity(
            id = "poem_hamza_1",
            title = "پښتنه حیا",
            content = """
                نــه هــېــرېــږي مــا تــه هــغــه يــادونــه
                د پــښــتــون د غــېــر تــنــکــي ســتــرګــونــه

                تــا چــې د نــظــر د غ غــشــي وویــشــتــل
                ز مــا ز هــړه بــه پــه ويــنــو بــنــد بــنــد شــي

                نــصــیــب مــې شــوه د پــښــتــنــې ښــکــلا غــوټــۍ
                پــه هــر غــزل کــې مــې د مــېــنــې اوازونــه
            """.trimIndent(),
            poetId = "poet_hamza_baba",
            poetName = "امیر حمزه خان شنوارى",
            category = "romance",
            authorUserId = null,
            isApproved = true,
            isFeatured = false,
            likesCount = 178,
            favoritesCount = 95,
            commentsCount = 11,
            createdAt = System.currentTimeMillis() - 300000000L
        ),
        PoemEntity(
            id = "poem_landay_1",
            title = "د پښتو مېنډې لنډۍ",
            content = """
                کــه پــه مــېــنــه کــې بــرې بــرې كــړې
                زه بــه صــحــرا تــه د جــانــان لــه مــيــنــې بــشــپــړ شــم

                ***

                تــورې ســتــرګــې د جــانــان ز مــا پــه زړه كــې
                لکــه د خــنــجــر نــکــې بــې نــښــانــه کــښېــنــي
            """.trimIndent(),
            poetId = "poet_darwesh_durrani",
            poetName = "ولسي لنډۍ",
            category = "landay",
            authorUserId = null,
            isApproved = true,
            isFeatured = false,
            likesCount = 210,
            favoritesCount = 145,
            commentsCount = 8,
            createdAt = System.currentTimeMillis() - 100000000L
        )
    )

    val adminUsers = listOf(
        UserEntity(
            id = "admin_01",
            name = "محمد عمران پښتون",
            email = "imran.admin@pashto.af",
            avatarUrl = null,
            bio = "ارشد اډمین او د پښتو ادبیاتو برخې مسؤل مدیر",
            isAdmin = true,
            isVerifiedPoet = true,
            isLoggedIn = false,
            createdAt = System.currentTimeMillis() - 864000000L
        ),
        UserEntity(
            id = "admin_02",
            name = "استاد عنایت الله درانی",
            email = "inayat.poet@pashto.af",
            avatarUrl = null,
            bio = "د شعرونو کره کتونکی او مذهبي او ننګیالیو اشعارو کره کتونکی مدیر",
            isAdmin = true,
            isVerifiedPoet = true,
            isLoggedIn = false,
            createdAt = System.currentTimeMillis() - 400000000L
        ),
        UserEntity(
            id = "admin_03",
            name = "انجنیر احمد شاه سلیمانخېل",
            email = "ahmad.tech@pashto.af",
            avatarUrl = null,
            bio = "د اپلیکیشن فني اډمین او تخنیکي ناظر",
            isAdmin = true,
            isVerifiedPoet = false,
            isLoggedIn = false,
            createdAt = System.currentTimeMillis() - 200000000L
        )
    )
}
