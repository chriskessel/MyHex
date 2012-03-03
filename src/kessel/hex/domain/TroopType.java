package kessel.hex.domain;

import net.jcip.annotations.Immutable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/** Defines a specific type of troop. */
@Immutable
public class TroopType
{
  // The troops levied by recruit orders.
  public static final TroopType LEVY = new TroopType( "Levy", 1000, 0.9, 0.55 );

  // Troops levied by recruit orders on cities where the player dominates the region.
  public static final TroopType REGIONAL = new TroopType( "Regional", 1000, 1.1, 0.45 );

  // What PopCenter's use on defense.
  public static final TroopType MILITIA = new TroopType( "Militia", 1000, 0.5, 0.5 );
  public static final TroopType CITY_WATCH = new TroopType( "Soldiers", 1000, 0.6, 0.5 );

  // A generic troop type to represent the fact a troop type exists, but no details.
  public static final TroopType UNKNOWN_TROOP = new TroopType( "Unknown", 0, 1.0, 0.5 );

  // Various magical troops. Also, "fake" troops for purposes of calculating spell damage.
  public static final TroopType UNDEAD = new TroopType( "Skeletons", 1000, 0.9, 0.55, 0.0, 0.0 );
  public static final TroopType WEREBEAST = new TroopType( "Were Beasts", 1000, 1.1, 0.45, 0.0, 0.0 );
  public static final TroopType OGRE = new TroopType( "Ogre", 1000, 1.7, 0.29, 0.0, 0.0 );
  public static final TroopType DRAGON = new TroopType( "Dragons", 1000, 3.3, 0.15, 0.0, 0.0 );
  public static final TroopType FIREBALL = new TroopType( "Fireball", 250, 1.0, 1.0 );
  public static final TroopType WIZARD_INNATE = new TroopType( "Wizard Innate", 75, 1.0, 1.0 );

  private static final double DEFAULT_MAINTENANCE = 0.75;
  private static final double DEFAULT_SUPPORT = 1.0;

  // How many men are in the unit.
  private final int _size;

  // How much damage each unit member does in combat.
  private final double _strength;

  // How much damage gets through to actually cause a wound. Lower is better: 0.1 means only 10% of hits cause wounds.
  private final double _armor;

  // The maintenance cost per turn for a unit of this troop type.
  private final double _maintenanceCost;

  // How many points of support are required for a unit of this troop type.
  private final double _supportRequired;

  private final String _name;

  TroopType() { this( "foo", 1, 1.0, 1.0, DEFAULT_MAINTENANCE, DEFAULT_SUPPORT ); } // only for GSON

  public TroopType( String name, int size, double strength, double armor )
  {
    this( name, size, strength, armor, DEFAULT_MAINTENANCE, DEFAULT_SUPPORT );
  }

  public TroopType( String name, int size, double strength, double armor, double maintenanceCost, double supportRequired )
  {
    _name = name;
    _size = size;
    _strength = strength;
    _armor = armor;
    _maintenanceCost = maintenanceCost;
    _supportRequired = supportRequired;
  }

  public int getSize() { return _size; }

  public String getName() { return _name; }

  public double getStrength() { return _strength; }

  public double getArmor() { return _armor; }

  public int getMaintenanceCost() { return (int) (_maintenanceCost * Game.BASE_HAMLET_PRODUCTION * Game.GOLD_GRANULARITY); }

  public double getSupportRequired() { return _supportRequired; }

  public boolean equals( Object o )
  {
    return EqualsBuilder.reflectionEquals( this, o );
  }

  public int hashCode()
  {
    return HashCodeBuilder.reflectionHashCode( this );
  }
}
