package com.example.data.repository

import java.util.Calendar

data class DailyDhikrItem(
    val id: Int,
    val textArabic: String,
    val textEnglishTransliteration: String,
    val textEnglishTranslation: String,
    val virtueArabic: String,
    val virtueEnglish: String,
    val referenceArabic: String,
    val referenceEnglish: String,
    val count: Int = 1
)

object DailyDhikrRepository {

    val dailyDhikrs = listOf(
        DailyDhikrItem(
            id = 1,
            textArabic = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ.",
            textEnglishTransliteration = "Asbahna wa asbahal-mulku lillahi wal-hamdulillahi, la ilaha illallahu wahdahu la sharika lahu, lahul-mulku wa lahul-hamdu wa huwa 'ala kulli shay'in qadir.",
            textEnglishTranslation = "We have entered the morning and the kingdom belongs to Allah, praise be to Allah. There is no deity worthy of worship except Allah alone, with no partner. To Him belongs sovereignty and praise, and He is capable of all things.",
            virtueArabic = "من قالها في الصباح أُعطي خير ما في هذا اليوم وحُفظ من كل شر.",
            virtueEnglish = "Whoever recites this in the morning is granted the goodness of the day and protected from evil.",
            referenceArabic = "رواه مسلم (2723)",
            referenceEnglish = "Sahih Muslim 2723"
        ),
        DailyDhikrItem(
            id = 2,
            textArabic = "اللَّهُمَّ مَا أَصْبَحَ بِي مِنْ نِعْمَةٍ أَوْ بِأَحَدٍ مِنْ خَلْقِكَ فَمِنْكَ وَحْدَكَ لاَ شَرِيكَ لَكَ، فَلَكَ الْحَمْدُ وَلَكَ الشُّكْرُ.",
            textEnglishTransliteration = "Allahumma ma asbaha bi min ni'matin aw bi-ahadin min khalqika faminka wahdaka la sharika laka, falakal-hamdu wa lakash-shukr.",
            textEnglishTranslation = "O Allah, whatever blessing has received me or any of Your creation this morning, it is from You alone without any partner. To You belong all praise and gratitude.",
            virtueArabic = "من قالها حين يصبح فقد أدى شكر يومه.",
            virtueEnglish = "Whoever recites this in the morning has fulfilled their gratitude for that day.",
            referenceArabic = "رواه أبو داود (5073)",
            referenceEnglish = "Sunan Abi Dawud 5073"
        ),
        DailyDhikrItem(
            id = 3,
            textArabic = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ.",
            textEnglishTransliteration = "Subhanallahi wa bihamdihi, 'adada khalqihi, wa rida nafsihi, wa zinata 'arshihi, wa midada kalimatihi.",
            textEnglishTranslation = "Glory and praise be to Allah, as much as the number of His creation, according to His pleasure, equal to the weight of His Throne, and as vast as the ink of His words.",
            virtueArabic = "ذكر عظيم يعدل أذكاراً كثيرة في الأجر والتسبيح.",
            virtueEnglish = "A grand supplication whose reward outweighs hours of continuous glorification.",
            referenceArabic = "رواه مسلم (2726)",
            referenceEnglish = "Sahih Muslim 2726"
        ),
        DailyDhikrItem(
            id = 4,
            textArabic = "اللَّهُمَّ أَنْتَ رَبِّي لاَ إِلَهَ إِلاَّ أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لاَ يَغْفِرُ الذُّنُوبَ إِلاَّ أَنْتَ.",
            textEnglishTransliteration = "Allahumma Anta Rabbi la ilaha illa Anta, khalaqtani wa ana 'abduka, wa ana 'ala 'ahdika wa wa'dika mastata'tu, a'udhu bika min sharri ma sana'tu, abu'u laka bini'matika 'alayya, wa abu'u bidhanbi faghfir li fa-innahu la yaghfirudh-dhunuba illa Anta.",
            textEnglishTranslation = "O Allah, You are my Lord, there is no deity except You. You created me and I am Your servant, and I remain faithful to Your covenant as best I can. I seek refuge in You from the evil I have done. I acknowledge Your blessings upon me and I confess my sins, so forgive me, for none forgives sins except You.",
            virtueArabic = "سيد الاستغفار: من قالها موقناً بها فمات من يومه أو ليلته دخل الجنة.",
            virtueEnglish = "Sayyid al-Istighfar (The Master Supplication for Forgiveness): Whoever recites it with conviction and dies that day or night enters Paradise.",
            referenceArabic = "رواه البخاري (6306)",
            referenceEnglish = "Sahih Al-Bukhari 6306"
        ),
        DailyDhikrItem(
            id = 5,
            textArabic = "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالإِسْلاَمِ دِينًا، وَبِمُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ نَبِيًّا.",
            textEnglishTransliteration = "Raditu billahi Rabban, wa bil-Islami dinan, wa bi-Muhammadin sallallahu 'alayhi wa sallama Nabiyya.",
            textEnglishTranslation = "I am pleased with Allah as my Lord, with Islam as my religion, and with Prophet Muhammad (peace be upon him) as my Prophet.",
            virtueArabic = "من قالها حين يصبح ويمسي كان حقاً على الله أن يرضيه يوم القيامة.",
            virtueEnglish = "Whoever recites this morning and evening, Allah has promised to make them pleased on the Day of Resurrection.",
            referenceArabic = "رواه أحمد (18967)",
            referenceEnglish = "Musnad Ahmad 18967"
        ),
        DailyDhikrItem(
            id = 6,
            textArabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا، وَرِزْقًا طَيِّبًا، وَعَمَلاً مُتَقَبَّلاً.",
            textEnglishTransliteration = "Allahumma inni as'aluka 'ilman nafi'an, wa rizqan tayyiban, wa 'amalan mutaqabbala.",
            textEnglishTranslation = "O Allah, I ask You for beneficial knowledge, wholesome sustenance, and deeds that are accepted.",
            virtueArabic = "كان النبي ﷺ يدعو بها بعد صلاة الفجر كل يوم.",
            virtueEnglish = "The Prophet (peace be upon him) used to recite this every morning after Fajr prayer.",
            referenceArabic = "رواه ابن ماجه (925)",
            referenceEnglish = "Sunan Ibn Majah 925"
        ),
        DailyDhikrItem(
            id = 7,
            textArabic = "بِسْمِ اللَّهِ الَّذِي لاَ يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الأَرْضِ وَلاَ فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ.",
            textEnglishTransliteration = "Bismillahi alladhi la yadurru ma'asmihi shay'un fil-ardi wa la fis-sama'i wa Huwas-Sami'ul-'Alim.",
            textEnglishTranslation = "In the Name of Allah, with Whose Name nothing can cause harm in the earth or in the heavens, and He is the All-Hearing, All-Knowing.",
            virtueArabic = "من قالها ثلاث مرات صباحاً ومساءً لم يضره شيء.",
            virtueEnglish = "Whoever recites it three times morning and evening will not be harmed by anything.",
            referenceArabic = "رواه الترمذي (3388)",
            referenceEnglish = "Sunan At-Tirmidhi 3388"
        )
    )

    fun getTodayDhikr(): DailyDhikrItem {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val index = (dayOfYear - 1) % dailyDhikrs.size
        return dailyDhikrs[index]
    }

    fun getDhikrById(id: Int): DailyDhikrItem? {
        return dailyDhikrs.find { it.id == id }
    }
}
