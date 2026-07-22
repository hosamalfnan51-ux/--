package com.example.data.repository

data class DuaItem(
    val id: Int,
    val title: String,
    val text: String,
    val category: String, // "أدعية من القرآن الكريم", "أدعية من السنة النبوية", "أدعية جامعة وشاملة"
    val reference: String,
    val translationEnglish: String = ""
)

object DuaRepository {

    val categories = listOf(
        "الكل",
        "أدعية من القرآن الكريم",
        "أدعية من السنة النبوية",
        "أدعية جامعة وشاملة"
    )

    val duaList = listOf(
        // === أدعية من القرآن الكريم ===
        DuaItem(
            id = 1,
            title = "دعاء خيري الدنيا والآخرة والوقاية من النار",
            text = "﴿رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ﴾",
            category = "أدعية من القرآن الكريم",
            reference = "سورة البقرة - الآية 201",
            translationEnglish = "Our Lord, give us in this world that which is good and in the Hereafter that which is good and protect us from the punishment of the Fire."
        ),
        DuaItem(
            id = 2,
            title = "دعاء الثبات على الإيمان والرحمة",
            text = "﴿رَبَّنَا لاَ تُزِغْ قُلُوبَنَا بَعْدَ إِذْ هَدَيْتَنَا وَهَبْ لَنَا مِن لَّدُنكَ رَحْمَةً إِنَّكَ أَنتَ الْوَهَّابُ﴾",
            category = "أدعية من القرآن الكريم",
            reference = "سورة آل عمران - الآية 8",
            translationEnglish = "Our Lord, let not our hearts swerve after You have guided us and grant us from Yourself mercy. Indeed, You are the Bestower."
        ),
        DuaItem(
            id = 3,
            title = "دعاء المغفرة للوالدين وللمؤمنين",
            text = "﴿رَبَّنَا اغْفِرْ لِي وَلِوَالِدَيَّ وَلِلْمُؤْمِنِينَ يَوْمَ يَقُومُ الْحِسَابُ﴾",
            category = "أدعية من القرآن الكريم",
            reference = "سورة إبراهيم - الآية 41",
            translationEnglish = "Our Lord, forgive me and my parents and the believers the Day the account is established."
        ),
        DuaItem(
            id = 4,
            title = "دعاء شرح الصدر وتيسير الأمور (دعاء موسى عليه السلام)",
            text = "﴿رَبِّ اشْرَحْ لِي صَدْرِي * وَيَسِّرْ لِي أَمْرِي * وَاحْلُلْ عُقْدَةً مِّن لِّسَانِي * يَفْقَهُوا قَوْلِي﴾",
            category = "أدعية من القرآن الكريم",
            reference = "سورة طه - الآيات 25-28",
            translationEnglish = "My Lord, expand for me my breast [with assurance] and ease for me my task and untie the knot from my tongue that they may understand my speech."
        ),
        DuaItem(
            id = 5,
            title = "دعاء النصر وصدق المخرج والمدخل",
            text = "﴿رَّبِّ أَدْخِلْنِي مُدْخَلَ صِدْقٍ وَأَخْرِجْنِي مُخْرَجَ صِدْقٍ وَاجْعَل لِّي مِن لَّدُنكَ سُلْطَانًا نَّصِيرًا﴾",
            category = "أدعية من القرآن الكريم",
            reference = "سورة الإسراء - الآية 80",
            translationEnglish = "My Lord, cause me to enter a sound entrance and cause me to exit a sound exit and grant me from Yourself a supporting authority."
        ),
        DuaItem(
            id = 6,
            title = "دعاء صلاح الأزواج والذريات والأبناء",
            text = "﴿رَبَّنَا هَبْ لَنَا مِنْ أَزْوَاجِنَا وَذُرِّيَّاتِنَا قُرَّةَ أَعْيُنٍ وَاجْعَلْنَا لِلْمُتَّقِينَ إِمَامًا﴾",
            category = "أدعية من القرآن الكريم",
            reference = "سورة الفرقان - الآية 74",
            translationEnglish = "Our Lord, grant us from among our wives and offspring comfort to our eyes and make us an example for the righteous."
        ),
        DuaItem(
            id = 7,
            title = "دعاء إجابة الحاجة والرزق (دعاء موسى عند مدين)",
            text = "﴿رَبِّ إِنِّي لِمَا أَنزَلْتَ إِلَيَّ مِنْ خَيْرٍ فَقِيرٌ﴾",
            category = "أدعية من القرآن الكريم",
            reference = "سورة القصص - الآية 24",
            translationEnglish = "My Lord, indeed I am, for whatever good You would send down to me, in need."
        ),
        DuaItem(
            id = 8,
            title = "دعاء الذرية الصالحة (دعاء زكريا عليه السلام)",
            text = "﴿رَبِّ لَا تَذَرْنِي فَرْدًا وَأَنتَ خَيْرُ الْوَارِثِينَ﴾",
            category = "أدعية من القرآن الكريم",
            reference = "سورة الأنبياء - الآية 89",
            translationEnglish = "My Lord, do not leave me alone [with no heir], while You are the best of inheritors."
        ),
        DuaItem(
            id = 9,
            title = "دعاء يونس عليه السلام في بطن الحوت (كشف الكرب والهم)",
            text = "﴿لَّا إِلَٰهَ إِلَّا أَنتَ سُبْحَانَكَ إِنِّي كُنتُ مِنَ الظَّالِمِينَ﴾",
            category = "أدعية من القرآن الكريم",
            reference = "سورة الأنبياء - الآية 87",
            translationEnglish = "There is no deity except You; exalted are You. Indeed, I have been of the wrongdoers."
        ),
        DuaItem(
            id = 10,
            title = "دعاء زياد العلم والهدى",
            text = "﴿وَقُل رَّبِّ زِدْنِي عِلْمًا﴾",
            category = "أدعية من القرآن الكريم",
            reference = "سورة طه - الآية 114",
            translationEnglish = "And say, 'My Lord, increase me in knowledge.'"
        ),

        // === أدعية من السنة النبوية ===
        DuaItem(
            id = 101,
            title = "دعاء الهدى والتقى والعفاف والغنى",
            text = "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْهُدَى وَالتُّقَى وَالْعَفَافَ وَالْغِنَى.",
            category = "أدعية من السنة النبوية",
            reference = "صحيح مسلم عن ابن مسعود رضي الله عنه",
            translationEnglish = "O Allah, I ask You for guidance, piety, chastity, and self-sufficiency."
        ),
        DuaItem(
            id = 102,
            title = "دعاء العفو والمغفرة (دعاء ليلة القدر)",
            text = "اللَّهُمَّ إِنَّكَ عَفُوٌّ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي.",
            category = "أدعية من السنة النبوية",
            reference = "جامع الترمذي عن عائشة رضي الله عنها",
            translationEnglish = "O Allah, You are Pardoning, You love to pardon, so pardon me."
        ),
        DuaItem(
            id = 103,
            title = "دعاء صلاح الدين والدنيا والآخرة",
            text = "اللَّهُمَّ أَصْلِحْ لِي دِينِي الَّذِي هُوَ عِصْمَةُ أَمْرِي، وَأَصْلِحْ لِي دُنْيَايَ الَّتِي فِيهَا مَعَاشِي، وَأَصْلِحْ لِي آخِرَتِي الَّتِي فِيهَا مَعَادِي، وَاجْعَلِ الْحَيَاةَ زِيَادَةً لِي فِي كُلِّ خَيْرٍ، وَاجْعَلِ الْمَوْتَ رَاحَةً لِي مِنْ كُلِّ شَرٍّ.",
            category = "أدعية من السنة النبوية",
            reference = "صحيح مسلم عن أبي هريرة رضي الله عنه",
            translationEnglish = "O Allah, set right for me my religion which is the safeguard of my affairs, and set right for me my worldly affairs in which is my livelihood..."
        ),
        DuaItem(
            id = 104,
            title = "دعاء الثبات على الدين والطاعة",
            text = "يَا مُقَلِّبَ الْقُلُوبِ ثَبِّتْ قَلْبِي عَلَى دِينِكَ، اللَّهُمَّ مَصَرِّفَ الْقُلُوبِ صَرِّفْ قُلُوبَنَا عَلَى طَاعَتِكَ.",
            category = "أدعية من السنة النبوية",
            reference = "سنن الترمذي وصحيح مسلم",
            translationEnglish = "O Turner of the hearts, make my heart steadfast upon Your religion."
        ),
        DuaItem(
            id = 105,
            title = "دعاء الاستعاذة من الهم والحزن والعجز والكسل",
            text = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ، وَالْعَجْزِ وَالْكَسَلِ، وَالْبُخْلِ وَالْجُبْنِ، وَضَلَعِ الدَّيْنِ، وَغَلَبَةِ الرِّجَالِ.",
            category = "أدعية من السنة النبوية",
            reference = "صحيح البخاري عن أنس بن مالك رضي الله عنه",
            translationEnglish = "O Allah, I seek refuge in You from anxiety and sorrow, weakness and laziness, miserliness and cowardice..."
        ),
        DuaItem(
            id = 106,
            title = "دعاء تزكية النفس والتقوى",
            text = "اللَّهُمَّ آتِ نَفْسِي تَقْوَاهَا، وَزَكِّهَا أَنْتَ خَيْرُ مَنْ زَكَّاهَا، أَنْتَ وَلِيُّهَا وَمَوْلَاهَا.",
            category = "أدعية من السنة النبوية",
            reference = "صحيح مسلم عن زيد بن أرقم رضي الله عنه",
            translationEnglish = "O Allah, grant my soul its piety and purify it; You are the best to purify it. You are its Guardian and Master."
        ),

        // === أدعية جامعة وشاملة ===
        DuaItem(
            id = 201,
            title = "دعاء تفريج الهموم وقضاء الديون والرزق",
            text = "اللَّهُمَّ اكْفِنِي بِحَلَالِكَ عَنْ حَرَامِكَ، وَأَغْنِنِي بِفَضْلِكَ عَمَّنْ سِوَاكَ.",
            category = "أدعية جامعة وشاملة",
            reference = "سنن الترمذي وحسنه الألباني",
            translationEnglish = "O Allah, suffice me with Your lawful provisions against Your unlawful ones, and enrich me with Your bounty above all others."
        ),
        DuaItem(
            id = 202,
            title = "دعاء الشفاء للمريض والراحة للمتألم",
            text = "اللَّهُمَّ رَبَّ النَّاسِ أَذْهِبِ الْبَاسَ، اشْفِ وَأَنْتَ الشَّافِي، لَا شِفَاءَ إِلَّا شِفَاؤُكَ، شِفَاءً لَا يُغَادِرُ سَقَمًا.",
            category = "أدعية جامعة وشاملة",
            reference = "صحيح البخاري ومسلم",
            translationEnglish = "O Allah, Lord of mankind, remove the suffering, heal, You are the Healer, there is no healing except Yours..."
        ),
        DuaItem(
            id = 203,
            title = "دعاء التوبة الشامل وتجديد العهد مع الله",
            text = "اللَّهُمَّ اغْفِرْ لِي ذَنْبِي كُلَّهُ، دِقَّهُ وَجِلَّهُ، وَأَوَّلَهُ وَآخِرَهُ، وَعَلَانِيَتَهُ وَسِرَّهُ.",
            category = "أدعية جامعة وشاملة",
            reference = "صحيح مسلم عن أبي هريرة رضي الله عنه",
            translationEnglish = "O Allah, forgive me all my sins, small and great, first and last, open and secret."
        ),
        DuaItem(
            id = 204,
            title = "دعاء الرحمة والمغفرة للموتى ولجميع المسلمين والمسلمات",
            text = "اللَّهُمَّ اغْفِرْ لِحَيِّنَا وَمَيِّتِنَا، وَشَاهِدِنَا وَغَائِبِنَا، وَصَغِيرِنَا وَكَبِيرِنَا، وَذَكَرِنَا وَأُنْثَانَا. اللَّهُمَّ مَنْ أَحْيَيْتَهُ مِنَّا فَأَحْيِهِ عَلَى الْإِسْلَامِ، وَمَنْ تَوَفَّيْتَهُ مِنَّا فَتَوَفَّهُ عَلَى الْإِيمَانِ، اللَّهُمَّ لَا تَحْرِمْنَا أَجْرَهُ وَلَا تُضِلَّنَا بَعْدَهُ.",
            category = "أدعية جامعة وشاملة",
            reference = "سنن الترمذي وأبو داود وابن ماجه",
            translationEnglish = "O Allah, forgive our living and our dead, those present and those absent, our young and our old, our males and our females..."
        )
    )
}
