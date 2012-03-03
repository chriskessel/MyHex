package kessel.hex.domain;

import kessel.hex.map.Terrain;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ArmyUnitTest extends GameItemTest<ArmyUnit>
{
  private static final Logger LOG = Logger.getLogger( ArmyUnitTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  public void doEqualsTest( ArmyUnit a, ArmyUnit b ) throws Exception
  {
    assertEquals( a.getMorale(), b.getMorale() );
    assertEquals( a.getCasualties(), b.getCasualties() );
    assertEquals( a.getTroopType(), b.getTroopType() );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player player = game.getPlayers().get( 0 );

    ArmyUnit oldUnit = new ArmyUnit( new TroopType( "foo", 1000, 1.0, 0.5 ) );
    oldUnit.setOwner( player );

    String json = Game.GSON.toJson( oldUnit );
    LOG.debug( json );

    ArmyUnit newUnit = Game.GSON.fromJson( json, ArmyUnit.class );
    doEqualsTest( oldUnit, newUnit );
    newUnit.setOwner( player );
    assertEquals( 1000, newUnit.getCombatStrength( Terrain.Plain ) );
    assertEquals( 0, newUnit.getCasualties() );
    newUnit.adjustMorale( 10 );
    assertEquals( 1100, newUnit.getCombatStrength( Terrain.Plain ) );
    newUnit.adjustMorale( -10 );
    assertEquals( 1000, newUnit.getCombatStrength( Terrain.Plain ) );
    newUnit.takeDamage( 1000 );
    assertEquals( 500, newUnit.getCasualties() );
    assertEquals( 500, newUnit.getCombatStrength( Terrain.Plain ) );
    newUnit.takeDamage( 2000 );
    assertEquals( 1000, newUnit.getCasualties() );
    assertEquals( 0, newUnit.getCombatStrength( Terrain.Plain ) );
  }
}
