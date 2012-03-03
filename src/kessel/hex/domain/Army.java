package kessel.hex.domain;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.reflect.TypeToken;
import kessel.hex.map.Location;
import kessel.hex.map.Terrain;
import kessel.hex.orders.Order;
import kessel.hex.orders.army.AbstractArmyAttack;
import kessel.hex.orders.army.ArmyMove;
import kessel.hex.orders.army.ArmyRecruit;
import kessel.hex.orders.army.ArmySearch;
import kessel.hex.orders.army.ArmyTransfer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** An army, which is composed of units, leaders, etc. */
public class Army extends CombatCapableItem
{
  List<ArmyUnit> _units = new ArrayList<>();

  protected Army() { super(); } // GSON only
  public Army( int id, String name, int turnSeen, Location location )
  {
    super( id, name, turnSeen, location );
  }

  public Army( int id, String name, int turnSeen, Location location, Player owner )
  {
    super( id, name, turnSeen, location, owner );
  }

  public Army( Army toClone )
  {
    this( toClone.getId(), toClone.getName(), toClone.getTurnSeen(), toClone.getLocation(), toClone.getOwner() );
    _units.addAll( toClone.getUnits() );
  }

  public void clearUnits() { _units.clear(); }

  public void removeUnit( ArmyUnit transfer ) { _units.remove( transfer ); }

  public List<ArmyUnit> getUnits() { return _units; }

  public void addUnit( ArmyUnit armyUnit )
  {
    _units.add( armyUnit );
    armyUnit.setOwner( getOwner() );
  }

  /** @return a specific unit in the army or null if it doesn't exist. */
  public ArmyUnit getArmyUnit( int id )
  {
    for ( ArmyUnit unit : _units )
    {
      if ( unit.getId() == id )
      {
        return unit;
      }
    }
    return null;
  }

  public int getBaseCombatStrength()
  {
    int combatStrength = 0;
    for ( ArmyUnit unit : _units )
    {
      combatStrength += unit.getBaseCombatStrength();
    }
    return combatStrength;
  }

  public int getCombatStrength( Terrain terrain )
  {
    int combatStrength = 0;
    for ( ArmyUnit unit : _units )
    {
      combatStrength += unit.getCombatStrength( terrain );
    }

    if ( wasInPreviousCombat() )
    {
      combatStrength = (int) (combatStrength * 0.75);
    }
    return combatStrength;
  }

  /** @return true if the army was in a previous combat this turn. */
  private boolean wasInPreviousCombat()
  {
    for ( Order order : _ordersExecuted )
    {
      if ( order instanceof AbstractArmyAttack )
      {
        return true;
      }
    }
    return false;
  }

  public void retireUnit()
  {
    // Remove the weakest unit and zero out the casualties on the others.
    ArmyUnit weakestUnit = _units.get( 0 );
    for ( ArmyUnit unit : _units )
    {
      if ( unit.getBaseCombatStrength() < weakestUnit.getBaseCombatStrength() )
      {
        weakestUnit = unit;
      }
    }
    _units.remove( weakestUnit );
    for ( ArmyUnit unit : _units )
    {
      unit.reduceCasualties( unit.getTroopType().getSize() );
    }
  }

  public void takeDamage( int hits )
  {
    if ( _units.isEmpty() ) return;

    // Evenly allocate hits across all units.
    int hitsPerUnit = hits / _units.size();
    int totalCasualties = 0;
    ArmyUnit weakestUnit = _units.get( 0 );
    for ( ArmyUnit unit : _units )
    {
      unit.takeDamage( hitsPerUnit );
      totalCasualties += unit.getCasualties();
      if ( unit.getBaseCombatStrength() < weakestUnit.getBaseCombatStrength() )
      {
        weakestUnit = unit;
      }
    }

    // Check for unit consolidation. If the army's total casualties is >= the men left in the weakest unit, then
    // that unit is disbanded and it's remaining men (if any) are allocated equally across the remaining units. Repeat
    // as necessary since consolidation may require removing multiple units.
    while ( totalCasualties >= weakestUnit.getTroopType().getSize() )
    {
      _units.remove( weakestUnit );
      if ( !_units.isEmpty() )
      {
        int menReallocatedPerUnit = weakestUnit.getSize() / _units.size();
        weakestUnit = _units.get( 0 );
        totalCasualties = 0;
        for ( ArmyUnit unit : _units )
        {
          unit.reduceCasualties( menReallocatedPerUnit );
          totalCasualties += unit.getCasualties();
          if ( unit.getBaseCombatStrength() < weakestUnit.getBaseCombatStrength() )
          {
            weakestUnit = unit;
          }
        }
      }
      else
      {
        // Every unit is dead.
        break;
      }
    }
  }

  /** @param adjustment Adjust the morale of every unit in the army by the given amount. */
  public void adjustMorale( int adjustment )
  {
    for ( ArmyUnit unit : _units )
    {
      unit.adjustMorale( adjustment );
    }
  }

  /** @return true if the army has anything in it. */
  public boolean isActive()
  {
    return !_units.isEmpty();
  }

  /** @return all the items in the army. */
  public List<GameItem> getAllItems()
  {
    List<GameItem> items = new ArrayList<>();
    items.addAll( _units );
    return items;
  }

  public int getMaintenanceCost( double kingdomCostModifier )
  {
    int cost = 0;
    for ( ArmyUnit unit : _units )
    {
      cost += unit.getMaintenanceCost( kingdomCostModifier );
    }
    return cost;
  }

  public double getSupportRequired()
  {
    // The army itself requires a basic level of support just to exist.
    double level = 1.0;

    for ( ArmyUnit unit : _units )
    {
      level += unit.getSupportRequired();
    }
    return level;
  }

  public void setOwner( Player owner )
  {
    super.setOwner( owner );
    for ( ArmyUnit unit : _units )
    {
      unit.setOwner( owner );
    }
  }

  @SuppressWarnings({ "RawUseOfParameterizedType", "ChainOfInstanceofChecks" })
  public List<Class> getAvailableOrders()
  {
    boolean moved = false;
    for ( Order nextTurnOrder : getNextTurnOrders() )
    {
      if ( nextTurnOrder instanceof ArmyMove ) moved = true;
    }
    List<Class> orders = new ArrayList<>();
    orders.add( ArmyRecruit.class );
    if ( !moved )
    {
      orders.addAll( Arrays.asList( ArmyMove.class, ArmySearch.class ) );
    }
    if ( isActive() )
    {
      orders.add( AbstractArmyAttack.class );

      // Are there targets for a transfer order?
      for ( Army army : getOwner().getArmies() )
      {
        if ( !army.equals( this ) &&
             (!army.isActive() || army.getLocation().getCoord().equals( getLocation().getCoord() )) )
        {
          orders.add( ArmyTransfer.class );
          break;
        }
      }
    }
    return orders;
  }

  public String getShortStatusName()
  {
    return _name + "(" + getUnits().size() + ")";
  }

  public String getLongStatusName()
  {
    return getShortStatusName();
  }

  /** @return the average of the morale of all units. */
  private int getMorale()
  {
    if ( !_units.isEmpty() )
    {
      int morale = 0;
      for ( ArmyUnit unit : _units )
      {
        morale += unit.getMorale();
      }
      return morale / _units.size();
    }
    else
    {
      return 100;
    }
  }

  public String getDescription()
  {
    return getShortStatusName() + " at " + getLocation().getCoord() +
           " Strength:" + getCombatStrength( getLocation().getTerrain() ) +
           " Morale: " + getMorale();
  }

  public static class MyJsonAdapter extends GameItem.MyJsonAdapter<Army>
  {
    private static final String UNITS = "units";

    public JsonElement serialize( Army army, Type type, JsonSerializationContext context )
    {
      JsonObject jsonResponse = new JsonObject();
      super.serialize( army, jsonResponse, type, context );
      jsonResponse.add( UNITS, context.serialize( army._units ) );
      return jsonResponse;
    }

    public Army deserialize( JsonElement jsonOrder, Type type, JsonDeserializationContext context )
      throws JsonParseException
    {
      Army army = new Army();
      super.doDeserialize( army, jsonOrder, context );
      army._units = context.deserialize( jsonOrder.getAsJsonObject().get( UNITS ), new TypeToken<List<ArmyUnit>>(){}.getType() );
      return army;
    }
  }

  public void fixDeserializationReferences( Game game )
  {
    super.fixDeserializationReferences( game );
    for ( ArmyUnit unit : _units )
    {
      unit.setLocation( this.getLocation() );
      unit.fixDeserializationReferences( game );
    }
  }
}

