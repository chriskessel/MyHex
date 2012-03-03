package kessel.hex.orders.wizard;

import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.AbstractOrderTest;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

public class AlchemyTest extends AbstractOrderTest<Alchemy>
{
  private static final Logger LOG = Logger.getLogger( AlchemyTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizard = playerA.getWizards().get( 0 );
    Alchemy oldOrder = new Alchemy( wizard );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    Alchemy newOrder = Game.GSON.fromJson( jsonOrder, Alchemy.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testWizardIsSufficientLevel()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizard = playerA.getWizards().get( 0 );
    wizard.setLevel( 0 );
    Alchemy order = new Alchemy( wizard );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testAlchemy()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizard = playerA.getWizards().get( 0 );
    Alchemy order = new Alchemy( wizard );

    assertEquals( 5400, playerA.getGold() );
    assertEquals( 2, wizard.getLevel() );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( 5700, playerA.getGold() );
  }
}
