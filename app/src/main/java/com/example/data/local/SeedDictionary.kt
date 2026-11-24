package com.example.data.local

object SeedDictionary {

    val arabicCoreWords: List<String> = listOf(
        "السلام", "عليكم", "ورحمة", "الله", "وبركاته", "مرحبا", "أهلا", "وسهلا", "صباح", "الخير",
        "مساء", "النور", "شكرا", "لك", "جزيلا", "عفوا", "من", "فضلك", "لو", "سمحت",
        "كيف", "حالك", "الحمد", "لله", "بخير", "ممتاز", "رائع", "جميل", "جدا", "نعم",
        "لا", "ربما", "حسنا", "بالتأكيد", "طبعا", "موافق", "أعتذر", "آسف", "لا", "بأس",
        "إن", "شاء", "ما", "في", "على", "إلى", "عن", "مع", "هذا", "هذه",
        "ذلك", "تلك", "الذي", "التي", "الذين", "هنا", "هناك", "الآن", "اليوم", "غدا",
        "أمس", "دائما", "أبدا", "أحيانا", "كثيرا", "قليلا", "كل", "بعض", "جميع", "فقط",
        "أريد", "أحتاج", "يمكنني", "هل", "يمكنك", "مساعدتي", "أرسل", "الرسالة", "العمل", "المشروع",
        "التطبيق", "لوحة", "المفاتيح", "الذكاء", "الاصطناعي", "الترجمة", "التصحيح", "الإملائي", "النحوي", "النص",
        "اللغة", "العربية", "الإنجليزية", "سريع", "دقيق", "تحديث", "حفظ", "إلغاء", "تعديل", "مشاركة",
        "صديقي", "أخي", "أختي", "الأسرة", "الاجتماع", "الموعد", "الساعة", "الوقت", "المكان", "العنوان",
        "البريد", "الإلكتروني", "رقم", "الهاتف", "الاسم", "البيانات", "الملف", "الصورة", "الفيديو", "الرابط",
        "مشكلة", "حل", "سؤال", "إجابة", "فكرة", "ملاحظة", "تذكير", "مهم", "عاجل", "جاهز",
        "تم", "بنجاح", "قيد", "التنفيذ", "بانتظار", "ردك", "تحياتي", "مع", "خالص", "التقدير",
        "يسعدني", "تواصلك", "نلتقي", "قريبا", "أتمنى", "توفيق", "نجاح", "سعادة", "صحة", "عافية"
    ).distinct()

    val englishCoreWords: List<String> = listOf(
        "the", "be", "to", "of", "and", "a", "in", "that", "have", "I",
        "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
        "this", "but", "his", "by", "from", "they", "we", "say", "her", "she",
        "or", "an", "will", "my", "one", "all", "would", "there", "their", "what",
        "hello", "hi", "hey", "thanks", "thank", "please", "sorry", "yes", "no", "okay",
        "sure", "great", "awesome", "perfect", "good", "morning", "evening", "night", "today", "tomorrow",
        "yesterday", "meeting", "schedule", "project", "update", "message", "email", "send", "reply", "check",
        "confirm", "working", "ready", "done", "help", "need", "want", "know", "think", "understand",
        "correct", "grammar", "spelling", "translate", "language", "Arabic", "English", "keyboard", "smart", "quick",
        "important", "urgent", "note", "reminder", "address", "phone", "number", "link", "file", "document",
        "looking", "forward", "hearing", "soon", "regards", "best", "wishes", "welcome", "appreciate", "support",
        "available", "let", "me", "could", "should", "can", "about", "because", "between", "through"
    ).distinct()

    val frenchCoreWords: List<String> = listOf(
        "bonjour", "bonsoir", "merci", "beaucoup", "salut", "oui", "non", "accord", "pardon", "excusez",
        "comment", "allez", "vous", "bien", "très", "avec", "pour", "dans", "mais", "aussi",
        "demain", "aujourd'hui", "message", "projet", "travail", "aide", "anglais", "arabe", "français"
    )

    val spanishCoreWords: List<String> = listOf(
        "hola", "buenos", "días", "tardes", "gracias", "por", "favor", "perdón", "claro", "bueno",
        "cómo", "estás", "bien", "muy", "también", "mañana", "hoy", "trabajo", "mensaje", "ayuda"
    )

    val turkishCoreWords: List<String> = listOf(
        "merhaba", "selam", "teşekkürler", "lütfen", "evet", "hayır", "tamam", "nasılsın", "iyiyim", "günaydın",
        "iyi", "akşamlar", "bugün", "yarın", "mesaj", "yardım", "güzel", "harika", "çalışma", "proje"
    )

    val initialBigrams: List<Triple<String, String, String>> = listOf(
        Triple("السلام", "عليكم", "ar"),
        Triple("عليكم", "ورحمة", "ar"),
        Triple("ورحمة", "الله", "ar"),
        Triple("صباح", "الخير", "ar"),
        Triple("مساء", "الخير", "ar"),
        Triple("شكرا", "لك", "ar"),
        Triple("شكرا", "جزيلا", "ar"),
        Triple("كيف", "حالك", "ar"),
        Triple("إن", "شاء", "ar"),
        Triple("شاء", "الله", "ar"),
        Triple("مع", "خالص", "ar"),
        Triple("خالص", "التقدير", "ar"),
        Triple("thank", "you", "en"),
        Triple("good", "morning", "en"),
        Triple("good", "evening", "en"),
        Triple("how", "are", "en"),
        Triple("looking", "forward", "en"),
        Triple("let", "me", "en"),
        Triple("best", "regards", "en")
    )
}
