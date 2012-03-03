package kessel.hex.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicInteger;

/** Serialization support for AtomicInteger. */
public class AtomicIntegerJsonAdapter implements JsonDeserializer<AtomicInteger>, JsonSerializer<AtomicInteger>
{
  public AtomicInteger deserialize( JsonElement jsonElement, Type type, JsonDeserializationContext context )
    throws JsonParseException
  {
    AtomicInteger myInt = new AtomicInteger( jsonElement.getAsInt() );
    return myInt;
  }

  public JsonElement serialize( AtomicInteger atomicInteger, Type type, JsonSerializationContext context )
  {
    return context.serialize( atomicInteger.get() );
  }
}
