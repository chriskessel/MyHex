package kessel.hex.orders.wizard;

import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.AbstractOrderTest;
import kessel.hex.util.Tuple;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

public class MagicDomeTest extends AbstractOrderTest<MagicDome>
{
  private static final Logger LOG = Logger.getLogger( MagicDomeTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  public void doEqualsTest( MagicDome a, MagicDome b ) throws Exception
  {
    super.doEqualsTest( a, b );
    assertEquals( a.getTarget().getId(), b._jsonTargetId.intValue() );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    PopCenter pop = game.getPopCenter( new Tuple( 2, 2 ) );
    MagicDome oldOrder = new MagicDome( wizardForA, pop );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    MagicDome newOrder = Game.GSON.fromJson( jsonOrder, MagicDome.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testSuccess()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    PopCenter pop = game.getPopCenter( new Tuple( 2, 2 ) );
    wizardForA.setLevel( 6 );

    MagicDome order = new MagicDome( wizardForA, pop );
    order.execute( game );

    assertTrue( order.wasExecuted() );
    assertEquals( 1, game.getCurrentTurn().getOrdersOfType( MagicDome.class ).size() );
  }
}
