package com.example.organizadorapps

object CategoryRules {

    /**
     * Fuente única de reglas de categorías.
     *
     * Se usa LinkedHashMap para respetar el orden visual en Home y Compact.
     * La misma app puede aparecer en varias categorías cuando coincide con varias reglas.
     */
    val rules: Map<String, List<String>> = linkedMapOf(
        "Bancos" to listOf(
            "bcp", "viabcp", "bbva", "interbank", "scotiabank", "banbif",
            "pichincha", "banco de la nación", "banco de la nacion", "bn", "financiero",
            "banco", "caja arequipa", "caja huancayo", "caja piura", "mi banco", "mibanco"
        ),

        "Billeteras digitales" to listOf(
            "yape", "plin", "tunki", "mercado pago", "mercadopago", "agora pay",
            "agorapay", "ligo", "prex", "wallet", "bim", "astro pay", "astropay"
        ),

        "Pagos y transferencias" to listOf(
            "paypal", "wise", "western union", "moneygram", "remitly", "transfer",
            "pago", "pagos", "pay", "global66"
        ),

        "Cripto e inversiones" to listOf(
            "binance", "coinbase", "bitso", "crypto", "tradingview", "etoro",
            "bitcoin", "stocks", "acciones", "inversion", "inversiones"
        ),

        "Streaming" to listOf(
            "youtube", "netflix", "disney", "prime video", "primevideo", "hbo",
            "max", "crunchyroll", "twitch", "kick", "stream", "streaming", "video"
        ),

        "Música" to listOf(
            "spotify", "youtube music", "yt music", "apple music", "deezer",
            "soundcloud", "shazam", "music", "musica", "música", "audio", "podcast"
        ),

        "Mensajería" to listOf(
            "whatsapp", "telegram", "messenger", "signal", "discord", "messages",
            "mensajes", "sms", "chat"
        ),

        "Redes sociales" to listOf(
            "instagram", "facebook", "tiktok", "twitter", "threads", "snapchat",
            "linkedin", "pinterest", "social"
        ),

        "Google" to listOf(
            "google", "gmail", "maps", "drive", "photos", "chrome", "meet",
            "calendar", "keep", "youtube", "docs", "sheets", "slides", "classroom"
        ),

        "Compras" to listOf(
            "mercado libre", "mercadolibre", "falabella", "ripley", "amazon",
            "aliexpress", "temu", "shein", "shop", "shopping", "compra", "compras"
        ),

        "Supermercados" to listOf(
            "tottus", "plaza vea", "plazavea", "wong", "metro", "vivanda",
            "supermercado", "market", "tienda"
        ),

        "Delivery" to listOf(
            "rappi", "pedidosya", "uber eats", "ubereats", "delivery", "kfc",
            "mcdonald", "mcdonalds", "burger", "bembos", "popeyes", "pizza"
        ),

        "Transporte" to listOf(
            "uber", "didi", "cabify", "indrive", "yango", "taxi", "beat"
        ),

        "Mapas y rutas" to listOf(
            "maps", "waze", "moovit", "ruta", "rutas", "gps", "mapa", "transit"
        ),

        "Productividad" to listOf(
            "notion", "office", "word", "excel", "powerpoint", "docs", "sheets",
            "slides", "todo", "tasks", "keep", "onenote", "notas", "notes"
        ),

        "Trabajo y reuniones" to listOf(
            "teams", "zoom", "meet", "slack", "outlook", "trello", "asana",
            "jira", "work", "trabajo", "reunion", "reunión", "mail", "correo"
        ),

        "Archivos y nube" to listOf(
            "files", "archivo", "archivos", "drive", "onedrive", "dropbox", "mega",
            "cloud", "nube", "file manager", "explorer"
        ),

        "Herramientas" to listOf(
            "settings", "config", "calculator", "calculadora", "clock", "reloj",
            "scanner", "scan", "weather", "clima", "calendar", "contact", "contactos"
        ),

        "Seguridad" to listOf(
            "authenticator", "1password", "bitwarden", "lastpass", "security",
            "seguridad", "antivirus", "password", "contraseña", "contrasena", "vpn"
        ),

        "Educación" to listOf(
            "classroom", "coursera", "udemy", "duolingo", "platzi", "canvas",
            "moodle", "blackboard", "learn", "aprender", "educacion", "educación"
        ),

        "Juegos" to listOf(
            "game", "games", "juego", "juegos", "roblox", "minecraft", "clash",
            "free fire", "freefire", "pubg", "pokemon", "steam", "riot"
        ),

        "Fotos y edición" to listOf(
            "photos", "gallery", "galeria", "galería", "camera", "camara", "cámara",
            "lightroom", "canva", "capcut", "snapseed", "picsart", "editor", "foto", "fotos"
        ),

        "Salud" to listOf(
            "fit", "fitness", "health", "salud", "samsung health", "mi fitness",
            "strava", "calm", "meditation", "meditacion", "meditación", "workout"
        ),

        "Viajes" to listOf(
            "booking", "airbnb", "expedia", "despegar", "latam", "sky airline",
            "jetsmart", "travel", "viaje", "viajes", "hotel", "hoteles", "flight", "vuelo"
        ),

        "Noticias y lectura" to listOf(
            "noticias", "news", "el comercio", "rpp", "gestion", "gestión",
            "kindle", "medium", "pocket", "reader", "lectura", "libros"
        )
    )
}
