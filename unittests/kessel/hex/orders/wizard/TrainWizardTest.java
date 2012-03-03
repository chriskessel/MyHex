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

/** Test moving agents. */
public class TrainWizardTest extends AbstractOrderTest<TrainWizard>
{
  private static final Logger LOG = Logger.getLogger( TrainWizardTest.class );

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
    TrainWizard oldOrder = new TrainWizard( wizard );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    TrainWizard newOrder = Game.GSON.fromJson( jsonOrder, TrainWizard.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testNotEnoughMoney()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizard = playerA.getWizards().get( 0 );
    TrainWizard order = new TrainWizard( wizard );
    playerA.setGold( 0 );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void trainedTwice()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizard = playerA.getWizards().get( 0 );
    playerA.setGold( 20000 );
    TrainWizard orderOne = new TrainWizard( wizard );
    orderOne.execute( game );
    assertTrue( orderOne.wasExecuted() );
    TrainWizard orderTwo = new TrainWizard( wizard );
    orderTwo.execute( game );
    assertFalse( orderTwo.wasExecuted() );
  }

  @Test
  public void testTrained()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    playerA.setGold( 10000 );
    Wizard wizard = playerA.getWizards().get( 0 );
    int oldLevel = wizard.getLevel();
    TrainWizard order = new TrainWizard( wizard );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( oldLevel + 1, wizard.getLevel() );
  }
}
