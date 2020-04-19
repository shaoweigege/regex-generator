package org.olafneumann.regex.generator

import javafx.application.Application
import org.olafneumann.regex.generator.views.HTML5View
import tornadofx.App

class HTML5App : App(HTML5View::class)

fun main(args: Array<String>) {
    RegexGeneratorProtocol.registerRegexGeneratorProtocol()
    Application.launch(HTML5App::class.java, *args)
}
