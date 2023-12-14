package org.toitlang.intellij.sdk

import com.intellij.openapi.util.SystemInfo
import com.intellij.util.system.CpuArch
import com.jetbrains.rd.util.string.println
import kotlinx.serialization.json.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun fetchVersions(): List<ToitReleaseVersion> {
    // fetch versions from github
    // https://api.github.com/repos/toitlang/toit/releases?per_page=100&page=1
    val client = HttpClient.newHttpClient()
    var page = 1
    val result = mutableListOf<ToitReleaseVersion>()
    while (true) {
        val request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/repos/toitlang/toit/releases?per_page=100&page=$page"))
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        // Parse JSON response
        val elm = Json.parseToJsonElement(response.body())
        if (elm is JsonArray) {
            if (elm.size == 0) break
            for (release in elm) {
                val tagName = release.jsonObject["tag_name"]
                if (tagName != null) {
                    result.add(ToitReleaseVersion(tagName.jsonPrimitive.content, release))
                }
            }
        }
        page++
    }
    return result
}
class ToitReleaseVersion(val version: String, val release: JsonElement) {
    val platformAsset: String?
        get() {
            val archOs = when {
                SystemInfo.isLinux   -> "linux"
                SystemInfo.isMac     -> "macos"
                SystemInfo.isWindows -> "windows"
                else -> error("Unsupported OS: ${SystemInfo.getOsNameAndVersion()}")
            }
            val assets = release.jsonObject["assets"]
            if (assets != null && assets is JsonArray) {
                for (asset in assets) {
                    val name = asset.jsonObject["name"]
                    if (name != null && name.jsonPrimitive.content.contains("toit-$archOs.tar.gz")) {
                        return asset.jsonObject["url"]!!.jsonPrimitive.content
                    }
                }
            }
            return null
        }
    override fun toString(): String {
        return version
    }
}