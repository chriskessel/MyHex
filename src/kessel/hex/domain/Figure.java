package kessel.hex.domain;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import kessel.hex.map.Location;

import java.lang.reflect.Type;

/** Represents an figure in the game, such as an agent, wizard, etc. */
public abstract class Figure extends GameItem
{
  // Figures have a level of ability.
  protected int _level = 1;

  // Figures work from a base of operations, such as a population center or army.
  protected GameItem _base;
  protected int _jsonBaseId;

  // How far the figure can move normally (e.g. a move order, not things like teleport).
  protected int _range = 10;

  protected Figure() { super(); } // GSON only.
  protected Figure( int id, String name, int turnSeen, GameItem base )
  {
    super( id, name, turnSeen, base != null ? base.getLocation() : null );
    _base = base;
  }

  protected Figure( int id, String name, int turnSeen, GameItem base, Player owner )
  {
    super( id, name, turnSeen, base != null ? base.getLocation() : null, owner );
    _base = base;
  }

  /** @return the level of success required to assassinate the figure. */
  public abstract int getAssassinationDifficulty();

  public int getLevel() { return _level; }

  public void setLevel( int level ) { _level = level; }

  public void incrementLevel()
  {
    if ( getLevelCap() >= _level + 1 )
    {
      _level++;
    }
  }

  public abstract int getLevelCap();

  @Override // Defer to the base. You can't set a Figure's location, you have to change its base.
  public Location getLocation()
  { return _base._location; }

  public void setLocation( Location location ) { /* ignore if called */ }

  public GameItem getBase() { return _base; }

  public void setBase( GameItem base )
  {
    _base = base;
    super.setLocation( base.getLocation() );
  }

  public int getRange() { return _range; }

  public void setRange( int range ) { _range = range; }

  public boolean isInPopCenter() { return (_base != null) && (_base instanceof PopCenter); }

  @SuppressWarnings({ "UnusedDeclaration" })
  public boolean isInArmy() { return (_base != null) && (_base instanceof Army); }

  /** @return a modifier for this figure's training cost. */
  public abstract double getTrainingCostModifier();

  /** @return a sum of the total levels. E.g. level 1=1, 2=3, 3=6, 4=10, etc. */
  public int getTotalLevels()
  {
    int total = 0;
    for ( int i = 1; i <= _level; i++ )
    {
      total += i;
    }
    return total;
  }

  /**
   * Used when a figure is forced to move, such as escaping capture or a capitol relocation.
   *
   * @param target the new base
   */
  public abstract void forceMove( PopCenter target );

  public static class MyJsonAdapter<X extends Figure> extends GameItem.MyJsonAdapter<X>
  {
    private static final String LEVEL = "level";
    private static final String BASE_ID = "baseId";
    private static final String RANGE = "range";

    /** serialize jut those bits relevant to Figure. */
    public void serialize( X figure, JsonObject jsonResponse, Type type, JsonSerializationContext context )
    {
      super.serialize( figure, jsonResponse, type, context );
      jsonResponse.add( LEVEL, context.serialize( figure.getLevel() ) );
      jsonResponse.add( BASE_ID, context.serialize( figure.getBase().getId() ) );
      jsonResponse.add( RANGE, context.serialize( figure.getRange() ) );
    }

    /** deserialize jut those bits relevant to Figure. */
    public void doDeserialize( X figure, JsonElement jsonOrder, JsonDeserializationContext context )
      throws JsonParseException
    {
      super.doDeserialize( figure, jsonOrder, context );
      figure._level = context.deserialize( jsonOrder.getAsJsonObject().get( LEVEL ), Integer.class );
      figure._jsonBaseId = context.deserialize( jsonOrder.getAsJsonObject().get( BASE_ID ), Integer.class );
      figure._range = context.deserialize( jsonOrder.getAsJsonObject().get( RANGE ), Integer.class );
    }
  }

  public void fixDeserializationReferences( Game game )
  {
    super.fixDeserializationReferences( game );
    _base = game.getItem( _jsonBaseId );
    boolean b = true;
  }
}
