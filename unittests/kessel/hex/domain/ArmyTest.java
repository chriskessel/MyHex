package kessel.hex.domain;

import kessel.hex.map.Location;
import kessel.hex.map.Terrain;
import kessel.hex.orders.army.AbstractArmyAttack;
import kessel.hex.orders.army.ArmyAttackArmy;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ArmyTest extends GameItemTest<Army>
{
  private static final Logger LOG = Logger.getLogger( ArmyTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  public void doEqualsTest( Army a, Army b ) throws Exception
  {
    assertEquals( a._units.size(), b._units.size() );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player player = game.getPlayers().get( 0 );

    TroopType troopType = new TroopType( "foo", 1000, 1.0, 0.5 );
    Army oldArmy = new Army( 1, "foo", 0, Location.NOWHERE );
    oldArmy.setOwner( player );
    for ( int i = 0; i < 3; i++ )
    {
      oldArmy.addUnit( new ArmyUnit( troopType ) );
    }

    String json = Game.GSON.toJson( oldArmy );
    LOG.debug( json );

    Army newArmy = Game.GSON.fromJson( json, Army.class );
    newArmy.setOwner( player );
    assertEquals( oldArmy, newArmy );
    assertEquals( 3000, newArmy.getCombatStrength( Terrain.Plain ) );
    doEqualsTest( oldArmy, newArmy );
  }

  @Test
  public void testDamageAllocation() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player player = game.getPlayers().get( 0 );

    TroopType normalTroop = new TroopType( "foo", 1000, 1.0, 0.5 );
    TroopType armoredTroop = new TroopType( "foo", 1000, 1.0, 0.2 );
    Army army = new Army( 1, "foo", 0, Location.NOWHERE );
    army.setOwner( player );
    army.addUnit( new ArmyUnit( armoredTroop ) );
    army.addUnit( new ArmyUnit( normalTroop ) );
    army.addUnit( new ArmyUnit( normalTroop ) );

    // Each unit should take 200 hits, the result of which varies by troop type.
    army.takeDamage( 600 );
    assertEquals( 40, army.getUnits().get( 0 ).getCasualties() );
    assertEquals( 100, army.getUnits().get( 1 ).getCasualties() );
    assertEquals( 100, army.getUnits().get( 2 ).getCasualties() );
  }

  @Test
  public void testPriorCombatEffect() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player player = game.getPlayers().get( 0 );

    TroopType normalTroop = new TroopType( "foo", 1000, 1.0, 0.5 );
    TroopType armoredTroop = new TroopType( "foo", 1000, 1.0, 0.2 );
    Army army = new Army( 1, "foo", 0, Location.NOWHERE );
    army.setOwner( player );
    army.addUnit( new ArmyUnit( armoredTroop ) );
    army.addUnit( new ArmyUnit( normalTroop ) );
    army.addUnit( new ArmyUnit( normalTroop ) );

    // Each unit should take 200 hits, the result of which varies by troop type.
    AbstractArmyAttack attackOrder = new ArmyAttackArmy( army, game.getPlayers().get( 1 ).getArmies().get( 0 ) );
    assertEquals( 3000, army.getCombatStrength( Terrain.Plain ) );
    army.addOrderExecuted( attackOrder );
    assertEquals( 2250, army.getCombatStrength( Terrain.Plain ) );
  }

  @Test
  public void testConsolidationAfterDamage() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player player = game.getPlayers().get( 0 );

    TroopType normalTroop = new TroopType( "normal", 1000, 1.0, 0.5 );
    TroopType armoredTroop = new TroopType( "armor", 1000, 1.0, 0.2 );
    Army army = new Army( 1, "foo", 0, Location.NOWHERE );
    army.setOwner( player );
    army.addUnit( new ArmyUnit( armoredTroop ) );
    army.addUnit( new ArmyUnit( armoredTroop ) );
    army.addUnit( new ArmyUnit( normalTroop ) );
    army.addUnit( new ArmyUnit( normalTroop ) );

    // Take damage, but not enough to cause a consolidation.
    army.takeDamage( 2000 );
    assertEquals( 4, army.getUnits().size() );
    assertEquals( 100, army.getUnits().get( 0 ).getCasualties() );
    assertEquals( 100, army.getUnits().get( 1 ).getCasualties() );
    assertEquals( 250, army.getUnits().get( 2 ).getCasualties() );
    assertEquals( 250, army.getUnits().get( 3 ).getCasualties() );

    // Take more damage and now the weakest unit should be gone.
    army.takeDamage( 1000 );
    assertEquals( 3, army.getUnits().size() );
    assertEquals( armoredTroop, army.getUnits().get( 0 ).getTroopType() );
    assertEquals( armoredTroop, army.getUnits().get( 1 ).getTroopType() );
    assertEquals( normalTroop, army.getUnits().get( 2 ).getTroopType() );
    assertEquals( 0, army.getUnits().get( 0 ).getCasualties() );
    assertEquals( 0, army.getUnits().get( 1 ).getCasualties() );
    assertEquals( 167, army.getUnits().get( 2 ).getCasualties() );

    // Take more damage and now two more units should be gone.
    army.takeDamage( 9000 );
    assertEquals( 1, army.getUnits().size() );
    assertEquals( armoredTroop, army.getUnits().get( 0 ).getTroopType() );
    assertEquals( 200, army.getUnits().get( 0 ).getCasualties() );
  }
}
