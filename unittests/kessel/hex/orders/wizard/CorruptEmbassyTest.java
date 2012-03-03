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

public class CorruptEmbassyTest extends AbstractOrderTest<CorruptEmbassy>
{
  private static final Logger LOG = Logger.getLogger( CorruptEmbassyTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  public void doEqualsTest( CorruptEmbassy a, CorruptEmbassy b ) throws Exception
  {
    super.doEqualsTest( a, b );
    assertEquals( a._playerName, b._playerName );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    CorruptEmbassy oldOrder = new CorruptEmbassy( wizardForA, playerB.getName() );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    CorruptEmbassy newOrder = Game.GSON.fromJson( jsonOrder, CorruptEmbassy.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testTargetHitTwice()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Wizard wizardOneForA = playerA.getWizards().get( 0 );
    Wizard wizardTwoForA = playerA.getWizards().get( 1 );
    wizardTwoForA.setBase( wizardOneForA.getBase() );
    playerB.improveEmbassy( wizardOneForA.getLocation().getRegion() );

    wizardOneForA.setLevel( 5 );
    CorruptEmbassy orderOne = new CorruptEmbassy( wizardOneForA, playerB.getName() );
    int oldEmbassy = playerB.getEmbassyLevel( wizardOneForA.getLocation().getRegion() );
    orderOne.execute( game );
    assertTrue( orderOne.wasExecuted() );
    assertEquals( oldEmbassy - 1, playerA.getEmbassyLevel( wizardOneForA.getLocation().getRegion() ) );

    wizardTwoForA.setLevel( 5 );
    CorruptEmbassy orderTwo = new CorruptEmbassy( wizardTwoForA, playerB.getName() );
    orderTwo.execute( game );
    assertFalse( orderTwo.wasExecuted() );
  }

  @Test
  public void testRegionShielded()
  {
    // First execute the shield.
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    Wizard wizardForB = playerB.getWizards().get( 0 );
    wizardForB.setLevel( 3 );
    ShieldRegion shieldOrder = new ShieldRegion( wizardForB );
    shieldOrder.execute( game );
    assertTrue( shieldOrder.wasExecuted() );

    // Now try the corruption.
    wizardForB.setBase( wizardForA.getBase() );
    playerB.improveEmbassy( wizardForA.getLocation().getRegion() );

    wizardForA.setLevel( 5 );
    CorruptEmbassy corruptOrder = new CorruptEmbassy( wizardForA, playerB.getName() );
    corruptOrder.execute( game );
    assertFalse( corruptOrder.wasExecuted() );
  }

  @Test
  public void testSuccess()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    playerB.improveEmbassy( wizardForA.getLocation().getRegion() );
    wizardForA.setLevel( 5 );

    CorruptEmbassy order = new CorruptEmbassy( wizardForA, playerB.getName() );
    int oldEmbassy = playerB.getEmbassyLevel( wizardForA.getLocation().getRegion() );
    order.execute( game );

    assertTrue( order.wasExecuted() );
    assertEquals( oldEmbassy - 1, playerA.getEmbassyLevel( wizardForA.getLocation().getRegion() ) );
  }
}
