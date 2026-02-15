import java.lang.System.getenv

rootProject.name = "core"

fun platform(name: String, vararg capabilities: String) =
    capabilities.forEach { include("$name-$it") }

getenv("MODULE")?.takeIf { it.isNotBlank() }?.let {
    include(it)
} ?: also {
    platform(
        "banking",
        "issuer",
        "acquirer"
    )

    platform(
        "clearing-settlement",
        "payment"
    )

    platform(
        "compliance-regulatory",
        "payment"
    )

    platform(
        "fraud-risk",
        "payment"
    )

    platform(
        "payment",
        "gateway",
        "processor",
        "card-network"
    )

    platform(
        "shopping",
        "merchant"
    )
}
