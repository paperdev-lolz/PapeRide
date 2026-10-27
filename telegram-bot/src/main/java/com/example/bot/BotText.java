package com.example.bot;

import java.util.Locale;

enum BotText {
    WELCOME(
            "👋 *Welcome to PapeRide, %s!* 🚖\n_Fast, reliable & transparent rides across Tashkent._\n\n⚡ *What you can do:*\n• 📍 *Instant Booking* — Share your GPS pin or type an address\n• 🗺 *Live Tracking* — Watch your driver approach in real time\n• 💳 *Fair Pricing* — Economy, Comfort & Business rates in UZS\n• 📱 *Web Console & MiniApp* — Full interactive map experience\n\n🚀 *How to book:*\n1️⃣ Tap *🚕 Book a Ride* below or send /request\n2️⃣ Share your pickup 📍 and destination 🏁\n3️⃣ Confirm and track your trip!\n\n👇 *Tap a button below to get started:*",
            "👋 *Добро пожаловать в PapeRide, %s!* 🚖\n_Быстрые, надёжные и комфортные поездки по Ташкенту._\n\n⚡ *Наши возможности:*\n• 📍 *Быстрый заказ* — Отправьте геопозицию или напишите адрес\n• 🗺 *Живое отслеживание* — Наблюдайте за подачей авто на карте\n• 💳 *Честные цены* — Эконом, Комфорт и Бизнес в сумах (UZS)\n• 📱 *Веб-консоль и MiniApp* — Интерактивная карта прямо в Telegram\n\n🚀 *Как заказать:*\n1️⃣ Нажмите *🚕 Заказать поездку* ниже или отправьте /request\n2️⃣ Укажите точку посадки 📍 и пункт назначения 🏁\n3️⃣ Подтвердите и следите за поездкой онлайн!\n\n👇 *Выберите действие ниже:*",
            "👋 *PapeRide'ga xush kelibsiz, %s!* 🚖\n_Toshkent bo'ylab tezkor, ishonchli va qulay safarlar._\n\n⚡ *Xizmat imkoniyatlari:*\n• 📍 *Tezkor buyurtma* — Joylashuvni yuboring yoki manzilni yozing\n• 🗺 *Jonli kuzatuv* — Haydovchi kelishini real vaqtda kuzating\n• 💳 *Shaffof narxlar* — Start, Komfort va Biznes tariflari (so'mda)\n• 📱 *Veb-konsol va MiniApp* — To'liq interaktiv xarita Telegram ichida\n\n🚀 *Qanday buyurtma beriladi:*\n1️⃣ Quyidagi *🚕 Safar buyurtma qilish* tugmasini bosing yoki /request yuboring\n2️⃣ Olib ketish nuqtasi 📍 va boradigan manzilni 🏁 yuboring\n3️⃣ Tasdiqlang va haydovchini jonli xaritada kuzating!\n\n👇 *Boshlash uchun quyidagi tugmalardan birini tanlang:*"),
    HELP(
            "📋 *PapeRide — Command Center & Help* 🛠️\n\nHere are all available commands and features:\n\n🚕 *Rides & Booking*\n• /request — Book a new ride in seconds\n• /rides — Check current ride status & history\n• /cancel — Cancel an active ride or pending search\n• /estimate — Check pricing model & fare estimates\n\n🗺️ *Interactive Maps & Tracking*\n• /web — Open live web console & full tracking map\n• /miniapp — Open Telegram MiniApp inside Telegram\n\n⚙️ *Settings & Safety*\n• /language — Change language (EN / RU / UZ)\n• /emergency — Emergency assistance info\n• /contact — Support hotline & feedback\n• /start — Reopen the main welcome menu\n\n💡 *Pro Tips:*\n📌 *Fast Location:* Tap 📎 → *Location* to send your exact GPS pin!\n🏛️ *Landmarks:* You can type landmark names like _Amir Timur Square_ or _Airport_.\n👀 *Live Map:* Tap *Web Console* anytime to watch your driver's real-time movement!",
            "📋 *PapeRide — Центр помощи и список команд* 🛠️\n\nВсе доступные команды и возможности сервиса:\n\n🚕 *Поездки и заказы*\n• /request — Быстро заказать поездку\n• /rides — Статус текущей поездки и история заказов\n• /cancel — Отменить поиск или активную поездку\n• /estimate — Оценить стоимость поездки и тарифы\n\n🗺️ *Карты и отслеживание*\n• /web — Открыть Веб-консоль с живой картой Ташкента\n• /miniapp — Открыть интерактивный Telegram MiniApp\n\n⚙️ *Настройки и безопасность*\n• /language — Сменить язык (EN / RU / UZ)\n• /emergency — Экстренная связь и помощь\n• /contact — Поддержка и контакты диспетчерской\n• /start — Вернуться в главное меню\n\n💡 *Полезные советы:*\n📌 *Точный адрес:* Нажмите 📎 → *Геопозиция*, чтобы отправить GPS-точку!\n🏛️ *Поиск мест:* Можно просто написать _Сквер Амира Темура_ или _Аэропорт_.\n👀 *Живая карта:* Нажмите *Веб-консоль*, чтобы следить за машиной онлайн!",
            "📋 *PapeRide — Yordam va buyruqlar markazi* 🛠️\n\nXizmatdan foydalanish uchun barcha buyruqlar va imkoniyatlar:\n\n🚕 *Safarlar va buyurtma*\n• /request — Bir necha soniyada yangi safar buyurtma qilish\n• /rides — Joriy safar holati va safarlar tarixi\n• /cancel — Qidiruvni yoki faol safarni bekor qilish\n• /estimate — Safar narxini baholash va tariflar\n\n🗺️ *Xaritalar va kuzatuv*\n• /web — Toshkent jonli xaritasi bilan Veb-konsolni ochish\n• /miniapp — Telegram ichida MiniApp'ni ochish\n\n⚙️ *Sozlamalar va xavfsizlik*\n• /language — Tilni almashtirish (EN / RU / UZ)\n• /emergency — Favqulodda yordam ma'lumotlari\n• /contact — Qo'llab-quvvatlash xizmati va aloqa\n• /start — Asosiy menyuni qayta ochish\n\n💡 *Foydali maslahatlar:*\n📌 *Tezkor nuqta:* Aniq GPS nuqtani yuborish uchun 📎 → *Joylashuv*ni bosing!\n🏛️ *Mashhur joylar:* _Amir Temur xiyoboni_ yoki _Aeroport_ kabi nomlarni yozishingiz mumkin.\n👀 *Jonli xarita:* Haydovchi harakatini kuzatish uchun *Veb-konsol* tugmasini bosing!"),
    WEB_LINK(
            "🌐 *PapeRide Web Console & Live Map*\n\nTrack your rides in real time, view driver telematics, and use full interactive map features:\n👉 %s",
            "🌐 *Веб-консоль и живая карта PapeRide*\n\nОтслеживайте поездки в реальном времени, наблюдайте за машинами и пользуйтесь интерактивной картой:\n👉 %s",
            "🌐 *PapeRide veb-konsoli va jonli xarita*\n\nSafar va haydovchilarni jonli xaritada kuzating va to'liq interaktiv funksiyalardan foydalaning:\n👉 %s"),
    LANGUAGE_PROMPT(
            "🌐 Choose your language:",
            "🌐 Выберите язык:",
            "🌐 Tilni tanlang:"),
    LANGUAGE_CHANGED(
            "Language set to English.",
            "Язык изменён на русский.",
            "Til o'zbek tiliga o'zgartirildi."),
    PROMPT_PICKUP(
            "📍 *Where should we pick you up?*\n\nShare your current location, or tap 📎 → Location to choose a pin on the map. You can type an address first for a clearer label.",
            "📍 *Где вас забрать?*\n\nОтправьте своё местоположение или нажмите 📎 → Геопозиция, чтобы выбрать точку на карте. Сначала можно ввести адрес для подписи.",
            "📍 *Sizni qayerdan olib ketaylik?*\n\nJoriy joylashuvingizni yuboring yoki 📎 → Joylashuv orqali xaritadan nuqta tanlang. Aniq nom ko‘rinishi uchun avval manzilni yozishingiz mumkin."),
    SHARE_LOCATION(
            "📍 Share my location",
            "📍 Отправить геопозицию",
            "📍 Joylashuvimni yuborish"),
    SHARED_LOCATION(
            "Shared location",
            "Отправленная геопозиция",
            "Yuborilgan joylashuv"),
    PICKUP_NOTED(
            "✍️ Noted: %s\n\nNow share your current location or tap 📎 → Location to pin the pickup point on the map.",
            "✍️ Записано: %s\n\nТеперь отправьте своё местоположение или нажмите 📎 → Геопозиция, чтобы отметить точку посадки на карте.",
            "✍️ Qayd qilindi: %s\n\nEndi joriy joylashuvingizni yuboring yoki 📎 → Joylashuv orqali xaritada olib ketish nuqtasini belgilang."),
    DESTINATION_NOTED(
            "✍️ Noted: %s\n\nNow share your location or tap 📎 → Location to pin the destination on the map.",
            "✍️ Записано: %s\n\nТеперь отправьте геопозицию или нажмите 📎 → Геопозиция, чтобы отметить место назначения на карте.",
            "✍️ Qayd qilindi: %s\n\nEndi joylashuvingizni yuboring yoki 📎 → Joylashuv orqali xaritada manzilni belgilang."),
    PROMPT_DESTINATION(
            "📌 *Where do you want to go?*\n\nShare your location, tap 📎 → Location to choose a map pin, or type the address first for a clearer label.",
            "📌 *Куда вы хотите поехать?*\n\nОтправьте геопозицию, нажмите 📎 → Геопозиция, чтобы выбрать точку на карте, или сначала введите адрес для подписи.",
            "📌 *Qayerga bormoqchisiz?*\n\nJoylashuvni yuboring, xaritadan nuqta tanlash uchun 📎 → Joylashuvni bosing yoki aniq nom uchun avval manzilni yozing."),
    LOCATION_OR_ADDRESS(
            "Please send a location or type an address.",
            "Отправьте геопозицию или введите адрес.",
            "Joylashuvni yuboring yoki manzilni yozing."),
    RIDE_SUMMARY(
            "🚗 Ride Summary\n\n🟢 Pickup: %s\n🔴 Destination: %s\n\nDoes this look right?",
            "🚗 Детали поездки\n\n🟢 Откуда: %s\n🔴 Куда: %s\n\nВсё верно?",
            "🚗 Safar tafsilotlari\n\n🟢 Olib ketish: %s\n🔴 Manzil: %s\n\nHammasi to'g'rimi?"),
    PICKUP("Pickup", "Откуда", "Olib ketish"),
    DESTINATION("Destination", "Куда", "Manzil"),
    CONFIRM("✅ Confirm", "✅ Подтвердить", "✅ Tasdiqlash"),
    CANCEL("❌ Cancel", "❌ Отмена", "❌ Bekor qilish"),
    BTN_REQUEST_RIDE("🚕 Book a Ride", "🚕 Заказать поездку", "🚕 Safar buyurtma qilish"),
    BTN_MINIAPP("🚀 Open MiniApp", "🚀 Открыть MiniApp", "🚀 MiniApp'ni ochish"),
    BTN_WEB_CONSOLE("🌐 Web Console", "🌐 Веб-консоль", "🌐 Veb-konsol"),
    BTN_MY_RIDES("🧾 My Rides", "🧾 Мои поездки", "🧾 Safarlarim"),
    BTN_LANGUAGE("🌐 Language", "🌐 Сменить язык", "🌐 Tilni o'zgartirish"),
    BTN_HELP("❓ Help & Guide", "❓ Справка и команды", "❓ Yordam va qo'llanma"),
    BTN_MAIN_MENU("🏠 Main Menu", "🏠 Главное меню", "🏠 Asosiy menyu"),
    QUICK_ACTIONS(
            "✨ *Quick Actions:* Tap a button below to get started:",
            "✨ *Быстрые действия:* Нажмите кнопку ниже для старта:",
            "✨ *Tezkor amallar:* Boshlash uchun quyidagi tugmani bosing:"),
    AUTO_PICKUP_SET(
            "📍 *Pickup set to your location:*\n%s\n\n📌 Now share your location or tap 📎 → Location to pin your *destination*:",
            "📍 *Точка посадки установлена:*\n%s\n\n📌 Теперь отправьте геопозицию или нажмите 📎 → Геопозиция для выбора *места назначения*:",
            "📍 *Olib ketish nuqtasi belgilandi:*\n%s\n\n📌 Endi boradigan *manzilingizni* yuboring yoki xaritadan tanlang:"),
    MINIAPP_PROMPT(
            "🚀 *PapeRide Telegram MiniApp*\n\nTap below to open the interactive map, track active drivers, and order rides directly inside Telegram:",
            "🚀 *Telegram MiniApp PapeRide*\n\nНажмите ниже, чтобы открыть интерактивную карту, следить за водителями и заказывать поездки прямо в Telegram:",
            "🚀 *PapeRide Telegram MiniApp*\n\nTelegram ichida interaktiv xarita, haydovchilarni kuzatish va buyurtma berish uchun quyidagi tugmani bosing:"),
    USE_BUTTONS(
            "👆 Please use the buttons above to *Confirm* or *Cancel* your ride.",
            "👆 Используйте кнопки выше, чтобы *подтвердить* или *отменить* поездку.",
            "👆 Safarni *tasdiqlash* yoki *bekor qilish* uchun yuqoridagi tugmalardan foydalaning."),
    NUDGE(
            "Type /request to book a ride, /web for map, or /help to see all commands.",
            "Отправьте /request, чтобы заказать поездку, /web для карты, или /help для списка команд.",
            "Safar buyurtma qilish uchun /request, xarita uchun /web, yoki buyruqlar uchun /help yuboring."),
    NO_ACTIVE_REQUEST(
            "⚠️ No active ride request found. Send /request to start a new one.",
            "⚠️ Активный запрос не найден. Отправьте /request, чтобы создать новый.",
            "⚠️ Faol safar so'rovi topilmadi. Yangisini boshlash uchun /request yuboring."),
    NEED_BOTH_LOCATIONS(
            "⚠️ I need both points on the map 📍\n\nSend /request and share your pickup and destination locations.",
            "⚠️ Нужны обе точки на карте 📍\n\nОтправьте /request и поделитесь геопозицией посадки и назначения.",
            "⚠️ Xarita uchun ikkala nuqta ham kerak 📍\n\n/request yuboring va olib ketish hamda manzil joylashuvlarini ulashing."),
    POINTS_MUST_DIFFER(
            "⚠️ Pickup and destination are the same point. Send a different destination pin or address.",
            "⚠️ Точка подачи совпадает с местом назначения. Отправьте другую точку или адрес назначения.",
            "⚠️ Olib ketish va manzil bir xil nuqta. Boshqa manzil nuqtasi yoki manzilni yuboring."),
    PENDING_RIDE_REMINDER(
            "⏳ Your ride is still waiting for a driver. We'll message you as soon as one is assigned.",
            "⏳ Ваша поездка всё ещё ожидает водителя. Мы сообщим, как только он будет назначен.",
            "⏳ Safaringiz hali ham haydovchini kutmoqda. Haydovchi tayinlanganda sizga xabar beramiz."),
    RIDE_CONFLICT(
            "⚠️ You already have a ride in progress. Please wait for it to finish.",
            "⚠️ У вас уже есть активная поездка. Дождитесь её завершения.",
            "⚠️ Sizda allaqachon faol safar bor. U tugashini kuting."),
    BOOK_FAILED(
            "⚠️ Sorry, we couldn't book your ride. Please try again in a moment.",
            "⚠️ Не удалось заказать поездку. Попробуйте ещё раз чуть позже.",
            "⚠️ Safarni buyurtma qilib bo'lmadi. Birozdan so'ng qayta urinib ko'ring."),
    SERVICE_UNAVAILABLE(
            "⚠️ PapeRide is unavailable right now. Please try again in a moment.",
            "⚠️ PapeRide сейчас недоступен. Попробуйте ещё раз чуть позже.",
            "⚠️ PapeRide hozir ishlamayapti. Birozdan so'ng qayta urinib ko'ring."),
    RIDE_CONFIRMED(
            "✅ Ride confirmed! (booking #%s)\n\n🟢 %s: %s\n🔴 %s: %s\n\n🔍 Looking for a driver near you…\nWe'll notify you when a driver accepts your ride!",
            "✅ Поездка подтверждена! (заказ #%s)\n\n🟢 %s: %s\n🔴 %s: %s\n\n🔍 Ищем водителя рядом…\nМы сообщим, когда водитель примет заказ!",
            "✅ Safar tasdiqlandi! (buyurtma #%s)\n\n🟢 %s: %s\n🔴 %s: %s\n\n🔍 Yaqin atrofda haydovchi qidirilmoqda…\nHaydovchi buyurtmani qabul qilganda xabar beramiz!"),
    RIDE_CANCELLED_BUTTON(
            "❌ Ride cancelled.\n\nSend /request whenever you need a ride!",
            "❌ Поездка отменена.\n\nОтправьте /request, когда снова понадобится поездка!",
            "❌ Safar bekor qilindi.\n\nSafar kerak bo'lganda /request yuboring!"),
    CANCELLED_REQUEST(
            "❌ Ride request cancelled.\n\nSend /request whenever you're ready!",
            "❌ Запрос на поездку отменён.\n\nОтправьте /request, когда будете готовы!",
            "❌ Safar so'rovi bekor qilindi.\n\nTayyor bo'lganingizda /request yuboring!"),
    NO_REQUEST(
            "You have no active request.\n\nSend /request to book a ride.",
            "У вас нет активного запроса.\n\nОтправьте /request, чтобы заказать поездку.",
            "Sizda faol so'rov yo'q.\n\nSafar buyurtma qilish uchun /request yuboring."),
    NO_ACTIVE_RIDE(
            "You have no active ride to cancel.\n\nSend /request to book one.",
            "У вас нет активной поездки для отмены.\n\nОтправьте /request, чтобы заказать поездку.",
            "Bekor qilish uchun faol safaringiz yo'q.\n\nSafar buyurtma qilish uchun /request yuboring."),
    CANCELLED_RIDE_ID(
            "❌ Ride #%s has been cancelled.",
            "❌ Поездка #%s отменена.",
            "❌ #%s safar bekor qilindi."),
    CANCEL_FAILED(
            "⚠️ Could not cancel your ride right now. Please try again later.",
            "⚠️ Не удалось отменить поездку. Попробуйте позже.",
            "⚠️ Safarni hozir bekor qilib bo'lmadi. Keyinroq urinib ko'ring."),
    HISTORY_FAILED(
            "⚠️ Could not load your ride history right now. Please try again later.",
            "⚠️ Не удалось загрузить историю поездок. Попробуйте позже.",
            "⚠️ Safarlar tarixini yuklab bo'lmadi. Keyinroq urinib ko'ring."),
    HISTORY_EMPTY(
            "You have no ride requests yet.\n\nTap /request to book your first ride!",
            "У вас пока нет заказов.\n\nОтправьте /request, чтобы заказать первую поездку!",
            "Siz hali safar buyurtma qilmagansiz.\n\nBirinchi safar uchun /request yuboring!"),
    HISTORY_TITLE("🧾 Your recent rides", "🧾 Ваши последние поездки", "🧾 Oxirgi safarlaringiz"),
    BOOKING("booking #%s", "заказ #%s", "buyurtma #%s"),
    RIDE_DRIVER_FOUND("🚕 Driver found!", "🚕 Водитель найден!", "🚕 Haydovchi topildi!"),
    YOUR_DRIVER("Your driver", "Ваш водитель", "Haydovchingiz"),
    DRIVER_ON_WAY("Your driver is on the way!", "Водитель уже едет к вам!", "Haydovchi siz tomonga yo'l oldi!"),
    RIDE_STARTED(
            "🚗 Your ride has started. Sit back and enjoy the trip!",
            "🚗 Поездка началась. Устраивайтесь поудобнее!",
            "🚗 Safaringiz boshlandi. Maroqli yo'l tilaymiz!"),
    RIDE_COMPLETED("✅ Ride completed!", "✅ Поездка завершена!", "✅ Safar yakunlandi!"),
    THANKS_FOR_RIDING("Thanks for riding with PapeRide!", "Спасибо, что выбрали PapeRide!", "PapeRide'ni tanlaganingiz uchun rahmat!"),
    RIDE_CANCELLED_STATUS(
            "❌ Your ride was cancelled.",
            "❌ Ваша поездка отменена.",
            "❌ Safaringiz bekor qilindi."),
    REQUEST_AGAIN("Send /request whenever you need a ride!", "Отправьте /request, когда понадобится поездка!", "Safar kerak bo'lganda /request yuboring!"),
    ESTIMATE(
            "💰 Fare estimate: share pickup + destination to get a price quote before booking.",
            "💰 Оценочная стоимость: укажите отправку и пункт назначения, чтобы узнать цену перед заказом.",
            "💰 Narx baholash: olib ketish va manzilni yuboring, buyurtma berishdan oldin narxni bilishingiz mumkin."
    ),
    RATE(
            "⭐ How would you rate your last ride? (1–5 stars)",
            "⭐ Как бы вы оценили последнюю поездку? (1–5 звёзд)",
            "⭐ Oxirgi safaringizni qanday baholaysiz? (1–5 yulduz)"
    ),
    EMERGENCY(
            "🚨 Emergency: dial local emergency services immediately. Share your ride ID with support via /contact.",
            "🚨 Чрезвычайная ситуация: немедленно звоните в службу экстренной помощи. Сообщите ID поездки в поддержку через /contact.",
            "🚨 Havfsizlik: avvalroq mijoz xizmatiga qo'ng'iroch qiling. Safar ID'sini /contact orqali yuboring."
    ),
    SHARE(
            "📤 Share your ride details with friends or family.",
            "📤 Поделитесь данными поездки с друзьями или семьей.",
            "📤 Safar ma'lumotlarini do'stlar yoki xodislamingiz bilan ulashing."
    ),
    FAVORITE(
            "📍 Saved locations: home, work, and frequent spots come here.",
            "📍 Сохранённые места: дом, работа и частые точки.",
            "📍 Saqlangan joylar: uy, ish va tez-tez borgan joylar."
    ),
    KM("%s km", "%s км", "%s km");

    private final String english;
    private final String russian;
    private final String uzbek;

    BotText(String english, String russian, String uzbek) {
        this.english = english;
        this.russian = russian;
        this.uzbek = uzbek;
    }

    String format(BotLanguage language, Object... args) {
        String template = switch (language) {
            case RU -> russian;
            case UZ -> uzbek;
            case EN -> english;
        };
        return String.format(Locale.ROOT, template, args);
    }
}
