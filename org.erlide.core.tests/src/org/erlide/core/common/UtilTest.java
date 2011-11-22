package org.erlide.core.common;

import java.io.UnsupportedEncodingException;

import org.erlide.jinterface.util.TermParser;
import org.erlide.jinterface.util.TermParserException;
import org.junit.Assert;
import org.junit.Test;

import com.ericsson.otp.erlang.OtpErlang;
import com.ericsson.otp.erlang.OtpErlangBinary;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangString;

public class UtilTest {

    @Test
    public void testIoListToString_small() {
        final OtpErlangObject input = OtpErlang.mkList(new OtpErlangString(
                "hej"), new OtpErlangString("hoj"));
        final String result = Util.ioListToString(input, 10);
        final String expected = "hejhoj";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testIoListToString_large1() {
        final OtpErlangObject input = OtpErlang.mkList(new OtpErlangString(
                "hej"), new OtpErlangString("hoj"));
        final String result = Util.ioListToString(input, 4);
        final String expected = "hejh... <truncated>";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testIoListToString_large2() {
        final OtpErlangObject input = OtpErlang.mkList(new OtpErlangString(
                "hej"), new OtpErlangString("hoj"));
        final String result = Util.ioListToString(input, 6);
        final String expected = "hejhoj";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testIsTag_number() throws TermParserException {
        final OtpErlangObject input = TermParser.getParser().parse("3");
        Assert.assertEquals(false, Util.isTag(input, "ok"));
    }

    @Test
    public void testIsTag_good_atom() throws TermParserException {
        final OtpErlangObject input = TermParser.getParser().parse("ok");
        Assert.assertEquals(true, Util.isTag(input, "ok"));
    }

    @Test
    public void testIsTag_wrong_atom() throws TermParserException {
        final OtpErlangObject input = TermParser.getParser().parse("okx");
        Assert.assertEquals(false, Util.isTag(input, "ok"));
    }

    @Test
    public void testIsTag_tuple_int() throws TermParserException {
        final OtpErlangObject input = TermParser.getParser().parse("{3,9}");
        Assert.assertEquals(false, Util.isTag(input, "ok"));
    }

    @Test
    public void testIsTag_tuple_good_atom() throws TermParserException {
        final OtpErlangObject input = TermParser.getParser().parse("{ok, 9}");
        Assert.assertEquals(true, Util.isTag(input, "ok"));
    }

    @Test
    public void testIsTag_tuple_wrong_atom() throws TermParserException {
        final OtpErlangObject input = TermParser.getParser().parse("{okx, 9}");
        Assert.assertEquals(false, Util.isTag(input, "ok"));
    }

    @Test
    public void stringValue_1() throws TermParserException {
        final OtpErlangObject input = TermParser.getParser().parse(
                "\"a string\"");
        final String expected = "a string";
        final String actual = Util.stringValue(input);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void stringValue_2() throws TermParserException {
        final OtpErlangObject input = TermParser.getParser().parse("[]");
        final String expected = "";
        final String actual = Util.stringValue(input);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void stringValue_3() throws TermParserException {
        final OtpErlangObject input = TermParser.getParser()
                .parse("[51,52,53]");
        final String expected = "345";
        final String actual = Util.stringValue(input);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void stringValue_4() throws TermParserException {
        final OtpErlangObject input = new OtpErlangBinary(new byte[] { 51, 52,
                53 });
        final String expected = "345";
        final String actual = Util.stringValue(input);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void stringValue_5() throws TermParserException,
            UnsupportedEncodingException {
        final byte[] bytes = new byte[] { 197 - 256, 246 - 256 };
        final OtpErlangObject input = new OtpErlangBinary(bytes);
        final byte[] expected = bytes;
        final String actual = Util.stringValue(input);
        Assert.assertArrayEquals(expected, actual.getBytes("ISO-8859-1"));
    }

    @Test
    public void stringValue_6() throws TermParserException,
            UnsupportedEncodingException {
        final byte[] bytes = new byte[] { (byte) 0xE8, (byte) 0x8F, (byte) 0xAF };
        final OtpErlangObject input = new OtpErlangBinary(bytes);
        final byte[] expected = bytes;
        final String actual = Util.stringValue(input);
        Assert.assertArrayEquals(expected, actual.getBytes("UTF-8"));
    }

}
