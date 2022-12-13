package lox

import lox.Lox.error
import lox.TokenType.*


internal class Scanner(private val source: String) {
    private val tokens: ArrayList<Token> = ArrayList()

    private var start = 0
    private var current = 0
    private var line = 1
    private var keywords: HashMap<String, TokenType> = HashMap<String, TokenType>();

    init {
        keywords["and"] = AND;
        keywords["class"] = CLASS;
        keywords["else"] = ELSE;
        keywords["false"] = FALSE;
        keywords["for"] = FOR;
        keywords["fun"] = FUN;
        keywords["if"] = IF;
        keywords["nil"] = NIL;
        keywords["or"] = OR;
        keywords["print"] = PRINT;
        keywords["return"] = RETURN;
        keywords["super"] = SUPER;
        keywords["this"] = THIS;
        keywords["true"] = TRUE;
        keywords["var"] = VAR;
        keywords["while"] = WHILE;
    }

    // Tokens

    private fun addToken(type: TokenType) {
        addToken(type, null)
    }

    private fun addToken(type: TokenType, literal: Any?) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    // Scanning

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current
            scanToken()

        }
        tokens.add(Token(EOF, "", null, line))
        return tokens
    }

    private fun scanToken() {
        when (val c: Char = advance()) {
            '(' -> addToken(LEFT_PAREN)
            ')' -> addToken(RIGHT_PAREN)
            '{' -> addToken(LEFT_BRACE)
            '}' -> addToken(RIGHT_BRACE)
            ',' -> addToken(COMMA)
            '.' -> addToken(DOT)
            '-' -> addToken(MINUS)
            '+' -> addToken(PLUS)
            ';' -> addToken(SEMICOLON)
            '*' -> addToken(STAR)
            '!' -> addToken(if (match('=')) BANG_EQUAL else BANG)
            '=' -> addToken(if (match('=')) EQUAL_EQUAL else EQUAL)
            '<' -> addToken(if (match('=')) LESS_EQUAL else LESS)
            '>' -> addToken(if (match('=')) GREATER_EQUAL else GREATER)
            '/' -> if (match('/')) {
                // A comment goes until the end of the line.
                while (peek() != '\n' && !isAtEnd()) advance();
            } else {
                addToken(SLASH);
            }
            '"' -> string()
            ' ', '\r', '\t' -> {}
            '\n' -> line++
            else -> {
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
            };
        }
    }

    private fun advance(): Char {
        return source[current++]
    }

    private fun peek(forward: Int = 0): Char {
        return if (isAtEnd(forward)) '\u0000' else source[current + forward]
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false
        current++
        return true
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }
        if (isAtEnd()) {
            error(line, "Unterminated string.")
            return
        }

        // The closing ".
        advance()

        // Trim the surrounding quotes.
        val value = source.substring(start + 1, current - 1)
        addToken(STRING, value)
    }

    private fun number() {
        while (isDigit(peek())) advance()

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peek(1))) {
            // Consume the "."
            advance()
            while (isDigit(peek())) advance()
        }
        addToken(NUMBER, source.substring(start, current).toDouble())
    }

    private fun identifier() {
        while (isAlphaNumeric(peek())) advance()

        val text = source.substring(start, current)
        var type = keywords[text]
        if (type == null) type = IDENTIFIER
        addToken(type)
    }

    private fun isAtEnd(forward: Int = 0): Boolean {
        return current + forward >= source.length
    }

    // Identifiers

    private fun isDigit(c: Char): Boolean {
        return c in '0'..'9'
    }

    private fun isAlpha(c: Char): Boolean {
        return c in 'a'..'z' || c in 'A'..'Z' || c == '_'
    }

    private fun isAlphaNumeric(c: Char): Boolean {
        return isAlpha(c) || isDigit(c)
    }
}