package com.christo.moneyplant.helpers;

import android.util.Xml;

import androidx.annotation.NonNull;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

public class AadhaarXMLParser {

    public final String[] ATTRIBUTES = {"uid", "name", "gender", "yob", "co", "house", "street", "dist", "subdist", "state", "pc", "dob" };
    private final HashMap <String, String> attributes;

    public AadhaarXMLParser (String in) throws XmlPullParserException, IOException {
        attributes = new HashMap<>();
        try (InputStream stream = new ByteArrayInputStream(in.getBytes())) {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(stream, null);
            int event = parser.getEventType();
            parser.nextTag();
            if (parser.getName().equals("PrintLetterBarcodeData")) {
                for (String attribute : ATTRIBUTES) {
                    String val = parser.getAttributeValue(null, attribute);
                    if (val == null) throw new XmlPullParserException("Attributes not found");
                    else attributes.put(attribute, val);
                }
            }
        }
    }

    public HashMap<String, String> getAttributes() {
        return attributes;
    }
    @NonNull
    public String getAttribute (String attribute) throws  XmlPullParserException {
        String value = attributes.get(attribute);
        if (value == null) throw new XmlPullParserException("Attribute not found !");
        return value;
    }
}
