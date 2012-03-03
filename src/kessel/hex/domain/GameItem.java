package kessel.hex.domain;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import kessel.hex.map.Location;
import kessel.hex.orders.Order;
import kessel.hex.util.Tuple;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Represents an item in the game as seen from a particular viewpoint (usually a Player). Since this represents the view of an item, there
 * may be multiple GameItems to represent different views of the true underlying game item. For example, a Player may see a Town on turn 3
 * and his Town GameItem will reflect his view at that point in time.
 */
@SuppressWarnings({ "RawUseOfParameterizedType" })
public abstract class GameItem
{
  /** Indicates a particular value, like a power level, is unknown. */
  public static final int VALUE_UNKNOWN = -1;

  /** Indicates the unique id of the item is unknown. This would typically happen when someone only knows partial item info. */
  public static final int UNKNOWN_ID = 0;

  /** Every item must have a unique id. */
  protected int _id;

  /** The descriptive name (e.g. "Princess Penelope"). */
  protected String _name;

  /** Whether the group is currently invisible. */
  protected boolean _isInvisible = false;

  /** What turn this item was seen. */
  protected int _turnSeen;

  /** Where the item is. */
  protected Location _location;

  /** Who owns the item. The id is serialized, the Player is initialized by the owner. */
  protected Player _owner = Player.UNOWNED;

  /** A description of what the item did last turn. Useful in turn reports. */
  protected List<String> _lastTurnStatus = new ArrayList<>();

  /** Orders executed by this item this turn (the item was the acting subject of the order, such as a diplomat). */
  protected List<Order<GameItem>> _ordersExecuted = new ArrayList<>();

  // References exist only to support deserialization. Reference resolution is handled by the Region's owning object.
  Tuple _jsonLocationTuple;
  String _jsonOwnerName;


  protected GameItem() {} // GSON requires this.

  protected GameItem( int id, String name, int turnSeen, Location location )
  {
    _id = id;
    _name = name;
    _turnSeen = turnSeen;
    _location = location;
    _ordersExecuted = new ArrayList<>();
  }

  protected GameItem( int id, String name, int turnSeen, Location location, Player owner )
  {
    this( id, name, turnSeen, location );
    _owner = owner;
    _jsonOwnerName = owner.getName();
  }

  public boolean hasOrders() { return !getNextTurnOrders().isEmpty(); }

  public int getTurnSeen() { return _turnSeen; }

  public void setTurnSeen( int turnSeen ) { _turnSeen = turnSeen; }

  public Location getLocation() { return _location; }

  public void setLocation( Location location ) { _location = location; }

  public String getName() { return _name; }

  public int getId() { return _id; }

  public Player getOwner() { return _owner; }

  public void setOwner( Player owner )
  {
    _owner = owner;
    _jsonOwnerName = owner.getName();
  }

  public String getOwnerName() { return _owner.getName(); }

  public String getOwnerShortName() { return new StringTokenizer( _owner.getName() ).nextToken(); }

  @SuppressWarnings({ "RawUseOfParameterizedType", "unchecked" })
  public void addOrderExecuted( Order order )
  {
    _ordersExecuted.add( order );
    _lastTurnStatus.add( order.getShortDescription() );
  }

  public List<Order<GameItem>> getOrdersExecuted() { return _ordersExecuted; }

  @SuppressWarnings({ "RawUseOfParameterizedType", "unchecked" })
  public void addNextTurnOrder( Order order )
  {
    _owner.addOrder( order );
  }

  public List<Order<GameItem>> getNextTurnOrders()
  {
    return _owner.getNextTurnOrders( this );
  }

  public void removeNextTurnOrder( Order order )
  {
    _owner.removeOrder( order );
  }

  protected static <T extends GameItem> T getGameItem( List<T> gameItems, int itemId )
  {
    for ( T gameItem : gameItems )
    {
      if ( gameItem.getId() == itemId )
      {
        return gameItem;
      }
    }
    return null;
  }

  /** @return true if the pop is known to be owned by a player. */
  public boolean isOwned()
  {
    return !_jsonOwnerName.equals( Player.UNOWNED.getName() ) && !_jsonOwnerName.equals( Player.UNKNOWN.getName() );
  }

  /** Return a very short status name of an object. So, for a level 7 town it'd say something like: "T7". */
  public abstract String getShortStatusName();

  /** Return a descriptive status name of an object. So, for a level 7 town it'd say something like: "Town(7)-Timberville". */
  public abstract String getLongStatusName();

  /** A description of the item and it's location. */
  public abstract String getDescription();

  /** @return a description of the item's action of interest last turn. */
  public List<String> getLastTurnStatus()
  { return _lastTurnStatus; }

  public void clearLastTurnStatus() { _lastTurnStatus.clear(); }

  public void setInvisible( boolean isInvisible ) { _isInvisible = isInvisible; }

  public boolean isInvisible() { return _isInvisible; }

  /** @return the orders this game item can issue. */
  public abstract List<Class> getAvailableOrders();

  public boolean equals( Object o )
  {
    if ( this == o ) return true;
    if ( o == null || getClass() != o.getClass() ) return false;
    GameItem gameItem = (GameItem) o;
    return _id == gameItem._id;
  }

  public int hashCode() { return _id; }


  /** Exists to support subclass deserialization, never registered with or invoked directly by GSON. */
  public static class MyJsonAdapter<X extends GameItem> implements JsonSerializer<X>, JsonDeserializer<X>
  {
    protected static final String CLASS = "class";
    protected static final String ID = "id";
    protected static final String NAME = "name";
    protected static final String INVISIBLE = "isInvisible";
    protected static final String TURN_SEEN = "turnSeen";
    protected static final String COORDINATE = "coordinate";
    protected static final String OWNER = "owner";
    protected static final String LAST_TURN_STATUS = "lastTurnStatus";

    /** serialize jut those bits relevant to GameItem. */
    public void serialize( X gameItem, JsonObject jsonResponse, Type type, JsonSerializationContext context )
    {
      jsonResponse.add( CLASS, context.serialize( gameItem.getClass().getName() ) );
      jsonResponse.add( ID, context.serialize( gameItem._id ) );
      jsonResponse.add( NAME, context.serialize( gameItem._name ) );
      jsonResponse.add( INVISIBLE, context.serialize( gameItem._isInvisible ) );
      jsonResponse.add( TURN_SEEN, context.serialize( gameItem._turnSeen ) );
      jsonResponse.add( COORDINATE, context.serialize( gameItem._location.getCoord() ) );
      jsonResponse.add( OWNER, context.serialize( gameItem._owner.getName() ) );
      jsonResponse.add( LAST_TURN_STATUS, context.serialize( gameItem._lastTurnStatus ) );
    }

    /** deserialize jut those bits relevant to GameItem. */
    public void doDeserialize( X gameItem, JsonElement jsonOrder, JsonDeserializationContext context )
      throws JsonParseException
    {
      gameItem._name = context.deserialize( jsonOrder.getAsJsonObject().get( NAME ), String.class );
      gameItem._id = context.deserialize( jsonOrder.getAsJsonObject().get( ID ), Integer.class );
      gameItem._isInvisible = context.deserialize( jsonOrder.getAsJsonObject().get( INVISIBLE ), Boolean.class );
      gameItem._turnSeen = context.deserialize( jsonOrder.getAsJsonObject().get( TURN_SEEN ), Integer.class );
      gameItem._jsonLocationTuple = context.deserialize( jsonOrder.getAsJsonObject().get( COORDINATE ), Tuple.class );
      gameItem._jsonOwnerName = context.deserialize( jsonOrder.getAsJsonObject().get( OWNER ), String.class );
      gameItem._lastTurnStatus = context.deserialize( jsonOrder.getAsJsonObject().get( LAST_TURN_STATUS ), new TypeToken<List<String>>(){}.getType() );
    }

    public X deserialize( JsonElement jsonElement, Type type, JsonDeserializationContext context )
      throws JsonParseException
    {
      String className = jsonElement.getAsJsonObject().get( "_class" ).getAsString();
      try
      {
        X gameItem = context.deserialize( jsonElement, Class.forName( className ) );
        return gameItem;
      }
      catch ( ClassNotFoundException e )
      {
        throw new RuntimeException( e );
      }
    }

    public JsonElement serialize( X x, Type type, JsonSerializationContext jsonSerializationContext )
    {
      // We can't mark the class abstract since we'll deserialize on GameItem, but serialization should
      // always happen in the overridden method in a GameItem subclass so this method shouldn't be invoked.
      throw new RuntimeException( "Shouldn't ever be called!" );
    }
  }

  protected static String getLevelString( int level )
  {
    return level == VALUE_UNKNOWN ? "?" : Integer.toString( level );
  }

  public void fixDeserializationReferences( Game game )
  {
    fixLocationDeserialization( game );
    fixOwnerDeserialization( game );
  }

  protected void fixLocationDeserialization( Game game )
  {
    _location = game.getMap().getLocation( _jsonLocationTuple );
    if ( _location == null ) { throw new RuntimeException( "Can't find location: " + _jsonLocationTuple ); }
  }

  private void fixOwnerDeserialization( Game game )
  {
    if ( _jsonOwnerName.equals( Player.UNKNOWN.getName() ) )
    {
      _owner = Player.UNKNOWN;
    }
    else if ( _jsonOwnerName.equals( Player.UNOWNED.getName() ) )
    {
      _owner = Player.UNOWNED;
    }
    else
    {
      _owner = game.getPlayer( _jsonOwnerName );
      if ( _owner == null ) { throw new RuntimeException( "Can't find owner: " + _jsonOwnerName ); }
    }
  }
}
