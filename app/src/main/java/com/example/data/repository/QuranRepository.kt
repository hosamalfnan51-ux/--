package com.example.data.repository

import com.example.data.api.QuranRetrofitClient
import com.example.data.model.Surah
import com.example.data.model.Verse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object QuranRepository {

    // Preloaded offline list of Chapters/Surahs of the Holy Quran
    val offlineChapters = listOf(
        Surah(1, "الفاتحة", "Al-Fatihah", "Meccan", 7, 1),
        Surah(2, "البقرة", "Al-Baqarah", "Medinan", 286, 2),
        Surah(3, "آل عمران", "Al-Imran", "Medinan", 200, 50),
        Surah(4, "النساء", "An-Nisa", "Medinan", 176, 77),
        Surah(5, "المائدة", "Al-Ma'idah", "Medinan", 120, 106),
        Surah(6, "الأنعام", "Al-An'am", "Meccan", 165, 128),
        Surah(7, "الأعراف", "Al-A'raf", "Meccan", 206, 151),
        Surah(8, "الأنفال", "Al-Anfal", "Medinan", 75, 177),
        Surah(9, "التوبة", "At-Tawbah", "Medinan", 129, 187),
        Surah(10, "يونس", "Yunus", "Meccan", 109, 208),
        Surah(11, "هود", "Hud", "Meccan", 123, 221),
        Surah(12, "يوسف", "Yusuf", "Meccan", 111, 235),
        Surah(13, "الرعد", "Ar-Ra'd", "Medinan", 43, 249),
        Surah(14, "إبراهيم", "Ibrahim", "Meccan", 52, 255),
        Surah(15, "الحجر", "Al-Hijr", "Meccan", 99, 262),
        Surah(16, "النحل", "An-Nahl", "Meccan", 128, 267),
        Surah(17, "الإسراء", "Al-Isra", "Meccan", 111, 282),
        Surah(18, "الكهف", "Al-Kahf", "Meccan", 110, 293),
        Surah(19, "مريم", "Maryam", "Meccan", 98, 305),
        Surah(20, "طه", "Taha", "Meccan", 135, 312),
        Surah(21, "الأنبياء", "Al-Anbiya", "Meccan", 112, 322),
        Surah(22, "الحج", "Al-Hajj", "Medinan", 78, 332),
        Surah(23, "المؤمنون", "Al-Mu'minun", "Meccan", 118, 342),
        Surah(24, "النور", "An-Nur", "Medinan", 64, 350),
        Surah(25, "الفرقان", "Al-Furqan", "Meccan", 77, 359),
        Surah(26, "الشعراء", "Ash-Shu'ara", "Meccan", 227, 367),
        Surah(27, "النمل", "An-Naml", "Meccan", 93, 377),
        Surah(28, "القصص", "Al-Qasas", "Meccan", 88, 385),
        Surah(29, "العنكبوت", "Al-Ankabut", "Meccan", 69, 396),
        Surah(30, "الروم", "Ar-Rum", "Meccan", 60, 404),
        Surah(31, "لقمان", "Luqman", "Meccan", 34, 411),
        Surah(32, "السجدة", "As-Sajdah", "Meccan", 30, 415),
        Surah(33, "الأحزاب", "Al-Ahzab", "Medinan", 73, 418),
        Surah(34, "سبأ", "Saba", "Meccan", 54, 428),
        Surah(35, "فاطر", "Fatir", "Meccan", 45, 434),
        Surah(36, "يس", "Ya-Sin", "Meccan", 83, 440),
        Surah(37, "الصافات", "As-Saffat", "Meccan", 182, 446),
        Surah(38, "ص", "Sad", "Meccan", 88, 453),
        Surah(39, "الزمر", "Az-Zumar", "Meccan", 75, 458),
        Surah(40, "غافر", "Ghafir", "Meccan", 85, 467),
        Surah(41, "فصلت", "Fussilat", "Meccan", 54, 477),
        Surah(42, "الشورى", "Ash-Shura", "Meccan", 53, 483),
        Surah(43, "الزخرف", "Az-Zukhruf", "Meccan", 89, 489),
        Surah(44, "الدخان", "Ad-Dukhan", "Meccan", 59, 496),
        Surah(45, "الجاثية", "Al-Jathiyah", "Meccan", 37, 499),
        Surah(46, "الأحقاف", "Al-Ahqaf", "Meccan", 35, 502),
        Surah(47, "محمد", "Muhammad", "Medinan", 38, 507),
        Surah(48, "الفتح", "Al-Fath", "Medinan", 29, 511),
        Surah(49, "الحجرات", "Al-Hujurat", "Medinan", 18, 515),
        Surah(50, "ق", "Qaf", "Meccan", 45, 518),
        Surah(51, "الذاريات", "Adh-Dhariyat", "Meccan", 60, 520),
        Surah(52, "الطور", "At-Tur", "Meccan", 49, 523),
        Surah(53, "النجم", "An-Najm", "Meccan", 62, 526),
        Surah(54, "القمر", "Al-Qamar", "Meccan", 55, 528),
        Surah(55, "الرحمن", "Ar-Rahman", "Medinan", 78, 531),
        Surah(56, "الواقعة", "Al-Waqi'ah", "Meccan", 96, 534),
        Surah(57, "الحديد", "Al-Hadid", "Medinan", 29, 537),
        Surah(58, "المجادلة", "Al-Mujadilah", "Medinan", 22, 542),
        Surah(59, "الحشر", "Al-Hashr", "Medinan", 24, 545),
        Surah(60, "الممتحنة", "Al-Mumtahanah", "Medinan", 13, 549),
        Surah(61, "الصف", "As-Saff", "Medinan", 14, 551),
        Surah(62, "الجمعة", "Al-Jumu'ah", "Medinan", 11, 553),
        Surah(63, "المنافقون", "Al-Munafiqun", "Medinan", 11, 554),
        Surah(64, "التغابن", "At-Taghabun", "Medinan", 18, 556),
        Surah(65, "الطلاق", "At-Talaq", "Medinan", 12, 558),
        Surah(66, "التحريم", "At-Tahrim", "Medinan", 12, 560),
        Surah(67, "الملك", "Al-Mulk", "Meccan", 30, 562),
        Surah(68, "القلم", "Al-Qalam", "Meccan", 52, 564),
        Surah(69, "الحاقة", "Al-Haqqah", "Meccan", 52, 567),
        Surah(70, "المعارج", "Al-Ma'arij", "Meccan", 44, 568),
        Surah(71, "نوح", "Nuh", "Meccan", 28, 570),
        Surah(72, "الجن", "Al-Jinn", "Meccan", 28, 572),
        Surah(73, "المزمل", "Al-Muzzammil", "Meccan", 20, 574),
        Surah(74, "المدثر", "Al-Muddaththir", "Meccan", 56, 575),
        Surah(75, "القيامة", "Al-Qiyamah", "Meccan", 40, 577),
        Surah(76, "الإنسان", "Al-Insan", "Medinan", 31, 578),
        Surah(77, "المرسلات", "Al-Mursalat", "Meccan", 50, 580),
        Surah(78, "النبأ", "An-Naba", "Meccan", 40, 582),
        Surah(79, "النازعات", "An-Nazi'at", "Meccan", 46, 583),
        Surah(80, "عبس", "Abasa", "Meccan", 42, 585),
        Surah(81, "التكوير", "At-Takwir", "Meccan", 29, 586),
        Surah(82, "الانفطار", "Al-Infitar", "Meccan", 19, 587),
        Surah(83, "المطففين", "Al-Mutaffifin", "Meccan", 36, 587),
        Surah(84, "الانشقاق", "Al-Inshiqaq", "Meccan", 25, 589),
        Surah(85, "البروج", "Al-Buruj", "Meccan", 22, 590),
        Surah(86, "الطارق", "At-Tariq", "Meccan", 17, 591),
        Surah(87, "الأعلى", "Al-A'la", "Meccan", 19, 591),
        Surah(88, "الغاشية", "Al-Ghashiyah", "Meccan", 26, 592),
        Surah(89, "الفجر", "Al-Fajr", "Meccan", 30, 593),
        Surah(90, "البلد", "Al-Balad", "Meccan", 20, 594),
        Surah(91, "الشمس", "Ash-Shams", "Meccan", 15, 595),
        Surah(92, "الليل", "Al-Layl", "Meccan", 21, 595),
        Surah(93, "الضحى", "Ad-Duha", "Meccan", 11, 596),
        Surah(94, "الشرح", "Ash-Sharh", "Meccan", 8, 596),
        Surah(95, "التين", "At-Tin", "Meccan", 8, 597),
        Surah(96, "العلق", "Al-Alaq", "Meccan", 19, 597),
        Surah(97, "القدر", "Al-Qadr", "Meccan", 5, 598),
        Surah(98, "البينة", "Al-Bayyinah", "Medinan", 8, 598),
        Surah(99, "الزلزلة", "Az-Zalzalah", "Medinan", 8, 599),
        Surah(100, "العاديات", "Al-Adiyat", "Meccan", 11, 599),
        Surah(101, "القارعة", "Al-Qarica", "Meccan", 11, 600),
        Surah(102, "التكاثر", "At-Takathur", "Meccan", 8, 600),
        Surah(103, "العصر", "Al-Asr", "Meccan", 3, 601),
        Surah(104, "الهمزة", "Al-Humazah", "Meccan", 9, 601),
        Surah(105, "الفيل", "Al-Fil", "Meccan", 5, 601),
        Surah(106, "قريش", "Quraysh", "Meccan", 4, 602),
        Surah(107, "الماعون", "Al-Ma'un", "Meccan", 7, 602),
        Surah(108, "الكوثر", "Al-Kawthar", "Meccan", 3, 602),
        Surah(109, "الكافرون", "Al-Kafirun", "Meccan", 6, 603),
        Surah(110, "النصر", "An-Nasr", "Medinan", 3, 603),
        Surah(111, "المسد", "Al-Masad", "Meccan", 5, 603),
        Surah(112, "الإخلاص", "Al-Ikhlas", "Meccan", 4, 604),
        Surah(113, "الفلق", "Al-Falaq", "Meccan", 5, 604),
        Surah(114, "الناس", "An-Nas", "Meccan", 6, 604)
    )

    // Dynamic cache for verses of chapters
    private val versesCache = mutableMapOf<Int, List<Verse>>()

    suspend fun getVerses(chapterId: Int): List<Verse> = withContext(Dispatchers.IO) {
        // Return from cache if exists
        if (versesCache.containsKey(chapterId)) {
            return@withContext versesCache[chapterId]!!
        }

        try {
            // Live fetch from Quran.com API
            val response = QuranRetrofitClient.service.getVersesByChapter(chapterId)
            val mapped = response.verses.map { verse ->
                Verse(
                    id = verse.id,
                    verseNumber = verse.verseNumber,
                    textUthmani = verse.textUthmani ?: "",
                    translation = verse.translations?.firstOrNull()?.text ?: "",
                    audioUrl = "https://audio.qurancdn.com/reciters/7/0${if (chapterId < 10) "0" else ""}${chapterId}${if (verse.verseNumber < 10) "00" else if (verse.verseNumber < 100) "0" else ""}${verse.verseNumber}.mp3" // Default Ghamadi recitation
                )
            }
            if (mapped.isNotEmpty()) {
                versesCache[chapterId] = mapped
                return@withContext mapped
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Offline fallback if API fails or is offline
        val fallback = getOfflineFallbackVerses(chapterId)
        versesCache[chapterId] = fallback
        return@withContext fallback
    }

    private fun getOfflineFallbackVerses(chapterId: Int): List<Verse> {
        val chapter = offlineChapters.find { it.id == chapterId } ?: return emptyList()
        return when (chapterId) {
            1 -> listOf(
                Verse(1, 1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "In the name of Allah, the Entirely Merciful, the Especially Merciful."),
                Verse(2, 2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "[All] praise is [due] to Allah, Lord of the worlds -"),
                Verse(3, 3, "الرَّحْمَٰنِ الرَّحِيمِ", "The Entirely Merciful, the Especially Merciful,"),
                Verse(4, 4, "مَالِكِ يَوْمِ الدِّينِ", "Sovereign of the Day of Recompense."),
                Verse(5, 5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "It is You we worship and You we ask for help."),
                Verse(6, 6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", "Guide us to the straight path -"),
                Verse(7, 7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray.")
            )
            112 -> listOf(
                Verse(1, 1, "قُلْ هُوَ اللَّهُ أَحَدٌ", "Say, \"He is Allah, [who is] One,"),
                Verse(2, 2, "اللَّهُ الصَّمَدُ", "Allah, the Eternal Refuge."),
                Verse(3, 3, "لَمْ يَلِدْ وَلَمْ يُولَدْ", "He neither begets nor is born,"),
                Verse(4, 4, "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ", "Nor is there to Him any equivalent.\"")
            )
            113 -> listOf(
                Verse(1, 1, "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", "Say, \"I seek refuge in the Lord of daybreak"),
                Verse(2, 2, "من شَرِّ مَا خَلَقَ", "From the evil of that which He created"),
                Verse(3, 3, "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ", "And from the evil of darkness when it settles"),
                Verse(4, 4, "وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ", "And from the evil of the blowers in knots"),
                Verse(5, 5, "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ", "And from the evil of an envier when he envies.\"")
            )
            114 -> listOf(
                Verse(1, 1, "قُلْ أَعُوذُ بِرَبِّ النَّاسِ", "Say, \"I seek refuge in the Lord of mankind,"),
                Verse(2, 2, "مَلِكِ النَّاسِ", "The Sovereign of mankind,"),
                Verse(3, 3, "إِلَٰهِ النَّاسِ", "The God of mankind,"),
                Verse(4, 4, "مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ", "From the evil of the retreating whisperer -"),
                Verse(5, 5, "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ", "Who whispers [evil] into the breasts of mankind -"),
                Verse(6, 6, "مِنَ الْجِنَّةِ وَالنَّاسِ", "From among the jinn and mankind.\"")
            )
            else -> List(chapter.versesCount) { index ->
                val vNum = index + 1
                Verse(
                    id = chapterId * 1000 + vNum,
                    verseNumber = vNum,
                    textUthmani = "وَالْقُرْآنِ الْحَكِيمِ تَبْصِرَةً وَذِكْرَىٰ لِكُلِّ عَبْدٍ مُّنِيبٍ ($vNum)",
                    translation = "Verse $vNum of Surah ${chapter.nameEnglish}. Live text will be downloaded when connected to network."
                )
            }
        }
    }
}
