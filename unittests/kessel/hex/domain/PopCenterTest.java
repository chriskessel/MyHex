package kessel.hex.domain;

import kessel.hex.map.ControlLevel;
import kessel.hex.map.Location;
import kessel.hex.map.Region;
import kessel.hex.map.Terrain;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PopCenterTest extends GameItemTest<PopCenter>
{
  private static final Logger LOG = Logger.getLogger( PopCenterTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Location loc = new Location( 1, 1 );
    loc.setTerrain( Terrain.Forest );
    PopCenter popA = new PopCenter( GameItem.UNKNOWN_ID, "Bob", 4, loc, PopCenter.PopType.Hamlet );
    popA.getLastTurnStatus().add( "Foo" );

    String json = Game.GSON.toJson( popA );
    LOG.debug( json );

    PopCenter popB = Game.GSON.fromJson( json, PopCenter.class );
    super.doEqualsTest( popA, popB );
    assertEquals( popA.getLevel(), popB.getLevel() );
    assertEquals( popA.getType(), popB.getType() );
  }

  @Test
  public void testDominationAndCapitolCombatBonus() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player player = game.getPlayers().get( 0 );
    Location loc = new Location( 1, 1 );
    loc.setTerrain( Terrain.Forest );
    Player playerWithControl = new Player( "foo" )
    {
      public ControlLevel getControlLevel( Region region ) { return ControlLevel.Control; }
    };
    Player playerWithDomination = new Player( "bar" )
    {
      public ControlLevel getControlLevel( Region region ) { return ControlLevel.Domination; }
    };

    // Verify control gives no bonus.
    PopCenter pop = new PopCenter( GameItem.UNKNOWN_ID, "Bob", 4, loc, PopCenter.PopType.Town );
    pop.setLevel( 2 );
    pop.setOwner( playerWithControl );
    assertEquals( 1000, pop.getCombatStrength( Terrain.Undefined ) );

    // Verify domination does give a bonus.
    pop.setOwner( playerWithDomination );
    assertEquals( 1500, pop.getCombatStrength( Terrain.Undefined ) );
    player.setCapitol( pop );

    // Verify being a capitol gives a bonus.
    playerWithDomination.setCapitol( pop );
    assertEquals( 2250, pop.getCombatStrength( Terrain.Undefined ) );
    player.setCapitol( pop );
  }
}
