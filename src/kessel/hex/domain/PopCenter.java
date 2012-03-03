package kessel.hex.domain;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import kessel.hex.map.ControlLevel;
import kessel.hex.map.Location;
import kessel.hex.map.Terrain;
import kessel.hex.orders.agent.HireAgent;
import kessel.hex.orders.diplomat.HireDiplomat;
import kessel.hex.orders.king.ImprovePopCenter;
import kessel.hex.orders.wizard.HireWizard;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** A populate center. */
public class PopCenter extends CombatCapableItem
{
  // Represents a pop center not on the board.
  public static final PopCenter THE_WILDS = new PopCenter( -2, "The Wilds", 0, Location.NOWHERE, PopType.Hamlet );

  static
  {
    THE_WILDS.setLevel( 0 );
  }

  public enum PopType
  {
    Hamlet, Town, City, Unknown
  }

  // A Pop has a level that determines its production, defense, diplomatic resistance, etc.
  private int _level = VALUE_UNKNOWN;

  // A Pop has a type, which never changes regardless of its level.
  private PopType _type;

  protected PopCenter() // GSON only
  {
    super();
    _type = PopType.Hamlet;
  }

  public PopCenter( PopCenter toClone )
  {
    this( toClone.getId(), toClone.getName(), toClone.getTurnSeen(), toClone.getLocation(), toClone.getType(), toClone.getOwner() );
    _level = toClone.getLevel();
  }

  public PopCenter( int id, String name, int turnSeen, Location location, PopType type )
  {
    super( id, name, turnSeen, location );
    _type = type;
  }

  public PopCenter( int id, String name, int turnSeen, Location location, PopType type, Player owner )
  {
    super( id, name, turnSeen, location, owner );
    _type = type;
  }

  public int produceResources()
  {
    int production = _level * Game.GOLD_GRANULARITY;
    return production;
  }

  public int getBaseCombatStrength()
  {
    TroopType defenders = determineTroopType();
    int strength = (int) (defenders.getSize() * getLevel() * defenders.getStrength());
    return strength;
  }

  private TroopType determineTroopType() { return _type == PopType.City ? TroopType.CITY_WATCH : TroopType.MILITIA; }

  public int getCombatStrength( Terrain terrain )
  {
    // Note: terrain is irrelevant for pop centers.
    int effectiveLevel = getEffectiveCombatLevel();
    TroopType defenders = determineTroopType();
    int strength = (int) (defenders.getSize() * effectiveLevel * defenders.getStrength());

    // Capitol's fight 50% harder.
    if ( this.equals( getOwner().getCapitol() ) )
    {
      strength = (int) (strength * 1.5);
    }
    return strength;
  }

  private int getEffectiveCombatLevel()
  {
    int effectiveLevel = _level;
    ControlLevel controlLevel = getOwner().getControlLevel( getLocation().getRegion() );
    if ( controlLevel == ControlLevel.Domination )
    {
      switch ( _type )
      {
        case Hamlet:
        case Unknown:
          break;
        case Town:
          effectiveLevel += 1;
          break;
        case City:
          effectiveLevel += 2;
          break;
      }
    }
    return effectiveLevel;
  }

  public void takeDamage( int hits )
  {
    // Population centers ignore damage taken.
  }

  public boolean isCapitol()
  {
    return (_owner != null) && equals( getOwner().getCapitol() );
  }

  public int getLevel() { return _level; }

  public void setLevel( int level ) { _level = level; }

  public void degradeLevel() { _level = Math.max( 1, _level - 1 ); }

  public void improveLevel() { _level++; }

  public PopType getType() { return _type; }

  public String getShortStatusName()
  {
    return _type.name() + "-" + getLevelString( _level ) + " (" + getOwnerShortName() + ")";
  }

  public String getLongStatusName()
  {
    return _type.name() + "-" + getLevelString( _level ) + " " +
           _name + " (" + getOwnerName() + ") " + (_level == VALUE_UNKNOWN ? "" : "Strength:" + getBaseCombatStrength());
  }

  public String getDescription()
  {
    return getLongStatusName() + " at " + _location.getCoord();
  }

  @SuppressWarnings({ "RawUseOfParameterizedType" })
  public List<Class> getAvailableOrders()
  {
    List<Class> orders = new ArrayList<>();
    orders.addAll( Arrays.asList( HireAgent.class, HireDiplomat.class, HireWizard.class, ImprovePopCenter.class ) );
    return orders;
  }

  public static class MyJsonAdapter extends GameItem.MyJsonAdapter<PopCenter>
  {
    private static final String LEVEL = "level";
    private static final String TYPE = "type";

    public JsonElement serialize( PopCenter pop, Type type, JsonSerializationContext context )
    {
      JsonObject jsonResponse = new JsonObject();
      super.serialize( pop, jsonResponse, type, context );
      jsonResponse.add( LEVEL, context.serialize( pop._level ) );
      jsonResponse.add( TYPE, context.serialize( pop._type ) );
      return jsonResponse;
    }

    public PopCenter deserialize( JsonElement jsonOrder, Type type, JsonDeserializationContext context )
      throws JsonParseException
    {
      PopCenter pop = new PopCenter();
      super.doDeserialize( pop, jsonOrder, context );
      pop._level = context.deserialize( jsonOrder.getAsJsonObject().get( LEVEL ), Integer.class );
      pop._type = context.deserialize( jsonOrder.getAsJsonObject().get( TYPE ), PopType.class );
      return pop;
    }
  }
}
