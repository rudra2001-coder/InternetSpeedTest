package com.rudra.internetspeedtest.core.ui

data class TooltipContent(
    val title: String,
    val content: String
)

object NetworkEducationContent {
    val bufferbloat = TooltipContent(
        title = "What is Bufferbloat?",
        content = "When your router buffers too much data, creating lag spikes under load. This causes video calls to freeze and games to lag even with 'fast' internet."
    )

    val jitter = TooltipContent(
        title = "What is Jitter?",
        content = "Jitter measures the variability in ping over time. Low jitter (<10ms) means stable connection. High jitter causes stuttering in video calls and gaming."
    )

    val packetLoss = TooltipContent(
        title = "What is Packet Loss?",
        content = "Packet loss occurs when data packets fail to reach their destination. Even 1% loss can significantly impact video calls and real-time applications."
    )

    val confidence = TooltipContent(
        title = "Confidence Score",
        content = "Our confidence score (0-100) reflects how reliable this test result is. High variance, packet loss, or severe bufferbloat reduce the score."
    )

    val threadCount = TooltipContent(
        title = "Parallel Connections",
        content = "We use multiple parallel connections to fully saturate your internet link. More connections help measure high-speed connections more accurately."
    )
}
