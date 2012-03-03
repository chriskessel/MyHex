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

public class BadOmenTest extends AbstractOrderTest<BadOmen>
{
  private static final Logger LOG = Logger.getLogger( BadOmenTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  public void doEqualsTest( BadOmen a, BadOmen b ) throws Exception
  {
    super.doEqualsTest( a, b );
    assertEquals( a.getTarget().getId(), b._jsonTargetId.intValue() );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    BadOmen oldOrder = new BadOmen( wizardForA, playerB.getKing() );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    BadOmen newOrder = Game.GSON.fromJson( jsonOrder, BadOmen.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testTargetShielded()
  {
    // Cast the shield.
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    Wizard wizardForB = playerB.getWizards().get( 0 );
    wizardForB.setLevel( 2 );

    ShieldFigure shieldOrder = new ShieldFigure( wizardForB, playerB.getKing() );
    shieldOrder.execute( game );
    assertTrue( shieldOrder.wasExecuted() );
    assertEquals( 1, game.getCurrentTurn().getOrdersOfType( ShieldFigure.class ).size() );

    // Cast the charm, it should fail due to the protection.
    wizardForA.setLevel( 5 );
    BadOmen charmOrder = new BadOmen( wizardForA, playerB.getKing() );
    charmOrder.execute( game );
    assertFalse( charmOrder.wasExecuted() );
  }

  @Test
  public void testTargetHitTwice()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Wizard wizardOneForA = playerA.getWizards().get( 0 );
    Wizard wizardTwoForA = playerA.getWizards().get( 1 );

    wizardOneForA.setLevel( 5 );
    BadOmen orderOne = new BadOmen( wizardOneForA, playerB.getKing() );
    int power = playerB.getPower();
    orderOne.execute( game );
    assertTrue( orderOne.wasExecuted() );

    wizardTwoForA.setLevel( 5 );
    BadOmen orderTwo = new BadOmen( wizardTwoForA, playerB.getKing() );
    orderTwo.execute( game );
    assertFalse( orderTwo.wasExecuted() );

    assertEquals( power - 1, playerB.getPower() );
  }

  @Test
  public void testSuccess()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    wizardForA.setLevel( 5 );

    BadOmen order = new BadOmen( wizardForA, playerB.getKing() );
    int power = playerB.getPower();
    order.execute( game );

    assertTrue( order.wasExecuted() );
    assertEquals( power - 1, playerB.getPower() );
  }
}
