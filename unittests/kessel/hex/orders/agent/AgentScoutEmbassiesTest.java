package kessel.hex.orders.agent;

import kessel.hex.domain.Agent;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

/** Test agent recon. */
public class AgentScoutEmbassiesTest extends AbstractAgentScoutRegionTest<AgentScoutEmbassies>
{
  private static final Logger LOG = Logger.getLogger( AgentScoutEmbassiesTest.class );

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
    AgentScoutEmbassies oldOrder = new AgentScoutEmbassies( agent, city );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    AgentScoutEmbassies newOrder = Game.GSON.fromJson( jsonOrder, AgentScoutEmbassies.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testTargetNotCity()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Agent agent = playerA.getAgents().get( 0 );
    PopCenter pop = game.getPopCenter( 1, 1 );
    AgentScoutEmbassies order = new AgentScoutEmbassies( agent, pop );
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
    AgentScoutEmbassies order = new AgentScoutEmbassies( agent, pop );
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
    playerB.improveEmbassy( city.getLocation().getRegion() );

    AgentScoutEmbassies order = new AgentScoutEmbassies( agent, city );
    assertEquals( 0, playerA.getKnownEmbassyLevel( city.getLocation().getRegion(), playerB ) );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( 1, playerA.getKnownEmbassyLevel( city.getLocation().getRegion(), playerB ) );
  }
}
