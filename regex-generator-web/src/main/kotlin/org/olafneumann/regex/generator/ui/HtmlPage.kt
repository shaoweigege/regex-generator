package org.olafneumann.regex.generator.ui

import org.olafneumann.regex.generator.regex.RecognizerCombiner
import org.olafneumann.regex.generator.regex.RecognizerMatch
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.Node
import kotlin.browser.document
import kotlin.dom.addClass
import kotlin.dom.clear
import kotlin.dom.removeClass


class HtmlPage(
    private val presenter: DisplayContract.Presenter
) : DisplayContract.View {
    private val RecognizerMatch.row: Int? get() = recognizerMatchToRow[this]
    private val RecognizerMatch.div: HTMLDivElement? get() = recognizerMatchToElements[this]

    private val textInput = getInputById(ID_INPUT_ELEMENT)
    private val textDisplay = getDivById(ID_TEXT_DISPLAY)
    private val rowContainer = getDivById(ID_ROW_CONTAINER)
    private val resultDisplay = getDivById(ID_RESULT_DISPLAY)
    private val buttonCopy = getButtonById(ID_BUTTON_COPY)
    private val checkOnlyMatches = getInputById(ID_CHECK_ONLY_MATCHES)
    private val checkCaseInsensitive = getInputById(ID_CHECK_CASE_INSENSITIVE)
    private val checkDotAll = getInputById(ID_CHECK_DOT_MATCHES_LINE_BRAKES)
    private val checkMultiline = getInputById(ID_CHECK_MULTILINE)

    private val recognizerMatchToRow = mutableMapOf<RecognizerMatch, Int>()
    private val recognizerMatchToElements = mutableMapOf<RecognizerMatch, HTMLDivElement>()

    private val Int.characterUnits: String get() = "${this}ch"

    init {
        textInput.addEventListener(EVENT_INPUT, { presenter.onInputChanges(inputText) })
        buttonCopy.addEventListener(EVENT_CLICK, { presenter.onButtonCopyClick() })
        checkCaseInsensitive.addEventListener(EVENT_INPUT, { presenter.onOptionsChange(options) })
        checkDotAll.addEventListener(EVENT_INPUT, { presenter.onOptionsChange(options) })
        checkMultiline.addEventListener(EVENT_INPUT, { presenter.onOptionsChange(options) })
        checkOnlyMatches.addEventListener(EVENT_INPUT, { presenter.onOptionsChange(options) })
    }

    private fun getDivById(id: String): HTMLDivElement {
        try {
            return document.getElementById(id) as HTMLDivElement
        } catch (e: ClassCastException) {
            throw RuntimeException("Unable to find div with id '$id'.", e)
        }
    }

    private fun getInputById(id: String): HTMLInputElement {
        try {
            return document.getElementById(id) as HTMLInputElement
        } catch (e: ClassCastException) {
            throw RuntimeException("Unable to find input with id '$id'.", e)
        }
    }

    private fun getButtonById(id: String): HTMLButtonElement {
        try {
            return document.getElementById(id) as HTMLButtonElement
        } catch (e: ClassCastException) {
            throw RuntimeException("Unable to find button with id '$id'.", e)
        }
    }

    override fun selectInputText() {
        textInput.select()
    }

    override var inputText: String
        get() = textInput.value
        set(value) { textInput.value = value}

    override var displayText: String
        get() = textDisplay.innerText
        set(value) { textDisplay.innerText = value }

    override var resultText: String
        get() = resultDisplay.innerText
        set(value) { resultDisplay.innerText = value }

    override fun showResults(matches: Collection<RecognizerMatch>) {
        // TODO remove CSS class iterator
        var index = 0
        val classes = listOf("primary", "success", "danger", "warning")
        fun getCssClass() = "bg-${classes[index++%classes.size]}"

        fun getElementTitle(match: RecognizerMatch) = "${match.recognizer.name} (${match.inputPart})"

        rowContainer.clear()
        recognizerMatchToRow.clear()
        recognizerMatchToElements.clear()

        // find the correct row for each match
        recognizerMatchToRow.putAll(distributeToRows(matches))
        // Create row elements
        val rowElements = (0..(recognizerMatchToRow.values.max()?:0)).map { createRowElement() }.toList()
        // Create match elements
        matches.forEach { match ->
            val rowElement = rowElements[match.row!!]
            val element = createMatchElement(rowElement)
            recognizerMatchToElements[match] = element
            element.addClass(getCssClass())
            element.style.width = match.inputPart.length.characterUnits
            element.style.left = match.range.first.characterUnits
            element.title = getElementTitle(match)
            element.addEventListener(EVENT_CLICK, { presenter.onSuggestionClick(match)})
        }
    }

    private fun distributeToRows(matches: Collection<RecognizerMatch>): Map<RecognizerMatch, Int> {
        val lines = mutableListOf<Int>()
        return matches.map { match ->
            val indexOfFreeLine = lines.indexOfFirst { it <= match.range.first }
            val line = if (indexOfFreeLine >= 0) indexOfFreeLine else { lines.add(0); lines.size - 1 }
            lines[line] = match.range.last + 1
            match to line
        }.toMap()
    }

    private fun createRowElement(): HTMLDivElement = createDivElement(rowContainer, CLASS_MATCH_ROW)

    private fun createMatchElement(parent: HTMLDivElement): HTMLDivElement = createDivElement(parent, CLASS_MATCH_ITEM)

    private fun createDivElement(parent: Node, vararg classNames: String): HTMLDivElement {
        val element = document.createElement(ELEMENT_DIV) as HTMLDivElement
        element.addClass(*classNames)
        parent.appendChild(element)
        return element
    }

    override fun select(match: RecognizerMatch, selected: Boolean) = match.div?.let { toggleClass(it, selected, CLASS_ITEM_SELECTED) }!!
    override fun disable(match: RecognizerMatch, disabled: Boolean) = match.div?.let { toggleClass(it, disabled, CLASS_ITEM_NOT_AVAILABLE) }!!

    private fun toggleClass(element: HTMLDivElement, selected: Boolean, className: String) {
        if (selected)
            element.addClass(className)
        else
            element.removeClass(className)
    }

    override val options: RecognizerCombiner.Options
        get() = RecognizerCombiner.Options(
            onlyPatterns = checkOnlyMatches.checked,
            caseSensitive = checkCaseInsensitive.checked,
            dotMatchesLineBreaks = checkDotAll.checked,
            multiline = checkMultiline.checked
        )
}