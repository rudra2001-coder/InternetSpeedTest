package com.rudra.internetspeedtest.feature.neutrality

enum class ServiceCategory { STREAMING, SOCIAL, MESSAGING, DEVELOPER, CONTROL }

enum class ServiceEndpoint(
    val service: String,
    val endpoint: String,
    val category: ServiceCategory
) {
    YOUTUBE("YouTube", "https://redirector.googlevideo.com", ServiceCategory.STREAMING),
    NETFLIX("Netflix", "https://fast.com", ServiceCategory.STREAMING),
    FACEBOOK("Facebook", "https://graph.facebook.com", ServiceCategory.SOCIAL),
    INSTAGRAM("Instagram", "https://i.instagram.com", ServiceCategory.SOCIAL),
    WHATSAPP("WhatsApp", "https://media.whatsapp.net", ServiceCategory.MESSAGING),
    TELEGRAM("Telegram", "https://telegram.org", ServiceCategory.MESSAGING),
    GITHUB("GitHub", "https://github.com", ServiceCategory.DEVELOPER),
    CLOUDFLARE_CONTROL("Control", "https://speed.cloudflare.com/__down", ServiceCategory.CONTROL)
}
