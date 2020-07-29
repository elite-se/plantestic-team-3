package de.unia.se.plantestic

import com.moandjiezana.toml.Toml

fun parseToml(toml: String): Map<String, Any> {
    return unnestTomlMap("", Toml().read(toml).toMap());
}

fun unnestTomlMap(prefix: String, tree: Map<String, Any>): Map<String, Any> {
    val resultMap = HashMap<String, Any>()
    for (entry in tree.keys) {
        val identifierPath = prefix + entry
        if (tree[entry] is Map<*, *>) {
            resultMap.putAll(unnestTomlMap("$identifierPath.", tree[entry] as Map<String, Any>));
        } else {
            resultMap[identifierPath] = tree[entry] ?: error("")
        }
    }
    return resultMap;
}