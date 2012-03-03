package kessel.hex.orders.agent;

import kessel.hex.domain.Agent;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.orders.AbstractOrderTest;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

/** Test agent running counter espionage. */
public class CounterEspionageTest extends AbstractOrderTest<CounterEspionage>
{
  private static final Logger LOG = Logger.getLogger( CounterEspionageTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Agent agentForA = game.getPlayers().get( 0 ).getAgents().get( 0 );

    CounterEspionage oldOrder = new CounterEspionage( agentForA );
    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    CounterEspionage newOrder = Game.GSON.fromJson( jsonOrder, CounterEspionage.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testCounterEspionageImpact()
  {
    Game game = GameTest.createSimpleGame();
    Agent agentForA = game.getPlayers().get( 0 ).getAgents().get( 0 );
    Agent agentForB = game.getPlayers().get( 1 ).getAgents().get( 0 );

    AssassinateAgent assassinateOrder = new AssassinateAgent( agentForA, agentForB, agentForB.getBase() );
    assertEquals( 7, assassinateOrder.determineResistance( game ) );

    CounterEspionage counterEspionageOrder = new CounterEspionage( agentForB );
    assertEquals( 0, game.getCurrentTurn().getOrdersOfType( CounterEspionage.class ).size() );
    counterEspionageOrder.execute( game );
    assertTrue( counterEspionageOrder.wasExecuted() );
    assertEquals( 1, game.getCurrentTurn().getOrdersOfType( CounterEspionage.class ).size() );

    assertEquals( 12, assassinateOrder.determineResistance( game ) );
  }
}

