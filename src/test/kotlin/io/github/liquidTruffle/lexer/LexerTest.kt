package io.github.liquidTruffle.lexer

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.StringReader

class LexerTest {
	@Test
	fun lexerProducesTokens() {
		val src = "{{ name | upcase }}"
		val lexer = Lexer(src)
		val tokens = lexer.lex()
		
		// Basic verification that lexer produces tokens
		assertThat(tokens).isNotEmpty()
		
		// Assert for the correct order: VAR_OPEN, IDENT, PIPE, IDENT, VAR_CLOSE (no whitespace tokens)
		val tokenTypes = tokens.map { it.type }
		assertThat(tokenTypes).containsExactly(
			TokenType.VAR_OPEN,
			TokenType.IDENT,      // name
			TokenType.PIPE,       // |
			TokenType.IDENT,      // upcase
			TokenType.VAR_CLOSE,
			TokenType.EOF
		)
		
		// Verify the actual lexemes
		assertThat(tokens[1].lexeme).isEqualTo("name")
		assertThat(tokens[3].lexeme).isEqualTo("upcase")
	}

	@Test
	fun lexerHandlesSimpleText() {
		val src = "Hello World"
		val lexer = Lexer(src)
		val tokens = lexer.lex()
		
		assertThat(tokens).hasSize(2)
		assertThat(tokens[0].type).isEqualTo(TokenType.TEXT)
		assertThat(tokens[0].lexeme).isEqualTo("Hello World")
		assertThat(tokens.last().type).isEqualTo(TokenType.EOF)
	}

	@Test
	fun lexerHandlesVariableSyntax() {
		val src = "{{ variable }}"
		val lexer = Lexer(src)
		val tokens = lexer.lex()
		
		assertThat(tokens).hasSizeGreaterThanOrEqualTo(4)
		assertThat(tokens[0].type).isEqualTo(TokenType.VAR_OPEN)
		assertThat(tokens[1].type).isEqualTo(TokenType.IDENT)
		assertThat(tokens[1].lexeme).isEqualTo("variable")
		assertThat(tokens[2].type).isEqualTo(TokenType.VAR_CLOSE)
		assertThat(tokens.last().type).isEqualTo(TokenType.EOF)
	}

	@Test
	fun lexerHandlesFilters() {
		val src = "{{ name | upcase | downcase }}"
		val lexer = Lexer(src)
		val tokens = lexer.lex()
		
		// Should have: VAR_OPEN, IDENT(name), PIPE, IDENT(upcase), PIPE, IDENT(downcase), VAR_CLOSE, EOF
		assertThat(tokens).hasSizeGreaterThanOrEqualTo(8)
		
		// Count pipes
		assertThat(tokens.map { it.type }.filter { it == TokenType.PIPE }).hasSize(2)
		
		// Count identifiers
		assertThat(tokens.map { it.type }.filter { it == TokenType.IDENT }).hasSize(3)
	}

	@Test
	fun lexerIgnoresWhitespaceTokensByDefault() {
		val src = "{{ variable }}"
		val lexer = Lexer(src)
		val tokens = lexer.lex()
		
		// Should not report whitespace tokens by default
		assertThat(tokens.map { it.type }).doesNotContain(TokenType.WHITESPACE)
		
		// Should have the other expected tokens
		assertThat(tokens.map { it.type }).containsExactly(
			TokenType.VAR_OPEN,
			TokenType.IDENT,
			TokenType.VAR_CLOSE,
			TokenType.EOF
		)
	}

	@Test
	fun lexerReportsWhitespaceTokensWhenEnabled() {
		val src = "{{ variable }}"
		val lexer = Lexer(src, reportWhitespaceTokens = true)
		val tokens = lexer.lex()
		
		// Should report whitespace tokens when explicitly enabled
		assertThat(tokens.map { it.type }).contains(TokenType.WHITESPACE)
		
		// Should have the expected tokens including whitespace
		assertThat(tokens.map { it.type }).containsExactly(
			TokenType.VAR_OPEN,
			TokenType.WHITESPACE,
			TokenType.IDENT,
			TokenType.WHITESPACE,
			TokenType.VAR_CLOSE,
			TokenType.EOF
		)
		
		// Should have more tokens than default behavior
		val defaultLexer = Lexer(src)
		val defaultTokens = defaultLexer.lex()
		assertThat(tokens).hasSizeGreaterThan(defaultTokens.size)
	}

	@Test
	fun lexerHandlesTextWithoutWhitespaceTokens() {
		val src = "Hello World"
		val lexer = Lexer(src)
		val tokens = lexer.lex()
		
		// Should not report whitespace tokens
		assertThat(tokens.map { it.type }).doesNotContain(TokenType.WHITESPACE)
		
		// Should have IDENT tokens for "Hello" and "World"
		val textTokens = tokens.filter { it.type == TokenType.TEXT }
		assertThat(textTokens).hasSize(1)
		assertThat(textTokens[0].lexeme).isEqualTo("Hello World")
	}

	@Test
	fun lexerWorksWithReader() {
		val src = "{{ variable | filter }}"
		val reader = StringReader(src)
		val lexer = Lexer(reader)
		val tokens = lexer.lex()
		
		// Should produce the same tokens as String constructor
		val stringLexer = Lexer(src)
		val stringTokens = stringLexer.lex()
		
		assertThat(tokens).hasSameSizeAs(stringTokens)
		assertThat(tokens.map { it.type }).isEqualTo(stringTokens.map { it.type })
		assertThat(tokens.map { it.lexeme }).isEqualTo(stringTokens.map { it.lexeme })
	}

	@Test
	fun lexerHandlesMultiLineInput() {
		val src = """{{ variable }}
Some text
{% if condition %}
More content
{% endif %}"""
		val lexer = Lexer(src)
		val tokens = lexer.lex()
		
		// Should handle multi-line input correctly
		assertThat(tokens).isNotEmpty()
		assertThat(tokens.last().type).isEqualTo(TokenType.EOF)
		
		// Should have all expected token types
		assertThat(tokens.map { it.type }).contains(TokenType.VAR_OPEN)
		assertThat(tokens.map { it.type }).contains(TokenType.VAR_CLOSE)
		assertThat(tokens.map { it.type }).contains(TokenType.TAG_OPEN)
		assertThat(tokens.map { it.type }).contains(TokenType.TAG_CLOSE)
		assertThat(tokens.map { it.type }).contains(TokenType.TEXT)
	}

	@Test
	fun lexerDfaTransitionsCorrectly() {
		val src = "{{ variable }}"
		val lexer = Lexer(src)
		
		// Process tokens and verify state transitions
		val tokens = lexer.lex()
		
		// Verify we got the expected tokens (no whitespace by default)
		assertThat(tokens.map { it.type }).containsExactly(
			TokenType.VAR_OPEN,
			TokenType.IDENT,
			TokenType.VAR_CLOSE,
			TokenType.EOF
		)
	}

	@Test
	fun lexerDfaHandlesAllTokenTypes() {
		val src = "{{ var | filter:arg,123 }}"
		val lexer = Lexer(src)
		val tokens = lexer.lex()
		
		// Verify we can handle all token types through DFA states (no whitespace by default)
		val expectedTypes = listOf(
			TokenType.VAR_OPEN,
			TokenType.IDENT,     // var
			TokenType.PIPE,      // |
			TokenType.IDENT,     // filter
			TokenType.COLON,     // :
			TokenType.IDENT,     // arg
			TokenType.COMMA,     // ,
			TokenType.NUMBER,    // 123
			TokenType.VAR_CLOSE,
			TokenType.EOF
		)
		
		assertThat(tokens.map { it.type }).containsExactlyElementsOf(expectedTypes)
	}

	@Test
	fun lexerDfaHandlesStringLiterals() {
		val src = """{{ var | filter:"hello world" }}"""
		val lexer = Lexer(src)
		val tokens = lexer.lex()
		
		// Find the string token
		val stringToken = tokens.find { it.type == TokenType.STRING }
		assertThat(stringToken).isNotNull
		assertThat(stringToken!!.lexeme).isEqualTo("hello world")
	}

	@Test
	fun lexerDfaHandlesTextContent() {
		val src = "Hello {{ variable }} World"
		val lexer = Lexer(src)
		val tokens = lexer.lex()
		
		// Should have text tokens for "Hello " and " World"
		val textTokens = tokens.filter { it.type == TokenType.TEXT }
		assertThat(textTokens).hasSize(2)
		assertThat(textTokens[0].lexeme).isEqualTo("Hello ")
		assertThat(textTokens[1].lexeme).isEqualTo(" World")
	}

	@Test
	fun lexerRecognizesKeywords() {
		val src = "{% if condition %}Hello{% endif %}"
		val lexer = Lexer(src)
		val tokens = lexer.lex()
		
		// Should recognize 'if' and 'endif' as keywords
		val keywordTokens = tokens.filter { it.type == TokenType.KEYWORD }
		assertThat(keywordTokens).hasSize(2)
		assertThat(keywordTokens[0].lexeme).isEqualTo("if")
		assertThat(keywordTokens[1].lexeme).isEqualTo("endif")
		
		// Should have other expected tokens
		assertThat(tokens.map { it.type }).contains(TokenType.TAG_OPEN)
		assertThat(tokens.map { it.type }).contains(TokenType.TAG_CLOSE)
		assertThat(tokens.map { it.type }).contains(TokenType.IDENT) // "condition"
		assertThat(tokens.map { it.type }).contains(TokenType.TEXT) // "Hello"
	}

	@Test
	fun lexerDistinguishesKeywordsFromIdentifiers() {
		val src = "{% for item in items %}{{ item.name }}{% endfor %}"
		val lexer = Lexer(src)
		val tokens = lexer.lex()
		
		// Should recognize 'for', 'in', and 'endfor' as keywords
		val keywordTokens = tokens.filter { it.type == TokenType.KEYWORD }
		assertThat(keywordTokens).hasSize(3)
		assertThat(keywordTokens.map { it.lexeme }).containsExactlyInAnyOrder("for", "in", "endfor")
		
		// Should recognize 'item', 'items', 'name' as identifiers
		val identTokens = tokens.filter { it.type == TokenType.IDENT }
		assertThat(identTokens).hasSize(4)
		assertThat(identTokens.map { it.lexeme }).containsExactlyInAnyOrder("item", "items", "item", "name")
	}

	@Test
	fun lexerRecognizesLogicalOperators() {
		val src = "{% if user and user.active %}{{ user.name }}{% endif %}"
		val lexer = Lexer(src)
		val tokens = lexer.lex()
		
		// Should recognize 'if', 'and', 'endif' as keywords
		val keywordTokens = tokens.filter { it.type == TokenType.KEYWORD }
		assertThat(keywordTokens).hasSize(3)
		assertThat(keywordTokens.map { it.lexeme }).containsExactlyInAnyOrder("if", "and", "endif")
		
		// Should recognize 'user', 'active', 'name' as identifiers
		val identTokens = tokens.filter { it.type == TokenType.IDENT }
		assertThat(identTokens).hasSize(5)
		assertThat(identTokens.map { it.lexeme }).containsExactlyInAnyOrder("user", "user", "active", "user", "name")
	}

	@Test
	fun lexerRecognizesBooleanKeywords() {
		val src = "{% if true or false %}{{ nil }}{% endif %}"
		val lexer = Lexer(src)
		val tokens = lexer.lex()
		
		// Should recognize 'if', 'true', 'or', 'false', 'nil', 'endif' as keywords
		val keywordTokens = tokens.filter { it.type == TokenType.KEYWORD }
		assertThat(keywordTokens).hasSize(6)
		assertThat(keywordTokens.map { it.lexeme }).containsExactly("if", "true", "or", "false", "nil", "endif")
	}

	@Test
	fun lexerRecognizesFilterKeywords() {
		val src = "{{ text | strip | escape }}"
		val lexer = Lexer(src)
		val tokens = lexer.lex()
		
		// Should recognize 'strip' and 'escape' as keywords (filter names)
		val keywordTokens = tokens.filter { it.type == TokenType.KEYWORD }
		assertThat(keywordTokens).hasSize(2)
		assertThat(keywordTokens.map { it.lexeme }).containsExactly("strip", "escape")
		
		// Should recognize 'text' as identifier
		val identTokens = tokens.filter { it.type == TokenType.IDENT }
		assertThat(identTokens).hasSize(1)
		assertThat(identTokens[0].lexeme).isEqualTo("text")
	}

	@Test
	fun lexerRecognizesComplexKeywordUsage() {
		val src = """{% for item in collection limit: 10 %}
			{% if item.active and item.price > 0 %}
				{{ item.name | strip | escape }}
			{% endif %}
		{% endfor %}"""
		val lexer = Lexer(src)
		val tokens = lexer.lex()
		
		// Should recognize multiple keywords
		val keywordTokens = tokens.filter { it.type == TokenType.KEYWORD }
		val keywordLexemes = keywordTokens.map { it.lexeme }
		
		assertThat(keywordLexemes).contains("for", "in", "limit", "if", "and", "endif", "endfor")
		assertThat(keywordLexemes).contains("strip", "escape")
		
		// Should recognize identifiers
		val identTokens = tokens.filter { it.type == TokenType.IDENT }
		val identLexemes = identTokens.map { it.lexeme }
		
		assertThat(identLexemes).contains("item", "collection", "active", "price", "name")
	}

    @Test
    fun lexerThrowsErrorInObject() {
        val src = "{{ % }}"
        val lexer = Lexer(src)
        assertThrows<LexerException> {
            lexer.lex()
        }
    }

    @Test
    fun lexerThrowsErrorInTag() {
        val src = "{% %%}"
        val lexer = Lexer(src)
        assertThrows<LexerException> {
            lexer.lex()
        }
    }
}
