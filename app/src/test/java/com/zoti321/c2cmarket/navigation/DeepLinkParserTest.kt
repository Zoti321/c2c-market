package com.zoti321.c2cmarket.navigation

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class DeepLinkParserTest {

    @Test
    fun parse_validProductUri_returnsProduct() {
        val destination = DeepLinkParser.parse(Uri.parse("c2cmarket://app/product/5"))

        assertEquals(DeepLinkDestination.Product(5), destination)
    }

    @Test
    fun parse_negativeProductId_returnsProduct() {
        val destination = DeepLinkParser.parse(Uri.parse("c2cmarket://app/product/-3"))

        assertEquals(DeepLinkDestination.Product(-3), destination)
    }

    @Test
    fun parse_validOrderUri_returnsOrder() {
        val destination = DeepLinkParser.parse(Uri.parse("c2cmarket://app/order/12"))

        assertEquals(DeepLinkDestination.Order(12L), destination)
    }

    @Test
    fun parse_validChatUri_returnsChat() {
        val destination = DeepLinkParser.parse(Uri.parse("c2cmarket://app/chat/3"))

        assertEquals(DeepLinkDestination.Chat(3L), destination)
    }

    @Test
    fun parse_wrongScheme_returnsNull() {
        assertNull(DeepLinkParser.parse(Uri.parse("https://app/product/1")))
    }

    @Test
    fun parse_wrongHost_returnsNull() {
        assertNull(DeepLinkParser.parse(Uri.parse("c2cmarket://other/product/1")))
    }

    @Test
    fun parse_unknownPath_returnsNull() {
        assertNull(DeepLinkParser.parse(Uri.parse("c2cmarket://app/unknown/1")))
    }

    @Test
    fun parse_invalidId_returnsNull() {
        assertNull(DeepLinkParser.parse(Uri.parse("c2cmarket://app/product/abc")))
    }

    @Test
    fun productUri_roundtrip() {
        val uri = DeepLinkParser.productUri(42)

        assertEquals(DeepLinkDestination.Product(42), DeepLinkParser.parse(uri))
        assertEquals("c2cmarket://app/product/42", uri.toString())
    }

    @Test
    fun orderUri_roundtrip() {
        val uri = DeepLinkParser.orderUri(99L)

        assertEquals(DeepLinkDestination.Order(99L), DeepLinkParser.parse(uri))
        assertEquals("c2cmarket://app/order/99", uri.toString())
    }

    @Test
    fun chatUri_roundtrip() {
        val uri = DeepLinkParser.chatUri(7L)

        assertEquals(DeepLinkDestination.Chat(7L), DeepLinkParser.parse(uri))
        assertEquals("c2cmarket://app/chat/7", uri.toString())
    }

    @Test
    fun shareText_includesTitleAndProductUri() {
        val text = DeepLinkParser.shareText("Phone", 5)

        assertEquals("Phone\nc2cmarket://app/product/5", text)
    }
}
