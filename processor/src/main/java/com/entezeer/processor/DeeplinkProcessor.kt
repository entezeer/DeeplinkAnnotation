package com.entezeer.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.writeTo

@OptIn(KotlinPoetKspPreview::class)
class DeeplinkProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private var isGenerated = false
    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (isGenerated) return emptyList()

        val symbols = resolver.getSymbolsWithAnnotation(DeeplinkActivity::class.qualifiedName!!)
        val activityLinks = mutableListOf<Pair<String, String>>()

        symbols.filterIsInstance<KSClassDeclaration>().forEach { classDeclaration ->
            val url = classDeclaration.annotations.first {
                it.shortName.asString() == "DeeplinkActivity"
            }.arguments.first().value.toString()

            val activityName = classDeclaration.qualifiedName!!.asString()
            activityLinks.add(url to activityName)

            logger.warn("Found Deeplink: $url -> $activityName")
        }

        if (activityLinks.isNotEmpty()) {
            generateDeeplinkHandler(activityLinks)
            isGenerated = true
        }

        return symbols.filterNot { it.validate() }.toList()
    }

    private fun generateDeeplinkHandler(activityLinks: List<Pair<String, String>>) {
        val packageName = "com.entezeer.generated"
        val className = "DeeplinkHandler"

        val navigateMethodBuilder = FunSpec.builder("navigateToActivity")
            .addParameter("context", ClassName("android.content", "Context"))
            .addParameter("deepLink", String::class)
            .returns(Boolean::class)
            .beginControlFlow("return when")


        activityLinks.forEach { (url, activityName) ->
            val (regexPattern, isDynamic) = generateUrlPattern(url)

            if (isDynamic) {
                navigateMethodBuilder.addStatement("deepLink.matches(%S.toRegex()) -> {", regexPattern)
            } else {
                navigateMethodBuilder.addStatement("deepLink == %S -> {", regexPattern)
            }

            navigateMethodBuilder
                .addStatement("val params = extractParameters(deepLink, %S)", url)
                .addStatement("val intent = Intent(context, %T::class.java)", ClassName.bestGuess(activityName))

            val parameters = extractParametersFromUrl(url)
            parameters.forEach { param ->
                navigateMethodBuilder.addStatement("intent.putExtra(%S, params[%S] ?: \"\")", param, param)
            }

            navigateMethodBuilder.addStatement("context.startActivity(intent)")
            navigateMethodBuilder.addStatement("true")
            navigateMethodBuilder.addStatement("}")
        }

        navigateMethodBuilder.addStatement("else -> false")
        navigateMethodBuilder.endControlFlow()

        val fileSpec = FileSpec.builder(packageName, className)
            .addImport("android.content", "Intent")
            .addType(
                TypeSpec.objectBuilder(className)
                    .addFunction(navigateMethodBuilder.build())
                    .addFunction(generateExtractParametersFunction())
                    .build()
            )
            .build()

        fileSpec.writeTo(codeGenerator, false)
    }

    private fun generateExtractParametersFunction(): FunSpec {
        return FunSpec.builder("extractParameters")
            .addParameter("url", String::class)
            .addParameter("pattern", String::class)
            .returns(Map::class.parameterizedBy(String::class, String::class))
            .addStatement("val regex = pattern.replace(Regex(\"\\\\{[^}]+\\\\}\"), \"(\\\\\\\\w+)\").toRegex()")
            .addStatement("val match = regex.matchEntire(url) ?: return emptyMap()")
            .addStatement("val keys = Regex(\"\\\\{([^}]+)\\\\}\").findAll(pattern).map { it.groupValues[1] }.toList()")
            .addStatement("return keys.zip(match.destructured.toList()).toMap()")
            .build()
    }

    private fun extractParametersFromUrl(url: String): List<String> {
        val regex = "\\{([^}]+)\\}".toRegex()
        return regex.findAll(url).map { it.groupValues[1] }.toList()
    }

    private fun generateUrlPattern(url: String): Pair<String, Boolean> {
        val regex = "\\{([^}]+)\\}".toRegex()
        val hasDynamicParameters = regex.containsMatchIn(url)

        return if (hasDynamicParameters) {
            val regexPattern = url.replace(regex, "([^&]+)")
                .replace("?", "\\?")
                .replace(".", "\\.")
                .replace("/", "\\/")

            regexPattern.toRegex().pattern to true
        } else {
            url to false
        }
    }
}


class DeeplinkProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return DeeplinkProcessor(environment.codeGenerator, environment.logger)
    }
}
