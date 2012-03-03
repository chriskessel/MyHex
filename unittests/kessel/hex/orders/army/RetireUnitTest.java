package kessel.hex.orders.army;

import kessel.hex.domain.Army;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.orders.AbstractOrderTest;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

/** Test army retiring a unit. */
public class RetireUnitTest extends AbstractOrderTest<RetireUnit>
{
  private static final Logger LOG = Logger.getLogger( RetireUnitTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army army = playerA.getArmies().get( 0 );
    RetireUnit oldOrder = new RetireUnit( army );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    RetireUnit newOrder = Game.GSON.fromJson( jsonOrder, RetireUnit.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testRetire()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army army = playerA.getArmies().get( 0 );
    army.setLocation( game.getMap().getLocation( 2,2 ) );
    RetireUnit order = new RetireUnit( army );
    int before = army.getUnits().size();
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( before - 1, army.getUnits().size() );

    assertTrue( army.isActive() );
    assertFalse( army.getLocation().equals( playerA.getCapitol().getLocation() ) );
    while ( !army.getUnits().isEmpty() )
    {
      order.execute( game );
    }
    assertFalse( army.isActive() );
    assertEquals( army.getLocation(), playerA.getCapitol().getLocation() );
  }
}
