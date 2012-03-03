package kessel.hex.orders.army;

import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.orders.AbstractOrderTest;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

/** Test army recruiting. */
public class CreateArmyTest extends AbstractOrderTest<CreateArmy>
{
  private static final Logger LOG = Logger.getLogger( CreateArmyTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    CreateArmy oldOrder = new CreateArmy( playerA.getKing() );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    CreateArmy newOrder = Game.GSON.fromJson( jsonOrder, CreateArmy.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testOutOfMoney()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    playerA.setGold( 0 );
    CreateArmy order = new CreateArmy( playerA.getKing() );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testOutOfSupport()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    playerA.setGold( 10000 );
    CreateArmy order = new CreateArmy( playerA.getKing() );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testSuccess()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    playerA.setGold( 10000 );
    playerA.add( new PopCenter( game.generateUniqueId(), "foo", 0, playerA.getCapitol().getLocation(), PopCenter.PopType.Hamlet ) );
    playerA.add( new PopCenter( game.generateUniqueId(), "foo", 0, playerA.getCapitol().getLocation(), PopCenter.PopType.Hamlet ) );
    playerA.add( new PopCenter( game.generateUniqueId(), "foo", 0, playerA.getCapitol().getLocation(), PopCenter.PopType.Hamlet ) );
    playerA.add( new PopCenter( game.generateUniqueId(), "foo", 0, playerA.getCapitol().getLocation(), PopCenter.PopType.Hamlet ) );
    playerA.add( new PopCenter( game.generateUniqueId(), "foo", 0, playerA.getCapitol().getLocation(), PopCenter.PopType.Hamlet ) );
    playerA.add( new PopCenter( game.generateUniqueId(), "foo", 0, playerA.getCapitol().getLocation(), PopCenter.PopType.Hamlet ) );
    assertEquals( 20, playerA.getSupportCapacity() );
    assertEquals( 9, playerA.getSupportRequired() );
    CreateArmy order = new CreateArmy( playerA.getKing() );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( 20, playerA.getSupportCapacity() );
    assertEquals( 10, playerA.getSupportRequired() );
    assertEquals( 4, playerA.getArmies().size() );
    assertFalse( playerA.getArmies().get( 2 ).isActive() );
  }
}
