package kessel.hex.domain;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import kessel.hex.map.Location;
import kessel.hex.map.Terrain;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.lang.reflect.Type;
import java.util.List;

/** An individual army unit (a corp, brigade, platoon, whatever you want to call it). */
public class ArmyUnit extends CombatCapableItem
{
  // A generic unit to represent the fact a unit exists, but no details.
  public static final ArmyUnit UNKNOWN_UNIT = new ArmyUnit( TroopType.UNKNOWN_TROOP );

  private TroopType _troopType;
  private int _casualties = 0;
  private double _morale = 100;

  protected ArmyUnit() {} // GSON only

  public ArmyUnit( int id, TroopType troopType, int turnSeen )
  {
    super( id, troopType.getName(), turnSeen, Location.NOWHERE );
    _troopType = troopType;
  }

  public ArmyUnit( TroopType armoredTroop )
  {
    this( UNKNOWN_ID, armoredTroop, 0 );
  }

  public TroopType getTroopType() { return _troopType; }

  public int getCasualties() { return _casualties; }

  public void reduceCasualties( int replacements )
  {
    _casualties = Math.max( _casualties - replacements, 0 );
  }

  public void increaseCasualties( int casualties )
  {
    _casualties = Math.min( _casualties + casualties, _troopType.getSize() );
  }

  public void adjustMorale( int adjustment ) { _morale += adjustment; }

  public int getMorale() { return (int) _morale; }

  public int getBaseCombatStrength()
  {
    return getCombatStrength( Terrain.Undefined );
  }

  public int getCombatStrength( Terrain terrain )
  {
    double terrainImpact = terrain == Terrain.Undefined ? 1.0 : getOwner().getKingdom().getCombatMultiplier( terrain );
    double cumulativeModifiers = terrainImpact * (_morale / 100);
    return (int) ((_troopType.getSize() - _casualties) * _troopType.getStrength() * cumulativeModifiers);
  }

  public void takeDamage( int hits )
  {
    increaseCasualties( (int) (hits * _troopType.getArmor()) );
  }

  public int getSize() { return _troopType.getSize() - _casualties; }

  public boolean equals( Object o ) { return EqualsBuilder.reflectionEquals( this, o ); }

  public int hashCode() { return HashCodeBuilder.reflectionHashCode( this ); }

  public String getShortStatusName() { return _troopType.getName() + " #" + _id; }

  public String getLongStatusName() { return getShortStatusName(); }

  public String getDescription() { return getShortStatusName(); }

  public List<Class> getAvailableOrders() { throw new RuntimeException( "Can't give orders to ArmyUnits." ); }

  public int getMaintenanceCost( double kingdomCostModifier )
  {
    int cost = (int) (_troopType.getMaintenanceCost() * kingdomCostModifier);
    return cost;
  }

  public double getSupportRequired() { return _troopType.getSupportRequired(); }

  public static class MyJsonAdapter extends GameItem.MyJsonAdapter<ArmyUnit>
  {
    private static final String TROOP_TYPE = "troopType";
    private static final String CASUALTIES = "casualties";
    private static final String MORALE = "morale";

    public JsonElement serialize( ArmyUnit unit, Type type, JsonSerializationContext context )
    {
      JsonObject jsonResponse = new JsonObject();
      super.serialize( unit, jsonResponse, type, context );
      jsonResponse.remove( COORDINATE );
      jsonResponse.add( TROOP_TYPE, context.serialize( unit._troopType ) );
      jsonResponse.add( CASUALTIES, context.serialize( unit._casualties ) );
      jsonResponse.add( MORALE, context.serialize( unit._morale ) );
      return jsonResponse;
    }

    public ArmyUnit deserialize( JsonElement jsonOrder, Type type, JsonDeserializationContext context )
      throws JsonParseException
    {
      ArmyUnit army = new ArmyUnit();
      super.doDeserialize( army, jsonOrder, context );
      army._troopType = context.deserialize( jsonOrder.getAsJsonObject().get( TROOP_TYPE ), TroopType.class );
      army._casualties = context.deserialize( jsonOrder.getAsJsonObject().get( CASUALTIES ), Integer.class );
      army._morale = context.deserialize( jsonOrder.getAsJsonObject().get( MORALE ), Double.class );
      return army;
    }
  }

  protected void fixLocationDeserialization( Game game )
  {
    // Army unit location isn't relevant since it's always contained in its army.
    _location = Location.NOWHERE;
  }
}
