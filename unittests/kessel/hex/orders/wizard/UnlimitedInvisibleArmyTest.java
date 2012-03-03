package kessel.hex.orders.wizard;

import kessel.hex.domain.Army;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.Wizard;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

public class UnlimitedInvisibleArmyTest extends AbstractInvisibleArmyTest<UnlimitedInvisibleArmy>
{
  private static final Logger LOG = Logger.getLogger( UnlimitedInvisibleArmyTest.class );

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
    Army army = playerA.getActiveArmies().get( 0 );
    wizard.setBase( army );
    UnlimitedInvisibleArmy oldOrder = new UnlimitedInvisibleArmy( wizard );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    UnlimitedInvisibleArmy newOrder = Game.GSON.fromJson( jsonOrder, UnlimitedInvisibleArmy.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testInvisible()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizard = playerA.getWizards().get( 0 );
    Army army = playerA.getActiveArmies().get( 0 );
    wizard.setBase( army );
    wizard.setLevel( 7 );
    UnlimitedInvisibleArmy order = new UnlimitedInvisibleArmy( wizard );

    assertFalse( army.isInvisible() );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertTrue( army.isInvisible() );
  }
}
