rootProject.name = "core"

fun platform(name: String, vararg capabilities: String) =
    capabilities.forEach { include("$name-$it") }

// ===== Banking =====
platform(
    "banking",
    "issuer",
    "acquirer"
)

// ===== Clearing =====
platform(
    "clearing-settlement",
    "payment"
)

// ===== Compliance =====
platform(
    "compliance-regulatory",
    "payment"
)

// ===== Fraud =====
platform(
    "fraud-risk",
    "payment"
)

// ===== Payment =====
platform(
    "payment",
    "gateway",
    "processor",
    "card-network"
)

// ===== Shopping =====
platform(
    "shopping",
    "merchant"
)
