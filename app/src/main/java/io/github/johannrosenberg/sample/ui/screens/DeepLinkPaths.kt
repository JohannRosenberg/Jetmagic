package io.github.johannrosenberg.sample.ui.screens

object DeepLinkPaths {
    const val root = "/jetmagic"
    const val sample = "$root/sample"
    const val petInfo = "$sample/pet_info"
    const val deepLink = "$sample/deeplink"
    const val deepLinkWithRegex = "[%$sample/category/[d-m]/details%]"
}