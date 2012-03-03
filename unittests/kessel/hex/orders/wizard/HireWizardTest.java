package kessel.hex.orders.wizard;

import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.orders.AbstractHireFigureTest;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

/** Test wizard purchasing. */
@SuppressWarnings({ "AssignmentToStaticFieldFromInstanceMethod" })
public class HireWizardTest extends AbstractHireFigureTest<HireWizard>
{
  private static final Logger LOG = Logger.getLogger( HireWizardTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    PopCenter popForA = playerA.getCapitol();

    HireWizard oldOrder = new HireWizard( playerA.getKing(), popForA );
    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    HireWizard newOrder = Game.GSON.fromJson( jsonOrder, HireWizard.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testBadPop()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    PopCenter popForB = playerB.getCapitol();

    HireWizard order = new HireWizard( playerA.getKing(), popForB );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testOutOfMoney()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    PopCenter popForA = playerA.getCapitol();
    playerA.setGold( 1 );

    HireWizard order = new HireWizard( playerA.getKing(), popForA );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testTooManyWizards()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    PopCenter popForA = playerA.getCapitol();
    playerA.setGold( Game.GOLD_GRANULARITY * 1000 );

    HireWizard order = new HireWizard( playerA.getKing(), popForA );
    assertEquals( 6, playerA.getWizards().size() );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testSuccess()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    PopCenter popForA = playerA.getCapitol();
    playerA.setGold( Game.GOLD_GRANULARITY * 1000 );
    playerA.getWizards().remove( 0 );

    HireWizard order = new HireWizard( playerA.getKing(), popForA );
    assertEquals( 5, playerA.getWizards().size() );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( 6, playerA.getWizards().size() );
  }
}
