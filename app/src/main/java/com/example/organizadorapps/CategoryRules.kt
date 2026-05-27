package com.example.organizadorapps

object CategoryRules {

    /**
     * Fuente única de reglas de categorías.
     *
     * El orden importa: primero van categorías frecuentes/específicas para evitar falsos positivos
     * como Banco Falabella en Compras, Alexa en Compras o Calculadora en Google.
     */
    val rules: Map<String, List<String>> = linkedMapOf(
        "Billeteras digitales" to listOf(
            "yape", "plin", "tunki", "mercado pago", "mercadopago",
            "agora pay", "agorapay", "ligo", "prex", "bim",
            "astro pay", "astropay", "wallet", "billetera"
        ),

        "Bancos" to listOf(
            "bcp", "viabcp", "bbva", "interbank", "scotiabank",
            "banbif", "pichincha", "banco de la nación", "banco de la nacion",
            "banco falabella", "banco ripley", "mibanco", "mi banco",
            "caja arequipa", "caja huancayo", "caja piura", "caja cusco",
            "banco financiero", "credicorp"
        ),

        "Redes sociales" to listOf(
            "instagram", "facebook", "tiktok", "twitter", "x corp",
            "threads", "snapchat", "linkedin", "pinterest", "kwai"
        ),

        "Mensajería" to listOf(
            "whatsapp", "telegram", "messenger", "signal", "discord",
            "messages", "mensajes", "sms", "chat"
        ),

        "IA" to listOf(
            "chatgpt", "openai", "gemini", "bard", "copilot",
            "claude", "perplexity", "poe", "grok", "deepseek",
            "meta ai", "character ai", "character.ai"
        ),

        "Delivery" to listOf(
            "rappi", "pedidosya", "pedidos ya", "uber eats", "ubereats",
            "delivery", "kfc", "mcdonald", "mcdonalds", "burger king",
            "bembos", "popeyes", "pizza hut", "dominos", "papa johns"
        ),

        "Transporte" to listOf(
            "uber", "didi", "cabify", "indrive", "in drive",
            "yango", "taxi", "beat", "sat escooter", "scooter"
        ),

        "Streaming" to listOf(
            "youtube", "netflix", "disney", "prime video", "primevideo",
            "hbo", "max", "crunchyroll", "twitch", "kick",
            "star+", "paramount", "pluto tv", "claro video", "movistar tv"
        ),

        "Música" to listOf(
            "spotify", "youtube music", "yt music", "apple music",
            "deezer", "soundcloud", "shazam", "tidal", "podcast"
        ),

        "Ecosistema Google" to listOf(
            "gmail", "google maps", "maps", "google drive", "drive",
            "google photos", "photos", "chrome", "google meet", "meet",
            "google calendar", "calendar", "keep", "google keep",
            "docs", "sheets", "slides", "classroom", "google home"
        ),

        "Compras" to listOf(
            "mercado libre", "mercadolibre", "falabella", "ripley",
            "amazon shopping", "aliexpress", "temu", "shein",
            "shop", "shopping", "compras"
        ),

        "Supermercados" to listOf(
            "tottus", "plaza vea", "plazavea", "wong", "metro",
            "vivanda", "supermercado", "supermarket"
        ),

        "Productividad" to listOf(
            "notion", "office", "word", "excel", "powerpoint",
            "docs", "sheets", "slides", "todo", "tasks",
            "onenote", "notas", "notes"
        ),

        "Trabajo y reuniones" to listOf(
            "teams", "zoom", "slack", "outlook", "trello",
            "asana", "jira", "work", "trabajo", "reunion",
            "reunión", "mail", "correo"
        ),

        "Archivos y nube" to listOf(
            "files", "archivo", "archivos", "file manager",
            "explorer", "onedrive", "dropbox", "mega", "cloud", "nube"
        ),

        "Herramientas" to listOf(
            "settings", "config", "calculator", "calculadora",
            "com.google.android.calculator", "clock", "reloj",
            "scanner", "scan", "weather", "clima",
            "contact", "contactos", "calendar"
        ),

        "Hogar inteligente" to listOf(
            "alexa", "amazon alexa", "google home", "mi home",
            "xiaomi home", "smartthings", "smart life", "tuya",
            "home assistant", "casa", "smart home"
        ),

        "Seguridad" to listOf(
            "authenticator", "microsoft authenticator", "google authenticator",
            "1password", "bitwarden", "lastpass", "security",
            "seguridad", "antivirus", "password", "contraseña",
            "contrasena", "vpn"
        ),

        "Educación" to listOf(
            "classroom", "coursera", "udemy", "duolingo", "platzi",
            "canvas", "moodle", "blackboard", "learn",
            "aprender", "educacion", "educación"
        ),

        "Juegos" to listOf(
            "angry birds", "rovio", "game", "games", "juego", "juegos",
            "roblox", "minecraft", "clash", "free fire", "freefire",
            "pubg", "pokemon", "steam", "riot", "brawl stars",
            "call of duty", "cod mobile", "candy crush"
        ),

        "Fotos y edición" to listOf(
            "gallery", "galeria", "galería", "camera", "camara",
            "cámara", "lightroom", "canva", "capcut", "snapseed",
            "picsart", "editor", "foto", "fotos"
        ),

        "Salud" to listOf(
            "fit", "fitness", "health", "salud", "samsung health",
            "mi fitness", "strava", "calm", "meditation",
            "meditacion", "meditación", "workout"
        ),

        "Viajes" to listOf(
            "booking", "airbnb", "expedia", "despegar", "latam",
            "sky airline", "jetsmart", "travel", "viaje",
            "viajes", "hotel", "hoteles", "flight", "vuelo"
        ),

        "Noticias y lectura" to listOf(
            "noticias", "news", "el comercio", "rpp", "gestion",
            "gestión", "kindle", "medium", "pocket",
            "reader", "lectura", "libros"
        )
    )

    val categoryExclusions: Map<String, List<String>> = mapOf(
        "Compras" to listOf(
            "banco falabella", "bancofalabella", "banco ripley",
            "alexa", "amazon alexa"
        ),
        "Ecosistema Google" to listOf(
            "calculator", "calculadora", "com.google.android.calculator"
        )
    )
}

