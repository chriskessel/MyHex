package kessel.hex.orders.agent;

import kessel.hex.domain.Agent;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.map.ControlLevel;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

/** Test agent recon. */
public class AgentScoutControlLevelTest extends AbstractAgentScoutRegionTest<AgentScoutControlLevel>
{
  private static final Logger LOG = Logger.getLogger( AgentScoutControlLevelTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Agent agent = playerA.getAgents().get( 0 );
    PopCenter city = game.getPopCenter( 2, 2 );
    AgentScoutControlLevel oldOrder = new AgentScoutControlLevel( agent, city );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    AgentScoutControlLevel newOrder = Game.GSON.fromJson( jsonOrder, AgentScoutControlLevel.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testTargetNotCity()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Agent agent = playerA.getAgents().get( 0 );
    PopCenter pop = game.getPopCenter( 1, 1 );
    AgentScoutControlLevel order = new AgentScoutControlLevel( agent, pop );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testTargetTooFar()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Agent agent = playerA.getAgents().get( 0 );
    agent.setBase( game.getPlayers().get( 0 ).getCapitol() );
    agent.setRange( 0 );
    PopCenter pop = game.getPopCenter( 2, 2 );
    AgentScoutControlLevel order = new AgentScoutControlLevel( agent, pop );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testStandardRecon()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Agent agent = playerA.getAgents().get( 0 );
    PopCenter city = game.getPopCenter( 2, 2 );

    AgentScoutControlLevel order = new AgentScoutControlLevel( agent, city );
    assertEquals( ControlLevel.Unknown, playerA.getControlLevelIntel( city.getLocation().getRegion(), playerB ) );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( ControlLevel.Presence, playerA.getControlLevelIntel( city.getLocation().getRegion(), playerB ) );
  }
}
