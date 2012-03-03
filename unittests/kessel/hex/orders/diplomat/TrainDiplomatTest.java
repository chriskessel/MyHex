package kessel.hex.orders.diplomat;

import kessel.hex.domain.Army;
import kessel.hex.domain.Diplomat;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.orders.AbstractOrderTest;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

/** Test traiing diplomats. */
public class TrainDiplomatTest extends AbstractOrderTest<TrainDiplomat>
{
  private static final Logger LOG = Logger.getLogger( TrainDiplomatTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Diplomat diplomat = playerA.getDiplomats().get( 0 );
    TrainDiplomat oldOrder = new TrainDiplomat( diplomat );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    TrainDiplomat newOrder = Game.GSON.fromJson( jsonOrder, TrainDiplomat.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testNotEnoughMoney()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Diplomat diplomat = playerA.getDiplomats().get( 0 );
    TrainDiplomat order = new TrainDiplomat( diplomat );
    playerA.setGold( 0 );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testNotAtOwnedPop()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    playerA.setGold( 10000 );
    Diplomat diplomat = playerA.getDiplomats().get( 0 );
    Army armyA = playerA.getArmies().get( 0 );
    diplomat.setBase( armyA );
    armyA.setLocation( game.getMap().getLocation( 2, 2 ) );
    TrainDiplomat order = new TrainDiplomat( diplomat );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testTrained()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    playerA.setGold( 10000 );
    Diplomat diplomat = playerA.getDiplomats().get( playerA.getDiplomats().size() - 1 );
    int oldLevel = diplomat.getLevel();
    TrainDiplomat order = new TrainDiplomat( diplomat );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( oldLevel + 1, diplomat.getLevel() );
  }
}
