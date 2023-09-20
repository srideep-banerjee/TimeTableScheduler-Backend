package org.example.dao.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class Byte2DArraySerializer extends JsonSerializer<byte[][]> {
    @Override
    public void serialize(byte[][] bytes, JsonGenerator jgen, SerializerProvider serializerProvider)
            throws IOException {
        jgen.writeStartArray();

        for (byte[] bArr : bytes) {
            jgen.writeStartArray();
            for (byte b : bArr) {
                jgen.writeNumber(unsignedToBytes(b));
            }
            jgen.writeEndArray();
        }

        jgen.writeEndArray();
    }

    private static int unsignedToBytes(byte b) {
        return b & 0xFF;
    }
}
