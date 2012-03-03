package kessel.hex.domain;

import kessel.hex.map.Terrain;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/** Defines the attributes of a kingdom. */
@SuppressWarnings({ "MapReplaceableByEnumMap" })
@Immutable
public class Kingdom
{
  private transient List<String> _figureNames = new ArrayList<>();

  private String _namesFile;
  private String _name;
  private String _armyName;
  private final Map<Terrain, Integer> _movementCosts = new HashMap<>();
  private final Map<Terrain, Double> _combatMultipliers = new HashMap<>();

  // A list of the power levels of each starting figure.
  private final List<Integer> _startingWizards = new ArrayList<>();
  private int _wizardLevelCap = 10;
  private double _wizardCostModifier = 1.0;
  private final List<Integer> _startingDiplomats = new ArrayList<>();
  private double _diplomatCostModifier = 1.0;
  private final List<Integer> _startingAgents = new ArrayList<>();
  private int _agentLevelCap = 20;
  private double _agentCostModifier = 1.0;

  // The number of units in each army.
  private final List<Integer> _startingArmies = new ArrayList<>();

  // How much this kingdom pays it's soldiers. This is regardless of troop type.
  private double _unitCost;

  // How many turns it takes for a new kingdom unit to be available.
  private double _reinforcementRate = 0;

  private int _startingGold = 0;
  private int _startingPower = 0;
  private int _startingEmbassies = 0;
  private TroopType _troopType;

  public Kingdom() {} // for GSON only
  public Kingdom( String name )
  {
    _name = name;
  }

  public String getName() { return _name; }

  public Map<Terrain, Integer> getMovementCosts() { return _movementCosts; }

  public void setMovementCosts( Map<Terrain, Integer> movementCosts )
  {
    _movementCosts.clear();
    _movementCosts.putAll( movementCosts );
  }

  public Map<Terrain, Double> getCombatMultipliers() { return _combatMultipliers; }

  public void setCombatMultipliers( Map<Terrain, Double> multipliers )
  {
    _combatMultipliers.clear();
    _combatMultipliers.putAll( multipliers );
  }

  public List<Integer> getStartingWizards() { return _startingWizards; }

  public void setStartingWizards( List<Integer> wizards )
  {
    _startingWizards.clear();
    _startingWizards.addAll( wizards );
  }

  public List<Integer> getStartingDiplomats() { return _startingDiplomats; }

  public void setStartingDiplomats( List<Integer> diplomats )
  {
    _startingDiplomats.clear();
    _startingDiplomats.addAll( diplomats );
  }

  public List<Integer> getStartingAgents() { return _startingAgents; }

  public void setStartingAgents( List<Integer> agents )
  {
    _startingAgents.clear();
    _startingAgents.addAll( agents );
  }

  public int getStartingEmbassies() { return _startingEmbassies; }

  public void setStartingEmbassies( int startingEmbassies ) { _startingEmbassies = startingEmbassies; }

  public int getStartingGold() { return _startingGold; }

  public void setStartingGold( int gold ) { _startingGold = gold; }

  public int getStartingPower() { return _startingPower; }

  public void setStartingPower( int power ) { _startingPower = power; }

  public List<Integer> getStartingArmies() { return _startingArmies; }

  public void setStartingArmies( List<Integer> armies )
  {
    _startingArmies.clear();
    _startingArmies.addAll( armies );
  }

  public TroopType getTroopType() { return _troopType; }

  public void setTroopType( TroopType troopType ) { _troopType = troopType; }

  public double getUnitCost() { return _unitCost; }

  public void setUnitCost( double unitCost ) { _unitCost = unitCost; }

  /** @return the terrain in order of combat ability. */
  public List<Terrain> getTerrainPreferences()
  {
    SortedMap<Double, List<Terrain>> m = new TreeMap<>( Collections.<Object>reverseOrder() );
    for ( Terrain terrain : Terrain.values() )
    {
      Double combatMultiplier = _combatMultipliers.get( terrain );
      List<Terrain> terrainsForThisMultiplier = m.get( combatMultiplier );
      if ( terrainsForThisMultiplier == null )
      {
        terrainsForThisMultiplier = new ArrayList<>();
        m.put( combatMultiplier, terrainsForThisMultiplier );
      }
      terrainsForThisMultiplier.add( terrain );
    }
    List<Terrain> toReturn = new ArrayList<>();
    for ( List<Terrain> terrains : m.values() )
    {
      toReturn.addAll( terrains );
    }
    return toReturn;
  }

  public int getMovementCost( Terrain terrain ) { return _movementCosts.get( terrain ); }

  public double getCombatMultiplier( Terrain terrain ) { return _combatMultipliers.get( terrain ); }

  public int getWizardLevelCap() { return _wizardLevelCap; }

  public void setWizardLevelCap( int wizardLevelCap ) { _wizardLevelCap = wizardLevelCap; }

  public int getAgentLevelCap() { return _agentLevelCap; }

  public void setAgentLevelCap( int agentLevelCap ) { _agentLevelCap = agentLevelCap; }

  public double getWizardCostModifier() { return _wizardCostModifier; }

  public void setWizardCostModifier( double wizardCostModifier ) { _wizardCostModifier = wizardCostModifier; }

  public double getDiplomatCostModifier() { return _diplomatCostModifier; }

  public void setDiplomatCostModifier( double diplomatCostModifier ) { _diplomatCostModifier = diplomatCostModifier; }

  public double getAgentCostModifier() { return _agentCostModifier; }

  public void setAgentCostModifier( double agentCostModifier ) { _agentCostModifier = agentCostModifier; }

  public double getReinforcementRate() { return _reinforcementRate; }

  public void setReinforcementRate( double reinforcementRate ) { _reinforcementRate = reinforcementRate; }

  public String getArmyName() { return _armyName; }

  public void setArmyName( String armyName ) { _armyName = armyName; }

  public String getNamesFile() { return _namesFile; }

  /** Copy the definition of the kingdoms abilities from another kingdom. */
  public void copyFrom( Kingdom kingdom )
  {
    _movementCosts.clear();
    _movementCosts.putAll( kingdom._movementCosts );

    _combatMultipliers.clear();
    _combatMultipliers.putAll( kingdom._combatMultipliers );

    _startingAgents.clear();
    _startingAgents.addAll( kingdom._startingAgents );
    _startingDiplomats.clear();
    _startingDiplomats.addAll( kingdom._startingDiplomats );
    _startingWizards.clear();
    _startingWizards.addAll( kingdom._startingWizards );
    _startingArmies.clear();
    _startingArmies.addAll( kingdom._startingArmies );
    _startingGold = kingdom._startingGold;
    _startingPower = kingdom._startingPower;
    _troopType = kingdom._troopType;
  }

  public ArmyUnit createArmyUnit( int id ) { return new ArmyUnit( id, _troopType, 0 ); }

  /** Return a kingdom-flavored name for a figure. Remember what names have been used. */
  public String getFigureName( int nameIndex )
  {
    String name = _figureNames.get( nameIndex % _figureNames.size() );
    return name;
  }

  public void setFigureNames( List<String> names )
  {
    _figureNames = names;
  }

  public boolean equals( Object o ) { return EqualsBuilder.reflectionEquals( this, o ); }

  public int hashCode() { return HashCodeBuilder.reflectionHashCode( this ); }
}
